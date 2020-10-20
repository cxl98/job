package com.core.enums;

public enum GlueTypeEnum {
    GULE_JAVA("GULE(JAVA)", false, null, null),
    GULE_SHELL("GLUE(Shell)", false, "bash", ".sh"),
    GLUE_PYTHON("GLUE(Python)", true, "python", ".py");

    private String desc;
    private boolean isScript;
    private String cmd;
    private String suffix;//后缀

    GlueTypeEnum(String desc, boolean isScript, String cmd, String suffix) {
        this.desc = desc;
        this.isScript = isScript;
        this.cmd = cmd;
        this.suffix = suffix;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isScript() {
        return isScript;
    }

    public void setScript(boolean script) {
        isScript = script;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public static GlueTypeEnum match(String name) {
        for (GlueTypeEnum item : GlueTypeEnum.values()) {
            if (item.equals(name)) {
                return item;
            }
        }
        return null;
    }
}
