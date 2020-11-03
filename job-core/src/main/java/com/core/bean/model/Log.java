package com.core.bean.model;

import lombok.Data;

import java.io.Serializable;
@Data
public class Log implements Serializable {
    private static final long serialVersionUID = 420L;
    private int fromLineNum;
    private int toLineNum;
    private String logContent;
    private boolean isEnd;

    public Log(int fromLineNum, int toLineNum, String logContent, boolean isEnd) {
        this.fromLineNum = fromLineNum;
        this.toLineNum = toLineNum;
        this.logContent = logContent;
        this.isEnd = isEnd;
    }

}
