package com.hit.dda.service;

import com.hit.dda.utils.DDASocket;
import ohos.aafwk.ability.RunningProcessInfo;
import ohos.aafwk.ability.SystemMemoryInfo;
import ohos.app.Context;
import ohos.app.IAbilityManager;
import ohos.app.ProcessInfo;
import ohos.bundle.AbilityInfo;
import ohos.bundle.BundleInfo;
import ohos.bundle.IBundleManager;
import ohos.global.icu.text.DecimalFormat;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.os.ProcessManager;
import ohos.rpc.RemoteException;

import java.util.ArrayList;
import java.util.List;

/**
 * @className : MySystemInfo
 * @description :
 * @author :
 * @date : Created in 2022/2/26 13:09
 * @version: : 1.0
 */
public class MySystemInfo {
    private static final String TAG = DDASocket.class.getSimpleName();
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 0xD001100,TAG);

    private final Context context;
    private final IBundleManager bundleManager;
    private final IAbilityManager iAbilityManager;
    private final SystemMemoryInfo systemMemoryInfo;
    private List<ProcessInfo> processInfoList;

    public MySystemInfo(Context context) {
        this.context = context;
        this.bundleManager = context.getBundleManager();
        this.iAbilityManager = context.getAbilityManager();
        this.systemMemoryInfo = new SystemMemoryInfo();
        iAbilityManager.getSystemMemoryInfo(systemMemoryInfo);
    }


    //获取系统总内存
    public String getSystemTotalMem(){
        String result = transferSize(systemMemoryInfo.getTotalSysMem());
        HiLog.info(LABEL_LOG,"获取系统总内存为%{public}s",result);
        return result;
    }
    //获取系统可使用内存
    public String getSysAvailMem(){
        String result = transferSize(systemMemoryInfo.getAvailSysMem());
        HiLog.info(LABEL_LOG,"获取系统可使用内存为%{public}s",result);
        return result;
    }

    public List<RunningProcessInfo> getProcessList(){
        return iAbilityManager.getAllRunningProcesses();
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

    public List<AbilityInfo> getAbilityInfo(String BundleName) {
        List<AbilityInfo> abilityInfos = new ArrayList<>();
        if(isAPPInstall(BundleName)){
            HiLog.info(LABEL_LOG,"%{public}s 应用已安装",BundleName);
            try {
                BundleInfo bundleInfo = bundleManager.getBundleInfo(BundleName,1);
                HiLog.info(LABEL_LOG,"应用AppId : %{public}s ",bundleInfo.getAppId());
                HiLog.info(LABEL_LOG,"应用uid : %{public}s ",bundleInfo.getUid());
                HiLog.info(LABEL_LOG,"应用AbilityInfos() : %{public}s ",bundleInfo.getAbilityInfos());
                abilityInfos = bundleInfo.getAbilityInfos();
                HiLog.info(LABEL_LOG,"应用abilityInfo个数: %{public}s ",abilityInfos.size());
                for(int i= 0;i<abilityInfos.size();i++){
                    AbilityInfo abilityInfo = abilityInfos.get(i);
                    int PID = ProcessManager.getPid();
                    HiLog.info(LABEL_LOG,"应用ProcessManager.getPid: %{public}s ",PID);
                    HiLog.info(LABEL_LOG,"应用ProcessManager.getTid();: %{public}s ",ProcessManager.getTid());
                    HiLog.info(LABEL_LOG,"应用abilityInfo: %{public}s ",abilityInfo.toString());
                    HiLog.info(LABEL_LOG,"应用abilityInfo.getProcess: %{public}s ",abilityInfo.getProcess());
                }
                int uid= bundleInfo.getUid();
                HiLog.info(LABEL_LOG,"应用bundleInfo.getUid() : %{public}s ",uid);
                String cpuAbi = bundleInfo.getCpuAbi();
                HiLog.info(LABEL_LOG,"应用cpuAbi : %{public}s ",cpuAbi);
                String process = bundleInfo.getAppInfo().getProcess();
                HiLog.info(LABEL_LOG,"应用process : %{public}s ",process);
                HiLog.info(LABEL_LOG,"应用bundleInfo.getUid() : %{public}s ",bundleInfo.getUid());
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }else{
            HiLog.info(LABEL_LOG,"%{public}s 应用未安装",BundleName);
        }

        return abilityInfos;
    }
    private boolean isAPPInstall(String bundleName) {
        try {
            bundleManager.isApplicationEnabled(bundleName);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
    public static String transferSize(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        }
        else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        }
        else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        }
        else if (size < 1024) {
            if (size <= 0) {
                bytes.append("0B");
            }
            else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }
}
