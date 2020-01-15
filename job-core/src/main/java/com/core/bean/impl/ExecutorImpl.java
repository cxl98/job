package com.core.bean.impl;

import com.core.bean.Executor;
import com.core.bean.model.Log;
import com.core.bean.model.ReturnT;
import com.core.bean.model.Trigger;
import com.core.enums.ExecutorBlockStrategyEmun;
import com.core.enums.GlueTypeEnum;
import com.core.executor.JobExecutor;
import com.core.factory.GlueFactory;
import com.core.handler.IJobHandler;
import com.core.handler.impl.GlueJobHandler;
import com.core.handler.impl.ScriptJobHandler;
import com.core.log.JobFileAppender;
import com.core.thread.JobThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class ExecutorImpl implements Executor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorImpl.class);

    @Override
    public ReturnT<String> beat() {
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> idleBeat(int jobId) {

        boolean isRunningOrHasQueue = false;
        JobThread jobThread = JobExecutor.loadJobThread(jobId);
        if (jobThread != null || jobThread.isRunningOrHasQueue()) {
            isRunningOrHasQueue = true;
        }
        if (isRunningOrHasQueue) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "jobThread is running or has trigger queue");
        }
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> kill(int jobId) {
        JobThread jobThread = JobExecutor.loadJobThread(jobId);
        if (jobThread != null) {
            JobExecutor.removeJobThread(jobId, "scheduling center kill job");
            return ReturnT.SUCCESS;
        }
        return new ReturnT<>(ReturnT.SUCCESS_CODE, "job thread already killed");
    }

    @Override
    public ReturnT<Log> log(long logDateTim, long logId, int fromLoneNum) {
        String logFileName = JobFileAppender.makeLogFileName(new Date(logDateTim), logId);

        Log logResult = JobFileAppender.readLog(logFileName, fromLoneNum);

        return new ReturnT<>(logResult);
    }

    @Override
    public ReturnT<String> run(Trigger trigger) {
        //load old:jobHandler+jobThread

        JobThread jobThread = JobExecutor.loadJobThread(trigger.getJobId());

        IJobHandler jobHandler = jobThread != null ? jobThread.getHandler() : null;
        String removeOldReason = null;

        //valid:jobHandler+jobThread
        GlueTypeEnum glueType = GlueTypeEnum.match(trigger.getGlueType());

        if (GlueTypeEnum.GULE_JAVA == glueType) {
            //new jobHandler
            IJobHandler newJobHandler = JobExecutor.loadJobHandler(trigger.getExecutorHandler());

            if (jobThread != null && jobHandler != newJobHandler) {
                removeOldReason = "change jobHandler or glue type,and terminate the old job thread";

                jobThread = null;
                jobHandler = null;
            }
            //newHandler
            if (jobHandler == null) {
                jobHandler = newJobHandler;
                if (jobHandler == null) {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "job handler [" + trigger.getExecutorHandler() + "] not found");
                }
            }
        } else if (GlueTypeEnum.GULE_JAVA == glueType) {

            if ((jobThread != null) && !(jobThread.getHandler() instanceof GlueJobHandler) && ((GlueJobHandler) jobThread.getHandler()).getGlueUpdateTime() == trigger.getGlueUpdateTime()) {
                removeOldReason = "change job source or glue type, and terminate the old job thread.";
                jobHandler = null;
                jobThread = null;
            }

            if (jobHandler == null) {
                try {
                    IJobHandler originJobHandler = GlueFactory.getInstance().loadNewInstance(trigger.getGlueSource());
                    jobHandler = new GlueJobHandler(originJobHandler, trigger.getGlueUpdateTime());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                    return new ReturnT<>(ReturnT.FAIL_CODE, e.getMessage());
                }
            }
        } else if (glueType != null && glueType.isScript()) {
            if (jobThread != null && !((jobThread.getHandler() instanceof ScriptJobHandler) && ((ScriptJobHandler) jobThread.getHandler()).getGlueUpdateTime() == trigger.getGlueUpdateTime())) {
                removeOldReason = "change job source or glue type , and terminate the old job thread.";

                jobThread = null;
                jobHandler = null;
            }

            if (jobHandler == null) {
                jobHandler = new ScriptJobHandler(trigger.getJobId(), trigger.getGlueUpdateTime(), trigger.getGlueSource(), GlueTypeEnum.match(trigger.getGlueType()));
            }
        } else {
            return new ReturnT<>(ReturnT.FAIL_CODE, "glueType[" + trigger.getGlueType() + "] is not valid.");
        }


        if (jobThread != null) {
            ExecutorBlockStrategyEmun blockStrategy = ExecutorBlockStrategyEmun.match(trigger.getExecutorBlockStrategy(), null);
            if (ExecutorBlockStrategyEmun.DISCARD_LATED == blockStrategy) {
                if (jobThread.isRunningOrHasQueue()) {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "block strategy effect:" + ExecutorBlockStrategyEmun.DISCARD_LATED.getTitle());
                }
            } else if (ExecutorBlockStrategyEmun.COVER_EARLY == blockStrategy) {
                if (jobThread.isRunningOrHasQueue()) {
                    removeOldReason = "block strategy effect:" + ExecutorBlockStrategyEmun.COVER_EARLY.getTitle();
                    jobThread = null;
                }
            }
        }
        if (jobThread==null) {
            jobThread=JobExecutor.registryJobThread(trigger.getJobId(),jobHandler,removeOldReason);
        }

        //push data to queue
        ReturnT<String> pushResult=jobThread.pushTriggerQueue(trigger);
        return pushResult;
    }
}

