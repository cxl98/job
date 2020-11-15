package com.core.handler;

import com.core.bean.model.ReturnT;

public abstract class IJobHandler {
    /**
     * success
     */
    public static final ReturnT<String> SUCCESS=new ReturnT<>(200,null);

    /**
     * fail
     */
    public static final ReturnT<String> FAIL=new ReturnT<>(500,null);

    /**
     * fail timeout
     */
    public static final ReturnT<String> FAIL_TIMEOUT=new ReturnT<>(502,null);

    /**
     * executor handler ,invoked when executor receives a scheduling request
     * @param param
     * @return
     * @throws Exception
     */
    public abstract ReturnT<String> execute(String param)throws Exception;

    /**
     * init handler ,invoker when JobThread init
     */
    public void init(){
    }

    /**
     * init handler ,invoker when JobThread destroy
     */
    public void destroy(){}
}
