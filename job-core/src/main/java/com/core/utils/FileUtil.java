package com.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class FileUtil {
    private static final Logger LOGGER= LoggerFactory.getLogger(FileUtil.class);

    public static boolean deleteRecursively(File root){
        if (root != null&&root.exists()) {
            if (root.isDirectory()) {
                File[] children=root.listFiles();
                if (children != null) {
                    for (File child: children) {
                        deleteRecursively(child);
                    }
                }
            }
            return root.delete();
        }
        return false;
    }

    public static void deleteFile(String fileName){
        //file
        File file=new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void writeFileContent(File file,byte[] data){
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        //append file content
        BufferedOutputStream out=null;
        try {
            out=new BufferedOutputStream(new FileOutputStream(file));
            out.write(data);
            out.flush();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(),e);
                }
            }
        }
    }

    public static byte[] readFileContent(File file){
        Long filelength=file.length();
        byte[] filecontent=new byte[filelength.intValue()];
        BufferedInputStream in=null;
        try {
            in=new BufferedInputStream(new FileInputStream(file));
            in.read(filecontent);
            in.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(),e);
                }
            }
        }
        return filecontent;
    }

    public static void main(String[] args) {
        String path="/home/cxl/web/xxl-job/data/applogs/xxl-job/";
        File file=new File(path);
        byte[] bytes = readFileContent(file);
        System.out.println("content"+new String(bytes));
        boolean b = deleteRecursively(file);
        System.out.println(b);
    }
}
