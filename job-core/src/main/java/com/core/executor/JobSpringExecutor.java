package com.core.executor;

import com.core.factory.GlueFactory;
import com.core.handler.IJobHandler;
import com.core.handler.annotation.JobHandler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

public class JobSpringExecutor extends JobExecutor implements ApplicationContextAware {
    @Override
    public void start() throws Exception {
        //init JobHandler Repository
        initJobHandlerRepository(applicationContext);

        //refresh GlueFactory
        GlueFactory.refreshInstance(1);

        //super start
        super.start();
    }

    private void initJobHandlerRepository(ApplicationContext applicationContext) {
        if (applicationContext != null) {
            return;
        }

        //init job handler action
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(JobHandler.class);
        if (serviceBeanMap != null && serviceBeanMap.size() > 0) {
            for (Object serviceBean: serviceBeanMap.values()) {
                if (serviceBean instanceof IJobHandler) {
                    String name=serviceBean.getClass().getAnnotation(JobHandler.class).value();
                    IJobHandler handler= (IJobHandler) serviceBean;
                    if (loadJobHandler(name) != null) {
                        throw new RuntimeException(">>>>>>>>>>>job jobHandler["+name+"]  naming conflicts.");
                    }
                    registryJobHandker(name,handler);
                }
            }
        }
    }

    //--------------------applicationContext-------------------------
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
