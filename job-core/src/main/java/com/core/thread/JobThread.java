package com.core.thread;

import com.core.bean.model.HandleCallbackParam;
import com.core.bean.model.ReturnT;
import com.core.bean.model.Trigger;
import com.core.executor.JobExecutor;
import com.core.handler.IJobHandler;
import com.core.log.JobFileAppender;
import com.core.utils.ShardingUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Data
public class JobThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobThread.class);
    private int jobId;
    private IJobHandler handler;
    private LinkedBlockingDeque<Trigger> triggerQueue;
    private Set<Long> triggerLogSet;

    private volatile boolean toStop = false;
    private String stopReason;

    private boolean running = false;
    private int idleTimes = 0;

    public JobThread(int jobId, IJobHandler handler) {
        this.jobId = jobId;
        this.handler = handler;
        this.triggerQueue = new LinkedBlockingDeque<>();
        this.triggerLogSet = Collections.synchronizedSet(new HashSet<Long>());
    }

    public ReturnT<String> pushTriggerQueue(Trigger triggerParam) {
        //avoid repeat
        if (triggerLogSet.contains(triggerParam.getLogId())) {
            LOGGER.info(">>>>>>>>>>>>>>>repeat trigger job, jobId:{}", triggerParam.getLogId());
            return new ReturnT<>(ReturnT.FAIL_CODE, "repeat trigger job ,logId:" + triggerParam.getLogId());
        }
        triggerLogSet.add(triggerParam.getLogId());
        triggerQueue.add(triggerParam);
        return ReturnT.SUCCESS;
    }

    /**
     * kill job thread
     */
    public void toStop(String stopReason) {
        /**
         * Thread.interrupt只支持终止线程的阻塞状态(wait、join、sleep)，
         * 在阻塞出抛出InterruptedException异常,但是并不会终止运行的线程本身；
         * 所以需要注意，此处彻底销毁本线程，需要通过共享变量方式；
         */
        this.toStop = true;
        this.stopReason = stopReason;
    }

    /**
     * is running job
     */
    public boolean isRunningOrHasQueue() {
        return running || triggerQueue.size() > 0;
    }

    @Override
    public void run() {

        //init
        try {
            handler.init();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }

        //executor
        while(!toStop){
            running=false;
            idleTimes++;

            Trigger trigger=null;
            ReturnT<String> executorResult=null;

            // to check toStop signal, we need cycle, so wo cannot use queue.take(), instand of poll(timeout)
            try {
                trigger=triggerQueue.poll(3L, TimeUnit.SECONDS);
                if (trigger != null) {
                    running=true;
                    idleTimes=0;
                    triggerLogSet.remove(trigger.getLogId());

                    String logFileName= JobFileAppender.makeLogFileName(new Date(trigger.getLogDateTime()),trigger.getLogId());

                    JobFileAppender.contextHolder.set(logFileName);
                    ShardingUtil.setShadingVo(new ShardingUtil.ShardingVO(trigger.getBroadcastIndex(),trigger.getBroadcastTotal()));


                    LOGGER.info("----------------job job executor start ----------------param:{}",trigger.getExecutorParams());
                    if (trigger.getExecutorTimeout()>0) {
                        Thread thread=null;

                        try {
                            final Trigger triggerTemp=trigger;
                            FutureTask<ReturnT<String>> futureTask=new FutureTask<ReturnT<String>>(new Callable<ReturnT<String>>() {
                                @Override
                                public ReturnT<String> call() throws Exception {
                                    return handler.execute(triggerTemp.getExecutorParams());
                                }
                            });

                            thread=new Thread(futureTask);
                            thread.start();
                            executorResult=futureTask.get(trigger.getExecutorTimeout(),TimeUnit.SECONDS);
                        } catch (Exception e) {
                            LOGGER.error(">>>>>>>>>job job executor timeout");
                            LOGGER.error(e.getMessage(),e);
                            executorResult=new ReturnT<>(IJobHandler.FAIL_TIMEOUT.getCode(),"job executor timeout");
                        }finally {
                            thread.interrupt();
                        }
                    }else{

                        executorResult=handler.execute(trigger.getExecutorParams());
                    }
                    if (executorResult==null) {
                        executorResult=IJobHandler.FAIL;

                    }else{
                        executorResult.setMsg((executorResult!=null&&executorResult.getMsg()!=null&&executorResult.getMsg().length()>50000)?executorResult.getMsg().substring(0,50000).concat("...."):executorResult.getMsg());
                        executorResult.setContent(null);
                    }
                    LOGGER.info("------------------job job execute end(finish)-----------------ReturnT:{}",executorResult);
                }else{
                    if (idleTimes>30) {
                        if (triggerQueue.size()==0) {
                            JobExecutor.removeJobThread(jobId,"executor idel time over limit");
                        }
                    }
                }
            } catch (Exception e) {
                if (toStop) {
                    LOGGER.error("JobThread toStop stopReason:{}",stopReason);
                }
            }finally {
                if (trigger != null) {
                    if (!toStop) {
                        TriggerCallbackThread.pushCallBack(new HandleCallbackParam(trigger.getLogId(),trigger.getLogDateTime(),executorResult));
                    }else{
                        ReturnT<String> stopResult=new ReturnT<>(ReturnT.FAIL_CODE,stopReason+"[job running killed]");
                        TriggerCallbackThread.pushCallBack(new HandleCallbackParam(trigger.getLogId(),trigger.getLogDateTime(),stopResult));
                    }
                }
            }
        }

        //callback trigger request in queue
        while(triggerQueue!=null&&triggerQueue.size()>0){
            Trigger triggerParam=triggerQueue.poll();
            if (triggerParam != null) {
                //is killed
                ReturnT<String> stopResult=new ReturnT(ReturnT.FAIL_CODE,stopReason+"[job not executed ,in the job queue,killed.]");
                TriggerCallbackThread.pushCallBack(new HandleCallbackParam(triggerParam.getLogId(),triggerParam.getLogDateTime(),stopResult));
            }
        }
        //destroy
        try {
            handler.destroy();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
        LOGGER.info(">>>>>>>>>>>>>>>>>job JobThread stop, hashCode:{}",Thread.currentThread());
    }
}
