package com.core.handler.impl;

import com.core.bean.model.ReturnT;
import com.core.enums.GlueTypeEnum;
import com.core.handler.IJobHandler;
import com.core.log.JobFileAppender;
import com.core.utils.ScriptUtil;
import com.core.utils.ShardingUtil;
import lombok.Data;

import java.io.File;

@Data
public class ScriptJobHandler extends IJobHandler {
    private int jobId;
    private long glueUpdateTime;
    private String glueSource;
    private GlueTypeEnum glueType;

    public ScriptJobHandler(int jobId, long glueUpdateTime, String glueSource, GlueTypeEnum glueType) {
        this.jobId = jobId;
        this.glueUpdateTime = glueUpdateTime;
        this.glueSource = glueSource;
        this.glueType = glueType;

        //clean old script file
        File glueSrcPath = new File(JobFileAppender.getGlueSrcPath());
        if (glueSrcPath.exists()) {
            File[] glueSrcFileList = glueSrcPath.listFiles();
            if (glueSrcFileList != null && glueSrcFileList.length > 0) {
                for (File glueSrcFileItem : glueSrcFileList) {
                    if (glueSrcFileItem.getName().startsWith(String.valueOf(jobId) + "_")) {
                        glueSrcFileItem.delete();
                    }
                }
            }
        }
    }

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        if (!glueType.isScript()) {
            return new ReturnT<>(IJobHandler.FAIL.getCode(), "glueType[" + glueType + "] invalid.");
        }
        //cmd
        String cmd = glueType.getCmd();

        //make script file
        String scriptFileName = JobFileAppender.getGlueSrcPath().
                concat(File.separator)
                .concat(String.valueOf(jobId))
                .concat("_")
                .concat(String.valueOf(glueUpdateTime))
                .concat(glueType.getSuffix());
        File scriptFile = new File(scriptFileName);
        if (!scriptFile.exists()) {
            ScriptUtil.markScriptFile(scriptFileName, glueSource);
        }
        String logFileName = JobFileAppender.contextHolder.get();

        ShardingUtil.ShardingVO shardingVO = ShardingUtil.getShardingVo();
        String[] scriptParams = new String[3];
        scriptParams[0] = param;
        scriptParams[1] = String.valueOf(shardingVO.getIndex());
        scriptParams[2] = String.valueOf(shardingVO.getTotal());

        int exitValue = ScriptUtil.execuToFile(cmd, scriptFileName, logFileName, scriptParams);

        if (exitValue == 0) {
            return IJobHandler.SUCCESS;
        } else {
            return new ReturnT<>(IJobHandler.FAIL.getCode(), "script exit value(" + exitValue + ") is failed");
        }

    }
}
