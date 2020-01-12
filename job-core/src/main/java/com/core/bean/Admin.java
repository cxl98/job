package com.core.bean;

import com.core.bean.model.HandleCallbackParam;
import com.core.bean.model.RegistryParam;
import com.core.bean.model.ReturnT;

import java.util.List;

public interface Admin {
    String MAPPING = "/api";

    ReturnT<String> callback(List<HandleCallbackParam> callbackParamList);

    ReturnT<String> registry(RegistryParam registryParam);

    ReturnT<String> registryRemove(RegistryParam registryParam);
}
