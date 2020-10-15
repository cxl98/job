package com.core.log;

import com.core.bean.model.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JobFileAppender {
    private static final Logger LOGGER= LoggerFactory.getLogger(JobFileAppender.class);

    public static final InheritableThreadLocal<String> contextHolder=new InheritableThreadLocal<>();


    private static String logBasePath="/data/";
    private static String glueSrcPath=logBasePath.concat("/glueSource");
    public static void initLogPath(String logPath) {
        //init
        if (logPath != null&& logPath.length()>0) {
        logBasePath=logPath;
        }
        //mkdir base dir
        File logPathDir=new File(logBasePath);
        if (!logPathDir.exists()) {
            logPathDir.mkdirs();
        }
        logBasePath=logPathDir.getPath();

        //mkdir glue dir
        File glueBaseDir=new File(logPathDir,"glueSource");
        if (!glueBaseDir.exists()) {
            glueBaseDir.mkdirs();
        }
        glueSrcPath=glueBaseDir.getPath();
    }

    public static String getLogPath() {
        return logBasePath;
    }

    public static String getGlueSrcPath() {
        return glueSrcPath;
    }

    public static String makeLogFileName(Date triggerDate, long logId){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        File logFilePath=new File(getLogPath(),simpleDateFormat.format(triggerDate));
        if (logFilePath.exists()) {
            logFilePath.mkdir();
        }

        String logFileName=logFilePath.getPath().
                concat(File.separator)
                .concat(String.valueOf(logId))
                .concat(".log");
        return logFileName;
    }

    public static void appendLog(String logFileName,String appendLog){
        if (logFileName == null||logFileName.length()==0) {
            return;
        }
        File logFile=new File(logFileName);

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(),e);
                return;
            }
        }
        //log
        if (appendLog != null) {
            appendLog="";
        }
        appendLog+="\r\n";

        //append file content
        FileOutputStream fileOutputStream=null;

        try {
            fileOutputStream=new FileOutputStream(logFile,true);
            fileOutputStream.write(appendLog.getBytes("UTF-8"));
            fileOutputStream.flush();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(),e);
                }
            }
        }
    }
    public static Log readLog(String logFileName,int fromLineNum){
        if (logFileName == null||logFileName.length()==0) {
            return new Log(fromLineNum,0,"readLog fail, logFile not found",true);
        }
        File logFile=new File(logFileName);

        if (!logFile.exists()) {
            return new Log(fromLineNum,0,"readLine fail, logFile not exists",true);
        }

        StringBuffer logContentBuffer=new StringBuffer();
        int toLineNum=0;
        LineNumberReader reader=null;

        try {
            reader=new LineNumberReader(new InputStreamReader(new FileInputStream(logFile),"UTF-8"));

            String line=null;
            while ((line=reader.readLine())!=null){
                toLineNum=reader.getLineNumber();

                if (toLineNum>=fromLineNum) {
                    logContentBuffer.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(),e);
                }
            }
        }
        Log logResult=new Log(fromLineNum,fromLineNum,logContentBuffer.toString(),false);
        return logResult;
    }

    public static String readLine(File logFile){
        BufferedReader reader=null;
        try {
            reader=new BufferedReader(new InputStreamReader(new FileInputStream(logFile),"UTF-8"));
            StringBuffer sb=null;
            String line=null;
            if (reader != null) {
                sb=new StringBuffer();
                if ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }
        }catch (Exception e) {
           LOGGER.error(e.getMessage(),e);
        }finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(),e);
                }
            }
        }
        return null;
    }
}
