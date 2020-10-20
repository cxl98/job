package com.core.enums;

public enum  ExecutorBlockStrategyEmun {
    SERIAL_EXECUTION("Serial execution"),
    DISCARD_LATED("Discard Later"),
    COVER_EARLY("cOVER early");
    private String title;

    ExecutorBlockStrategyEmun(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public static ExecutorBlockStrategyEmun match(String name,ExecutorBlockStrategyEmun defaultItem){
        if (name != null) {
            for (ExecutorBlockStrategyEmun item: ExecutorBlockStrategyEmun.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }
}
