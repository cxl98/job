package com.core.bean.model;

import lombok.Data;

import java.io.Serializable;
@Data
public class HandleCallbackParam implements Serializable {
    private static final long serialVersionUID=420L;
    private long logId;
    private long logDataTim;

    private ReturnT<String> executeResult;

    public HandleCallbackParam() {
    }

    public HandleCallbackParam(long logId, long logDataTim, ReturnT<String> executeResult) {
        this.logId = logId;
        this.logDataTim = logDataTim;
        this.executeResult = executeResult;
    }

    @Override
    public String toString() {
        return "HandleCallbackParam{" +
                "logId=" + logId +
                ", logDataTim=" + logDataTim +
                ", executeResult=" + executeResult +
                '}';
    }
}
