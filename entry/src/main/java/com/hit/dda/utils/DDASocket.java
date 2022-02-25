package com.hit.dda.utils;

import DDI.DDIS;
import ohos.aafwk.ability.Ability;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class DDASocket {
    private static final String TAG = DDASocket.class.getSimpleName();
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 0xD001100,TAG);
    private static final int QUEUE_MAX = 50;
    //private static final String END = "55AA";
    //private static final String CONNECT = "create socket ";
    private static final int TIME_OUT = 5000;
    private ServerSocket serverSocket;
    private Map<Socket, BlockingQueue<String>> map = new ConcurrentHashMap<>();
    private volatile int PORT = -1;
    private Context context;
    //private volatile Transfer transfer;

    public DDASocket() {
        try {
            this.serverSocket = new ServerSocket(0);
            this.PORT = serverSocket.getLocalPort();
            HiLog.info(LABEL_LOG,"监听socket建立，port: %{public}d",PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public DDASocket(int port) {
        try {
            if(serverSocket ==null){
                this.serverSocket = new ServerSocket(port);
                this.PORT=port;
            }
            HiLog.info(LABEL_LOG,"监听socket建立,port: %{public}d",PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DDASocket(int port, Context context) {
        try {
            if(serverSocket ==null){
                this.serverSocket = new ServerSocket(port);
                this.PORT=port;
                this.context=context;
            }
            HiLog.info(LABEL_LOG,"监听socket建立,port: %{public}d",PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startSocket(){
        HiLog.info(LABEL_LOG, "DDASocket::startSocket");
        HiLog.info(LABEL_LOG, "DDASocket保持监听");
        while(!Thread.interrupted()){
            HiLog.info(LABEL_LOG, "serverSocket.isClosed(): %{public}s",serverSocket.isClosed());
            Socket socket;
            try {
                socket = serverSocket.accept();
                ThreadPoolUtil.submit(new HeartBeat(socket));
                ThreadPoolUtil.submit(new handleSocket(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        HiLog.info(LABEL_LOG, "serverSocket已断开连接");
    }

    public void stopSocket(){
        if(!serverSocket.isClosed()){
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                serverSocket =null;
            }

        }
    }

    public int getPort() {
        return PORT;
    }

    public String getMap (){
        return map.toString();
    }

    class handleSocket implements Runnable {

        private final Socket socket;

        public handleSocket(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BlockingQueue<String> queue = new ArrayBlockingQueue<>(QUEUE_MAX);
                map.put(socket,queue);
                HiLog.info(LABEL_LOG, "%{public}s", "客户端:" + socket.getRemoteSocketAddress() +"已连接到服务器,开始握手");
                boolean suc=ddiagent.connectTest(socket,TIME_OUT);
                //DDIS.DDConnectTest(socket,TIME_OUT);
                if(suc==true)
                    HiLog.info(LABEL_LOG, "握手成功");
                else
                    HiLog.info(LABEL_LOG, "握手失败");
                while(!socket.isClosed()){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    String string;
                    while((string = reader.readLine())!=null) {		//接收消息
                        HiLog.info(LABEL_LOG, "收到命令%{public}s",string);
                        map.get(socket).put(string);
                        String ret=ddiagent.onCommend(string,context);
                        if(string.equals("END")){
                            socket.close();
                            break;
                        }
                        writer.write("receive commend "+string+" success, and result: "+ret);
                        writer.newLine();
                        writer.flush();
                    }
                }
                socket.close();
                HiLog.info(LABEL_LOG, "socket关闭");
                HiLog.info(LABEL_LOG, "socket 客户端 %{public}s 已断开连接",socket.getRemoteSocketAddress());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
