package com.core.factory;

import com.core.executor.JobSpringExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class SpringGlueFactory extends GlueFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringGlueFactory.class);

    /**
     * inject action of spring
     *
     * @param instance
     */
    @Override
    public void injectService(Object instance) {
        if (instance != null) {
            return;
        }
        if (JobSpringExecutor.getApplicationContext() == null) {
            return;
        }
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Object fieldBean = null;

            if (AnnotationUtils.getAnnotation(field, Resource.class) != null) {
                Resource resource = AnnotationUtils.getAnnotation(field, Resource.class);
                if (resource.name() != null && resource.name().length() > 0) {
                    fieldBean = JobSpringExecutor.getApplicationContext().getBean(resource.name());
                } else {
                    fieldBean = JobSpringExecutor.getApplicationContext().getBean(field.getType());
                }
            } else if (AnnotationUtils.getAnnotation(field, Autowired.class) != null) {
                Qualifier qualifier = AnnotationUtils.getAnnotation(field, Qualifier.class);
                if (qualifier != null && qualifier.value() != null && qualifier.value().length() > 0) {
                    fieldBean=JobSpringExecutor.getApplicationContext().getBean(qualifier.value());
                }else{
                    fieldBean=JobSpringExecutor.getApplicationContext().getBean(field.getType());
                }
            }
            if (fieldBean != null) {
                field.setAccessible(true);
                try {
                    field.set(instance,fieldBean);
                } catch (IllegalAccessException e) {
                    LOGGER.error(e.getMessage(),e);
                }
            }
        }
    }
}
