package com.hit.dda.service;
/**
 * @ClassName : MySystemInfoMangerment
 * @Description :
 * @Author :
 * @Date : Created in 2022/2/26 18:54
 * @Version: : 1.0
 */
import ohos.aafwk.ability.RunningProcessInfo;
import ohos.app.Context;
import ohos.app.IAbilityManager;
import ohos.bundle.IBundleManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

import java.util.List;

public class MySystemInfoMangerment {
    private static final String TAG = MySystemInfoMangerment.class.getSimpleName();
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 0xD001100,TAG);

    private final Context remoteContext;
    private final IBundleManager bundleManager;
    private final IAbilityManager iAbilityManager;
    private String handleName;

    public MySystemInfoMangerment(Context context,String handleName) {
        this.remoteContext = context.createBundleContext(handleName,Context.CONTEXT_IGNORE_SECURITY|Context.CONTEXT_INCLUDE_CODE);
        HiLog.info(LABEL_LOG," MySystemInfoMangerment %{public}s ",remoteContext.getApplicationInfo());
        this.bundleManager = remoteContext.getBundleManager();
        this.iAbilityManager = remoteContext.getAbilityManager();
        this.handleName = handleName;
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
    public RunningProcessInfo getProcessByName(){
        List<RunningProcessInfo> runningProcessInfos =getProcessList();
        HiLog.info(LABEL_LOG,"当前系统进程共有: %{public}s 个进程",runningProcessInfos.size());
        RunningProcessInfo processInfo = null;
        for(RunningProcessInfo runningProcessInfo:runningProcessInfos){
            if(runningProcessInfo.getProcessName().equals(handleName)){
                processInfo = runningProcessInfo;
            }else{
                continue;
            }
        }
        return processInfo;
    }

    public int getPidByName(){
        RunningProcessInfo processInfo = getProcessByName();
        if(processInfo!=null){
            return processInfo.getPid();
        }else{
            return -1;
        }
    }
}
