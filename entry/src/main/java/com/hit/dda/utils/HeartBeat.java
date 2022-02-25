package com.hit.dda.utils;

import ohos.agp.window.dialog.CommonDialog;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

public class HeartBeat implements Runnable{
	private static final String TAG = HeartBeat.class.getSimpleName();
	private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 0xD001100,TAG);
	Socket socket = null;
	HeartBeat(Socket s){
		this.socket=s;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int a=0;
		try {
			socket.setKeepAlive(true);
			socket.setSoTimeout(50);
			int w=0;
			a=0;
			 while (true) {
				 	a=1;
					/*
				 	socket.sendUrgentData(0xFF);*/
				 	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				 	//writer.newLine();

					 writer.flush();
					 writer.write("$");
					 writer.newLine();
				 	writer.flush();
				 	a=0;
				 	//HiLog.info(LABEL_LOG,String.valueOf(++w));
	                Thread.sleep(1000);

	            }
		} catch (SocketException e) {
			if(socket.isClosed())
				HiLog.info(LABEL_LOG,"本机socket已经关闭");
			else if(a==1)
				HiLog.info(LABEL_LOG,"远端socket连接已经断开,请重启app，并重新尝试用DDJ连接");
				HiLog.info(LABEL_LOG,e.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
