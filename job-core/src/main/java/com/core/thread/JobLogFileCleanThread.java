package com.core.thread;

import com.core.log.JobFileAppender;
import com.core.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class JobLogFileCleanThread {
    private static final Logger LOGGER=LoggerFactory.getLogger(JobLogFileCleanThread.class);
    private Thread thread;
    private volatile  boolean toStop=false;
    private static JobLogFileCleanThread instance=new JobLogFileCleanThread();
    public static JobLogFileCleanThread getIntance() {
        return instance;
    }

    public void start(final long logRetentionDays) {

        //limit min value
        if (logRetentionDays<3){
            return;
        }
        thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while(!toStop){
                    //clean log dir , over logRetentionDays
                    try {
                        File[] childDirs=new File(JobFileAppender.getLogPath()).listFiles();
                        if (childDirs != null&& childDirs.length>0) {

                            //today
                            Calendar todayCal=Calendar.getInstance();
                            todayCal.set(Calendar.HOUR_OF_DAY,0);
                            todayCal.set(Calendar.MINUTE,0);
                            todayCal.set(Calendar.SECOND,0);
                            todayCal.set(Calendar.MILLISECOND,0);

                            Date date=todayCal.getTime();

                            for (File childFile: childDirs) {

                                //valid
                                if (!childFile.isDirectory()) {
                                    continue;
                                }
                                if (childFile.getName().indexOf("-")==-1) {
                                    continue;
                                }

                                //file create date

                                Date logFileCreateDate=null;
                                try {
                                    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
                                    logFileCreateDate=simpleDateFormat.parse(childFile.getName());
                                } catch (ParseException e) {
                                    LOGGER.error(e.getMessage(),e);
                                }
                                if (logFileCreateDate==null) {
                                    continue;
                                }

                                if (date.getTime()-logFileCreateDate.getTime()>=logRetentionDays*(24*60*60*1000)) {
                                    FileUtil.deleteRecursively(childFile);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (!toStop){
                            LOGGER.error(e.getMessage(),e);
                        }
                    }

                    try {
                        TimeUnit.DAYS.sleep(1);
                    } catch (InterruptedException e) {
                        if (!toStop) {
                            LOGGER.error(e.getMessage(),e);
                        }
                    }
                }
                LOGGER.info(">>>>>>>>>>>>job , executor JobLogFileCleanThread thread destory");
            }
        });
        thread.setDaemon(true);
        thread.setName("job , executor JobLogFileCleanThread");
    }
    public void toStop(){
        toStop=true;

        if (thread==null) {
            return;
        }
        //interrupt and wait
        thread.interrupt();

        try {
            thread.join();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(),e);
        }
    }
}
