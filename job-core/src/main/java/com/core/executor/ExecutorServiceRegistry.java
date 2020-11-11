package com.core.executor;

import com.core.thread.ExecutorRegistryThread;
import com.cxl.rpc.registry.ServiceRegistry;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ExecutorServiceRegistry extends ServiceRegistry {
    @Override
    public void start(Map<String, String> map) {
        //start registry
        ExecutorRegistryThread.getInstance().start(map.get("appName"),map.get("address"));
    }

    @Override
    public void stop() {
        //stop registry
        ExecutorRegistryThread.getInstance().toStop();
    }

    @Override
    public boolean registry(Set<String> set, String s) {
        return false;
    }

    @Override
    public boolean remove(Set<String> set, String s) {
        return false;
    }

    @Override
    public Map<String, TreeSet<String>> discovery(Set<String> set) {
        return null;
    }

    @Override
    public TreeSet<String> discovery(String s) {
        return null;
    }
}
