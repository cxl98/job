package com.core.enums;

public class RegistryConfig {

    public static final int BEAT_TIMEOUT=30;
    public static final int DEAD_TIMEOUT=BEAT_TIMEOUT*2;
    public enum RegistryType{EXECUTOR,ADMIN}
}
