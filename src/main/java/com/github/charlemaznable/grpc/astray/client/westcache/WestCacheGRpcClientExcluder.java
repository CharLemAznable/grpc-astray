package com.github.charlemaznable.grpc.astray.client.westcache;

import com.github.bingoohuang.westcache.spring.exclude.WestCacheExcludeAnnotationTypeSupplier;
import com.github.charlemaznable.grpc.astray.client.GRpcClient;
import com.google.auto.service.AutoService;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;

@AutoService(WestCacheExcludeAnnotationTypeSupplier.class)
public final class WestCacheGRpcClientExcluder implements WestCacheExcludeAnnotationTypeSupplier {

    @Override
    public List<Class<? extends Annotation>> get() {
        return newArrayList(GRpcClient.class);
    }
}
