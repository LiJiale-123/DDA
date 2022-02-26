package com.hit.dda;

import com.hit.dda.utils.DDASocket;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.LocalRemoteObject;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.utils.Color;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.notification.NotificationHelper;
import ohos.event.notification.NotificationRequest;
import ohos.event.notification.NotificationSlot;
import ohos.rpc.IRemoteObject;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public class AgentServiceAbility extends Ability {
    //按钮状态，PLAY_STATE表示AgentService已启动；STOP_STATE表示AgentService已关闭
    public static final int PLAY_STATE = 0;
    public static final int STOP_STATE = 1;
    private static final String HANDLE_NAME = "com.hit.dda";

    private static final String TAG = AgentServiceAbility.class.getSimpleName();
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 0xD001100,TAG);

    //NotificationSlot的ID-使用前台Service，始终保持正在运行的图标在系统状态栏显示。
    private static final int NOTIFICATION_ID = 1005;
    //监听端口默认7000
    private static final int SOCKET_PORT = 7000;
    //当前AgentServiceAbility的状态
    private int state = STOP_STATE;

    private DDASocket ddaSocket;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        HiLog.info(LABEL_LOG, "AgentServiceAbility::onStart");
        new Thread(() -> {
            state = PLAY_STATE;
            ddaSocket = new DDASocket(SOCKET_PORT,this);
            ddaSocket.startSocket();
        }).start();
        sendNotification(Integer.toString(SOCKET_PORT));
        sendEvent();
    }


    @Override
    public void onCommand(Intent intent, boolean restart, int startId) {
        super.onCommand(intent, restart, startId);

        HiLog.info(LABEL_LOG, "ability连接到AgentService");
        state = PLAY_STATE;
        sendEvent();
    }

    @Override
    public void onBackground() {
        super.onBackground();
        HiLog.info(LABEL_LOG, "AgentServiceAbility::onBackground");
    }

    @Override
    public void onStop()
    {
        super.onStop();
        HiLog.info(LABEL_LOG, "AgentServiceAbility::onStop");
        cancelNotification();
        sendEvent();
    }
    @Override
    public IRemoteObject onConnect(Intent intent) {
        HiLog.info(LABEL_LOG, "AgentServiceAbility::onConnect  应用连接到AgentService");
        return new DDARemoteObject(this);
    }

    @Override
    public void onDisconnect(Intent intent) {
        HiLog.info(LABEL_LOG, "AgentServiceAbility::onDisconnect");
        super.onDisconnect(intent);
    }

    /**
     * 发送事件调整start按钮状态
     */
    private void sendEvent() {
        HiLog.info(LABEL_LOG, "sendEvent::onStart");
        try {
            Intent intent = new Intent();
            Operation operation = new Intent.OperationBuilder()
                    .withAction(HANDLE_NAME)
                    .build();
            intent.setOperation(operation);
            HiLog.info(LABEL_LOG, "sendEvent:"+state);
            intent.setParam("state", state);
            CommonEventData eventData = new CommonEventData(intent);
            CommonEventManager.publishCommonEvent(eventData);
        } catch (RemoteException e) {
            HiLog.error(LABEL_LOG, "Exception occurred during publishCommonEvent invocation.");
        }
    }

    /**
     * 发送通知提示socket在持续监听
     * @param str socket监听端口
     */
    private void sendNotification(String str) {
        HiLog.info(LABEL_LOG, "sendNotification");
        String slotId = "foregroundServiceId";
        String slotName = "foregroundServiceName";
        NotificationSlot slot = new NotificationSlot(slotId, slotName, NotificationSlot.LEVEL_HIGH);
        slot.setDescription("NotificationSlot Description");
        slot.setEnableVibration(true);
        slot.setLockscreenVisibleness(NotificationRequest.VISIBLENESS_TYPE_PUBLIC);
        slot.setEnableLight(true);
        slot.setLedLightColor(Color.RED.getValue());
        try {
            NotificationHelper.addNotificationSlot(slot);
        } catch (RemoteException ex) {
            HiLog.error(LABEL_LOG, "Exception occurred during addNotificationSlot invocation.");
        }

        NotificationRequest request = new NotificationRequest(NOTIFICATION_ID);
        request.setSlotId(slot.getId());
        String title = "AgentService is running";
        String text = "The Service is Running"+str;
        NotificationRequest.NotificationNormalContent content = new NotificationRequest.NotificationNormalContent();
        content.setTitle(title)
                .setText(text);
        NotificationRequest.NotificationContent notificationContent =
                new NotificationRequest.NotificationContent(content);
        request.setContent(notificationContent);
        keepBackgroundRunning(NOTIFICATION_ID, request);
    }

    /**
     * 取消监听的通知
     */
    private void cancelNotification() {
        cancelBackgroundRunning();
    }

    /**
     * Start DDA
     */
    public void startAgent() {
        if (state != STOP_STATE) {
            return;
        }
        HiLog.info(LABEL_LOG, "开启startAgent");
        new Thread(() -> {
            DDASocket ddaSocket = new DDASocket(SOCKET_PORT,getContext());
            ddaSocket.startSocket();
        }).start();
        state = PLAY_STATE;
        sendNotification(Integer.toString(SOCKET_PORT));
        sendEvent();
    }

    public void getInfo() {
        if (state != PLAY_STATE) {
            return;
        }
        HiLog.info(LABEL_LOG, "getInfo");
        //todo
    }

    public void stopAgent() {
        if (state == STOP_STATE) {
            return;
        }
        HiLog.info(LABEL_LOG, "开启stopAgent");
//        ddaSocket.stopSocket();
//        ddaSocket = null;
        state = STOP_STATE;
        cancelNotification();
        sendEvent();
    }

    /**
     * 获取取每个连接的socket，和该socket发送的命令
     * @return String 返回每个连接的socket，和该socket发送的命令
     */
    public String getMap(){
        return ddaSocket.getMap();
    }

    /**
     * LocalRemoteObject Implementation
     */
    public static class DDARemoteObject extends LocalRemoteObject {
        private final AgentServiceAbility agentService;

        DDARemoteObject(AgentServiceAbility agentService) {
            this.agentService = agentService;
        }

        public void start() {
            agentService.startAgent();
        }

        public String getMap(){
            return agentService.getMap();
        }

        public void stop() {
            agentService.stopAgent();
        }
    }
}