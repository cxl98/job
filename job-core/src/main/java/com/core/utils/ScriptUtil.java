package com.core.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.FileOutputStream;
import java.io.IOException;

public class ScriptUtil {
    /**
     * make script file
     *
     * @param scriptFile
     * @param content
     * @throws IOException
     */
    public static void markScriptFile(String scriptFile, String content) throws IOException {

        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(scriptFile);
            fileOutputStream.write(content.getBytes("UTF-8"));
            fileOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    public static int execuToFile(String command, String scriptFile, String logFile, String... params) {

        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(logFile, true);
            PumpStreamHandler streamHandler = new PumpStreamHandler(fileOutputStream, fileOutputStream, null);

            //command
            CommandLine commandLine = new CommandLine(command);
            commandLine.addArgument(scriptFile);
            if (params != null && params.length > 0) {
                commandLine.addArguments(params);
            }

            //exec
            DefaultExecutor executor=new DefaultExecutor();
            executor.setExitValues(null);
            executor.setStreamHandler(streamHandler);
            int exitValue=executor.execute(commandLine);//exit code 0=success, 1=error
            return exitValue;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
