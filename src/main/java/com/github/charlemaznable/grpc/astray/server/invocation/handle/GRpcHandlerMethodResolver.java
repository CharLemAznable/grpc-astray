package com.github.charlemaznable.grpc.astray.server.invocation.handle;

import com.github.charlemaznable.core.lang.Clz.DepthComparator;
import com.github.charlemaznable.core.lang.Mapp;
import com.github.charlemaznable.grpc.astray.server.GRpcService;
import com.github.charlemaznable.grpc.astray.server.autoconfigure.GRpcServicesRegistry;
import com.github.charlemaznable.grpc.astray.server.common.GRpcRuntimeExceptionAdapter;
import com.github.charlemaznable.grpc.astray.server.invocation.GRpcCleanupHandler;
import com.github.charlemaznable.grpc.astray.server.invocation.GRpcExceptionHandler;
import com.github.charlemaznable.grpc.astray.server.invocation.GRpcRequestHandler;
import com.github.charlemaznable.grpc.astray.server.invocation.GRpcResponseHandler;
import com.github.charlemaznable.grpc.astray.server.invocation.GRpcServiceAdvice;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ExceptionDepthComparator;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.charlemaznable.core.lang.Condition.checkNullRun;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.google.common.cache.CacheLoader.from;
import static java.util.Objects.isNull;
import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;

@Slf4j(topic = "grpc.invocation.handler.resolver")
final class GRpcHandlerMethodResolver {

    private Map<String, GRpcHandlerMethodResolver> privateResolvers;

    GRpcHandlerMethodResolver(ApplicationContext applicationContext, GRpcServicesRegistry registry) {
        // register GRpcServiceAdvice as global, which except beans annotated with GRpcService.
        this(applicationContext.getBeansWithAnnotation(GRpcServiceAdvice.class).values().stream()
                .filter(o -> !hasAnnotation(o.getClass(), GRpcService.class))
                .collect(Collectors.toList()));

        privateResolvers = registry.getServiceNameToServiceMap().entrySet().stream()
                .collect(Mapp.toMap(Map.Entry::getKey, entry -> new GRpcHandlerMethodResolver(
                        Collections.singleton(entry.getValue().getServiceBean()))));
    }

    List<GRpcRequestHandlerMethod> resolveMethodsByRequest(String grpcServiceName, Object request) {
        if (isNull(request)) return newArrayList();
        return Stream.concat(Optional.ofNullable(privateResolvers)
                        .map(r -> r.get(grpcServiceName))
                        .map(r -> r.resolveMethodsByRequest(grpcServiceName, request).stream())
                        .orElseGet(Stream::empty),
                get(lookupRequestCache, request.getClass()).stream()).collect(Collectors.toList());
    }

    List<GRpcResponseHandlerMethod> resolveMethodsByResponse(String grpcServiceName, Object response) {
        if (isNull(response)) return newArrayList();
        return Stream.concat(Optional.ofNullable(privateResolvers)
                        .map(r -> r.get(grpcServiceName))
                        .map(r -> r.resolveMethodsByResponse(grpcServiceName, response).stream())
                        .orElseGet(Stream::empty),
                get(lookupResponseCache, response.getClass()).stream()).collect(Collectors.toList());
    }

    Optional<Pair<GRpcExceptionHandlerMethod, Throwable>> resolveMethodByThrowable(String grpcServiceName, Throwable ex) {
        if (isNull(ex)) return Optional.empty();
        val exception = GRpcRuntimeExceptionAdapter.unwrap(ex);

        val privateHandler = Optional.ofNullable(privateResolvers)
                .map(r -> r.get(grpcServiceName))
                .flatMap(r -> r.resolveMethodByThrowable(grpcServiceName, exception));
        if (privateHandler.isPresent()) return privateHandler;

        val cacheHandlerOptional = get(lookupExceptionCache, exception.getClass());
        return cacheHandlerOptional.map(cacheHandler -> Optional.of(Pair.of(cacheHandler, exception)))
                .orElseGet(() -> Optional.ofNullable(exception.getCause())
                        .flatMap(cause -> resolveMethodByThrowable(grpcServiceName, cause)));

    }

    List<GRpcCleanupHandlerMethod> resolveCleanupMethods(String grpcServiceName) {
        return Stream.concat(Optional.ofNullable(privateResolvers)
                        .map(r -> r.get(grpcServiceName))
                        .map(r -> r.resolveCleanupMethods(grpcServiceName).stream())
                        .orElseGet(Stream::empty),
                cleanupHandlers.stream()).collect(Collectors.toList());
    }

    private final LoadingCache<Class<?>, List<GRpcRequestHandlerMethod>> lookupRequestCache
            = simpleCache(from(this::lookupRequestMethods));
    private final LoadingCache<Class<?>, List<GRpcResponseHandlerMethod>> lookupResponseCache
            = simpleCache(from(this::lookupResponseMethods));
    private final LoadingCache<Class<? extends Throwable>, Optional<GRpcExceptionHandlerMethod>> lookupExceptionCache
            = simpleCache(from(this::lookupExceptionMethod));

