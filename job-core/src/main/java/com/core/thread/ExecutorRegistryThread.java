package com.core.thread;

import com.core.bean.Admin;
import com.core.bean.model.RegistryParam;
import com.core.bean.model.ReturnT;
import com.core.enums.RegistryConfig;
import com.core.executor.JobExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ExecutorRegistryThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorRegistryThread.class);

    private static ExecutorRegistryThread instance = new ExecutorRegistryThread();

    public static ExecutorRegistryThread getInstance() {
        return instance;
    }

    private Thread registryThread;
    private volatile boolean toStop = false;

    public void start(final String appName, final String address) {

        //valid
        if (appName == null && appName.length() == 0) {
            LOGGER.warn(">>>>>>>>>>>>>>>job, executor registry config fail appName is null");
            return;
        }
        if (JobExecutor.getAdminList() == null) {
            LOGGER.warn(">>>>>>>>>>>>>>>>>>job executor registry config fail appName is null");
            return;
        }

        registryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //registry
                while (!toStop) {
                    try {
                        RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistryType.EXECUTOR.name(), appName, address);
                        for (Admin admin : JobExecutor.getAdminList()) {
                            try {
                                ReturnT<String> registryResult = admin.registry(registryParam);
                                if (registryResult != null && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                                    registryResult = ReturnT.SUCCESS;
                                    LOGGER.debug(">>>>>>>>>>>>>>>>job registry success, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                                }
                            } catch (Exception e) {
                                LOGGER.error(">>>>>>>>>>>job registry error registryParam:{}", registryParam, e);
                            }
                        }
                    } catch (Exception e) {
                        if (!toStop) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                    try {
                        if (!toStop) {
                            TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                        }
                    } catch (Exception e) {
                        if (!toStop) {
                            LOGGER.warn(">>>>>>>>>>>>>>>job , executor registry thread interrupted,error msg:{}", e.getMessage());
                        }
                    }
                }

                //registry remove
                try {
                    RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistryType.EXECUTOR.name(), appName, address);
                    for (Admin admin : JobExecutor.getAdminList()) {
                        try {
                            ReturnT<String> registryResult = admin.registryRemove(registryParam);
                            if (registryResult != null &&ReturnT.SUCCESS_CODE==registryResult.getCode()) {
                                  registryResult=ReturnT.SUCCESS;
                                  LOGGER.info(">>>>>>>>>>>>>>>>job registry-remove success, registryParam:{}, registryResult:{}",new Object[]{registryParam,registryResult});
                                  break;
                            }else{
                                LOGGER.info(">>>>>>>>>>>job registry-remove fail,registryParam:{}, registryResult:{}",new Object[]{registryParam,registryResult});
                            }
                        } catch (Exception e) {
                            if (!toStop) {
                                LOGGER.info(">>>>>>>>>>>>>>>job registry-remove error ,registryParam:{}",registryParam,e);
                            }
                        }

                    }
                } catch (Exception e) {
                    if (!toStop) {
                        LOGGER.error(e.getMessage(),e);
                    }
                    LOGGER.info(">>>>>>>>>>>job executor registry thread destory");
                }

            }
        });
        registryThread.setDaemon(true);
       registryThread.setName("job executor ExecutorRegistryThread");
       registryThread.start();
    }
    public void toStop(){
        toStop=true;
        //interrupt and wait
        try {
            registryThread.join();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(),e);
        }
    }
}
