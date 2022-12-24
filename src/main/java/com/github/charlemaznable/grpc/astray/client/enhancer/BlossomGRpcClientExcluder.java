package com.github.charlemaznable.grpc.astray.client.enhancer;

import blossom.spring.exclude.BlossomExcludeAnnotationTypeSupplier;
import com.github.charlemaznable.grpc.astray.client.GRpcClient;
import com.google.auto.service.AutoService;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;

@AutoService(BlossomExcludeAnnotationTypeSupplier.class)
public final class BlossomGRpcClientExcluder implements BlossomExcludeAnnotationTypeSupplier {

    @Override
    public List<Class<? extends Annotation>> get() {
        return newArrayList(GRpcClient.class);
    }
}