    @Nonnull
    private List<GRpcRequestHandlerMethod> lookupRequestMethods(@Nonnull Class<?> requestType) {
        return requestHandlers.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(requestType))
                .map(Entry::getValue)
                .sorted(new GRpcHandlerMethodComparator<>(requestType,
                        GRpcRequestHandlerMethod::getRequestType))
                .collect(Collectors.toList());
    }

    @Nonnull
    private List<GRpcResponseHandlerMethod> lookupResponseMethods(@Nonnull Class<?> responseType) {
        return responseHandlers.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(responseType))
                .map(Entry::getValue)
                .sorted(new GRpcHandlerMethodComparator<>(responseType,
                        GRpcResponseHandlerMethod::getResponseType))
                .collect(Collectors.toList());
    }

    @Nonnull
    private Optional<GRpcExceptionHandlerMethod> lookupExceptionMethod(@Nonnull Class<? extends Throwable> exceptionType) {
        val matches = new ArrayList<Class<? extends Throwable>>();
        exceptionHandlers.keySet().stream().filter(mapped ->
                mapped.isAssignableFrom(exceptionType)).forEach(matches::add);
        if (matches.isEmpty()) return Optional.empty();
        if (matches.size() > 1) matches.sort(new ExceptionDepthComparator(exceptionType));
        return Optional.of(exceptionHandlers.get(matches.get(0)));
    }

    private static class GRpcHandlerMethodComparator<T> implements Comparator<T> {

        private final DepthComparator depthComparator;
        private final Function<T, Class<?>> typeFunction;

        public GRpcHandlerMethodComparator(Class<?> invocationType,
                                           Function<T, Class<?>> typeFunction) {
            this.depthComparator = new DepthComparator(invocationType);
            this.typeFunction = typeFunction;
        }

        @Override
        public int compare(T o1, T o2) {
            return depthComparator.compare(typeFunction.apply(o1), typeFunction.apply(o2));
        }
    }

    private final Map<Class<?>, GRpcRequestHandlerMethod> requestHandlers = new HashMap<>(16);
    private final Map<Class<?>, GRpcResponseHandlerMethod> responseHandlers = new HashMap<>(16);
    private final Map<Class<? extends Throwable>, GRpcExceptionHandlerMethod> exceptionHandlers = new HashMap<>(16);
    private final List<GRpcCleanupHandlerMethod> cleanupHandlers = new ArrayList<>();

    private GRpcHandlerMethodResolver(Collection<Object> advices) {
        val tempHandlers = new ArrayList<GRpcCleanupHandlerMethod>(16);
        for (val advice : advices) {
            MethodIntrospector.selectMethods(advice.getClass(), this::requestFilter).forEach(method ->
                    GRpcRequestHandlerMethod.create(advice, method).ifPresent(handler -> {
                        val requestType = handler.getRequestType();
                        val oldHandler = requestHandlers.get(requestType);
                        checkNullRun(oldHandler, () -> requestHandlers.put(requestType, handler), old ->
                                log.warn("Ambiguous @GRpcRequestHandler method mapped for [{}]: {} vs {}",
                                        requestType, old.getMethod(), handler.getMethod()));
                    }));
            MethodIntrospector.selectMethods(advice.getClass(), this::responseFilter).forEach(method ->
                    GRpcResponseHandlerMethod.create(advice, method).ifPresent(handler -> {
                        val responseType = handler.getResponseType();
                        val oldHandler = responseHandlers.get(responseType);
                        checkNullRun(oldHandler, () -> responseHandlers.put(responseType, handler), old ->
                                log.warn("Ambiguous @GRpcResponseHandler method mapped for [{}]: {} vs {}",
                                        responseType, old.getMethod(), handler.getMethod()));
                    }));
            MethodIntrospector.selectMethods(advice.getClass(), this::exceptionFilter).forEach(method ->
                    GRpcExceptionHandlerMethod.create(advice, method).ifPresent(handler -> {
                        val exceptionType = handler.getExceptionType();
                        val oldHandler = exceptionHandlers.get(exceptionType);
                        checkNullRun(oldHandler, () -> exceptionHandlers.put(exceptionType, handler), old ->
                                log.warn("Ambiguous @GRpcExceptionHandler method mapped for [{}]: {} vs {}",
                                        exceptionType, old.getMethod(), handler.getMethod()));
                    }));
            MethodIntrospector.selectMethods(advice.getClass(), this::cleanupFilter).forEach(method ->
                    GRpcCleanupHandlerMethod.create(advice, method).ifPresent(tempHandlers::add));
        }
        tempHandlers.stream().sorted(new AnnotationAwareOrderComparator().withSourceProvider(
                o -> ((GRpcCleanupHandlerMethod) o).getMethod())).forEach(cleanupHandlers::add);
    }

    private boolean requestFilter(Method method) {
        return hasAnnotation(method, GRpcRequestHandler.class) &&
                !hasAnnotation(method, GRpcResponseHandler.class);
    }

    private boolean responseFilter(Method method) {
        return hasAnnotation(method, GRpcResponseHandler.class) &&
                !hasAnnotation(method, GRpcRequestHandler.class);
    }

    private boolean exceptionFilter(Method method) {
        return hasAnnotation(method, GRpcExceptionHandler.class);
    }

    private boolean cleanupFilter(Method method) {
        return hasAnnotation(method, GRpcCleanupHandler.class);
    }
}
