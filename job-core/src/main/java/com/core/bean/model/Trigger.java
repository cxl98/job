package com.core.bean.model;

import lombok.Data;

import java.io.Serializable;
@Data
public class Trigger  implements Serializable {
    private static final long serialVersionUID=420L;

    private int jobId;

    private String executorHandler;
    private String executorParams;
    private String executorBlockStrategy;
    private int executorTimeout;

    private long logId;
    private long logDateTime;

    private String glueType;
    private String glueSource;
    private long glueUpdateTime;

    private int broadcastIndex;
    private int broadcastTotal;

    public Trigger() {
    }
    @Override
    public String toString() {
        return "Trigger{" +
                "executorHandler='" + executorHandler + '\'' +
                ", executorParams='" + executorParams + '\'' +
                ", executorBlockStrategy='" + executorBlockStrategy + '\'' +
                ", executorTimeout=" + executorTimeout +
                ", logId=" + logId +
                ", glueType='" + glueType + '\'' +
                ", glueSource='" + glueSource + '\'' +
                ", glueUpdatetime=" + glueUpdateTime +
                ", broadcastIndex=" + broadcastIndex +
                ", broadcastTotal=" + broadcastTotal +
                '}';
    }
}
