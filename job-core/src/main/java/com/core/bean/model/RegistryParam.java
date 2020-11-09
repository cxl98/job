package com.core.bean.model;

import lombok.Data;

import java.io.Serializable;
@Data
public class RegistryParam implements Serializable {
    private static final long serialVersionUID=420L;

    private String registryGroup;
    private String registryKey;
    private String registryValue;

    public RegistryParam() {
    }

    public RegistryParam(String registryGroup, String registryKey, String registryValue) {
        this.registryGroup = registryGroup;
        this.registryKey = registryKey;
        this.registryValue = registryValue;
    }



    @Override
    public String toString() {
        return "RegistryParam{" +
                "registryGroup='" + registryGroup + '\'' +
                ", registryKey='" + registryKey + '\'' +
                ", registryValue='" + registryValue + '\'' +
                '}';
    }
}
