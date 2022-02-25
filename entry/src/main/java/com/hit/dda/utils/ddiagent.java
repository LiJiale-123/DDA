package com.hit.dda.utils;

import java.net.Socket;
import java.util.HashMap;

import DDI.DDIS;
import ohos.aafwk.ability.Ability;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

import static java.security.AccessController.getContext;

public class ddiagent {
    private static final String TAG = ddiagent.class.getSimpleName();
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 0xD001100,TAG);


    /**
     * 测试分布式调试所需的设备连接
     * @param socket 已连接的socket
     * @param timeout 设定的超时时间。
     * @return boolean为true时，表明连接畅通,否则连接失败。
     */
    static boolean connectTest(Socket socket, long timeout){
        HashMap<String,String> result=DDIS.DDConnectTest(socket,timeout);
        if(result.get("msg").isEmpty()||result.get("code").equals("0"))//结果正确
            return true;
        else
            return false;
    }

    /**
     * 调用DDP将getInfo的hashMap形式的输出转化为字符串形式，方便用socket发送
     * @return 包含deviceInfo信息的字符串。
     */
    static String GetDeviceInfoToString(Context context){
        //TODO
        //HiLog.info(LABEL_LOG,"看看能不能执行到这一行1");
        HashMap<String,String> a=DDIS.GetDeviceInfo(context);
        //HiLog.info(LABEL_LOG,"看看能不能执行到这一行2");
        HiLog.info(LABEL_LOG,"GetDeviceInfoToString： "+a.toString());
        return a.toString();
    }

    /**
     * dda获取到命令后的执行函数，通过switch判断命令类型,并处理和返回结果
     * @param Commend 命令的字符串形式。如果设计好DDP应该是由ddp解析函数返回的字符串，如果没有的话先这么设计。
     * @return 一些命令会调用函数并返回结果。
     * 这些结果需要转化为字符串装进报文通过socket回传给ddj。
     * 将数据类型转化成合适格式的字符串的工作应该由ddp完成。
     * 如果没有ddp可以我们定一个格式
     */
    static String onCommend(String Commend, Context context){
        String result="NoError";//如果结果返回NoError，说明执行的是void类型的ddi接口函数，且没有抛出异常。
        String Commendtype=ddpGettype(Commend);
        try {
            switch (Commendtype) {
                case "DDGetTime":
                    return DDIS.DDGetTime();
                case "GetDeviceInfo":
                    return GetDeviceInfoToString(context);
                /*
                    ...
                    新的命令类型和处理方法，在此处补充
                 */
            }
        }catch(Exception e){
            e.printStackTrace();
            return "Exception"+e.toString();
        }
        return result;
    }

    /**
     * 输入完整的Commed字符串，返回Commend的类型。以后这里用DDP的解析函数进行修改。
     * @param Commend 这里是完整的Commend字符串指令
     * @return 这里返回Commend类型
     */
    private static String ddpGettype(String Commend){
        String[] words=Commend.split(" ");
        return words[0];
    }
}
