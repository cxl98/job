package com.core.handler.impl;

import com.core.bean.model.ReturnT;
import com.core.handler.IJobHandler;

public class GlueJobHandler extends IJobHandler {
    private long glueUpdateTime;
    private IJobHandler jobHandler;

    public GlueJobHandler( IJobHandler jobHandler,long glueUpdateTime) {
        this.glueUpdateTime = glueUpdateTime;
        this.jobHandler = jobHandler;
    }

    public long getGlueUpdateTime() {
        return glueUpdateTime;
    }

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        return jobHandler.execute(param);
    }
}
