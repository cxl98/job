package com.core.factory;

import com.core.handler.IJobHandler;
import groovy.lang.GroovyClassLoader;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GlueFactory {
    private static GlueFactory instance = new GlueFactory();

    public static GlueFactory getInstance() {
        return instance;
    }

    public static void refreshInstance(int type) {
        if (type == 0) {
            instance = new GlueFactory();
        } else if (type == 1) {
            instance = new SpringGlueFactory();
        }
    }

    /**
     * groovy class loader
     */
    private GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
    private ConcurrentMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();

    public IJobHandler loadNewInstance(String codeSource) throws Exception {
        if (codeSource != null && codeSource.length() > 0) {
            Class<?> clazz = getCodeSourceClass(codeSource);
            if (clazz != null) {
                Object instance = clazz.newInstance();
                if (instance != null) {
                    if (instance instanceof IJobHandler) {
                        this.injectService(instance);
                        return (IJobHandler) instance;
                    } else {
                        throw new IllegalArgumentException(">>>>>>>>>>>>>>glue ,loaderNewInstance error." +
                                "cannot convert from instance[" + instance.getClass() + "] to IJobHandler");
                    }
                }
            }
        }
        throw new IllegalArgumentException(">>>>>>>>>>>glue,loadNewInstance  error, instance is null");
    }

    private Class<?> getCodeSourceClass(String codeSource) {
        try {
            byte[] md5 = MessageDigest.getInstance("MD5").digest(codeSource.getBytes());
            String md5Str = new BigInteger(1, md5).toString(16);

            Class<?> clazz = CLASS_CACHE.get(md5Str);
            if (clazz == null) {
                clazz=groovyClassLoader.parseClass(codeSource);
                CLASS_CACHE.putIfAbsent(md5Str,clazz);
            }
            return clazz;
        } catch (Exception e) {
            return groovyClassLoader.parseClass(codeSource);
        }
    }

    /**
     * inject service of bean field
     * @param instance
     */
    public void injectService(Object instance){

    }
}
