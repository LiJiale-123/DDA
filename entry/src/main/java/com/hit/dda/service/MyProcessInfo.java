package com.hit.dda.service;

import ohos.aafwk.ability.RunningProcessInfo;

public class MyProcessInfo {

    private final RunningProcessInfo runningProcessInfo;

    public MyProcessInfo(RunningProcessInfo runningProcessInfo) {
        this.runningProcessInfo = runningProcessInfo;
    }

    public int getPid() {
        return runningProcessInfo.getPid();
    }

    public String getProcessName() {
        return runningProcessInfo.getProcessName();
    }

    public int getUid() {
        return runningProcessInfo.getUid();
    }

    public int getMemSize() {
        return runningProcessInfo.getLastMemoryLevel();
    }
}
