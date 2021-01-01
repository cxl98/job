package com.core.thread;

import com.core.bean.model.HandleCallbackParam;
import com.core.executor.JobExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingDeque;

public class TriggerCallbackThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(TriggerCallbackThread.class);
    private static TriggerCallbackThread instance = new TriggerCallbackThread();

    public static TriggerCallbackThread getInstance() {
        return instance;
    }

    /**
     * job result callback queue
     */
    private LinkedBlockingDeque<HandleCallbackParam> callbackQueue=new LinkedBlockingDeque<>();
    public static void pushCallBack(HandleCallbackParam callbackParam){
        getInstance().callbackQueue.add(callbackParam);
        LOGGER.debug(">>>>>>>>>>>>>>>>>>>>job ,push callback request, logId:{}",callbackParam.getLogId());
    }

    private Thread triggerCallbackThread;
    private Thread triggerRetryCallbackThread;
    private volatile boolean toStop=false;

    public void start(){

        //valid
        if (JobExecutor.getAdminList()==null) {

        }
    }
    public void toStop(){
        toStop=true;
        if (triggerCallbackThread != null) {
            triggerCallbackThread.interrupt();
            try {
                triggerCallbackThread.join();
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(),e);
            }
        }

        if (triggerRetryCallbackThread != null) {
            triggerRetryCallbackThread.interrupt();
            try {
                triggerRetryCallbackThread.join();
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(),e);
            }
        }
    }

}
