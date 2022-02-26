package com.hit.dda.service;

import ohos.aafwk.ability.RunningProcessInfo;
import ohos.aafwk.ability.SystemMemoryInfo;
import ohos.app.Context;
import ohos.app.IAbilityManager;
import ohos.app.ProcessInfo;
import ohos.bundle.IBundleManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

import java.util.List;

/**
 * @ClassName : MySystemInfoMangerment
 * @Description :
 * @Author :
 * @Date : Created in 2022/2/26 18:54
 * @Version: : 1.0
 */
public class MySystemInfoMangerment {
    private static final String TAG = MySystemInfoMangerment.class.getSimpleName();
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 0xD001100,TAG);

    private final Context remoteContext;
    private final IBundleManager bundleManager;
    private final IAbilityManager iAbilityManager;

    public MySystemInfoMangerment(Context context,String handleName) {
        this.remoteContext = context.createBundleContext(handleName,Context.CONTEXT_IGNORE_SECURITY|Context.CONTEXT_INCLUDE_CODE);
        this.bundleManager = remoteContext.getBundleManager();
        this.iAbilityManager = remoteContext.getAbilityManager();
    }

    private List<RunningProcessInfo> getProcessList(){
        return iAbilityManager.getAllRunningProcesses();
    }
    private boolean isAPPInstall(String bundleName) {
        try {
            bundleManager.isApplicationEnabled(bundleName);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public void getInfo(){
        if(remoteContext!=null){

        }
    }
    public RunningProcessInfo getProcessByName(String name){
        List<RunningProcessInfo> runningProcessInfos =getProcessList();
        HiLog.info(LABEL_LOG,"当前系统进程共有: %{public}s 个进程",runningProcessInfos.size());
        RunningProcessInfo processInfo = null;
        for(RunningProcessInfo runningProcessInfo:runningProcessInfos){
            if(runningProcessInfo.getProcessName().equals(name)){
                processInfo = runningProcessInfo;
            }else{
                continue;
            }
        }
        return processInfo;
    }

    public int getPidByName(String name){
        RunningProcessInfo processInfo = getProcessByName(name);
        if(processInfo!=null){
            return processInfo.getPid();
        }else{
            return -1;
        }
    }
}
