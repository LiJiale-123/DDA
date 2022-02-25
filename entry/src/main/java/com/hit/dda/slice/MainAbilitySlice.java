package com.hit.dda.slice;

import DDI.DDIS;
import com.hit.dda.AgentServiceAbility;
import com.hit.dda.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Button;
import ohos.agp.components.Text;
import ohos.bundle.ElementName;
import ohos.event.commonevent.*;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.wifi.WifiDevice;
import ohos.wifi.WifiLinkedInfo;

import java.util.Optional;

import static com.hit.dda.AgentServiceAbility.PLAY_STATE;
import static com.hit.dda.AgentServiceAbility.STOP_STATE;

public class MainAbilitySlice extends AbilitySlice {

    private static final String TAG = MainAbilitySlice.class.getSimpleName();
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 0xD001100,TAG);

    private AgentServiceAbility.DDARemoteObject ddaRemoteObject;
    private MyCommonEventSubscriber subscriber;

    private Button startAgent;
    //private Button stopAgent;
    private Button getInfo;
    private Text clientText;
    private Text serverText;
    private static final int DEFAULT_STATE = -1;
    private int lastState = DEFAULT_STATE;

    private final IAbilityConnection connection = new IAbilityConnection() {
        @Override
        public void onAbilityConnectDone(ElementName elementName, IRemoteObject remoteObject, int i) {
            if (remoteObject instanceof AgentServiceAbility.DDARemoteObject) {
                HiLog.info(LABEL_LOG, "onAbilityConnectDone.");
                ddaRemoteObject = (AgentServiceAbility.DDARemoteObject) remoteObject;
            }
        }

        @Override
        public void onAbilityDisconnectDone(ElementName elementName, int i) {
            HiLog.info(LABEL_LOG, "onAbilityDisconnectDone.");
        }
    };

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        HiLog.info(LABEL_LOG, "mainAbilitySlice::onStart");
        initComponents();
        initSubscribeEvent();
        DDIS.DDEnvSet(getAbility());
        //getAbility().getContext();
        startAgentService();
    }

    private void initComponents(){
        startAgent = (Button)findComponentById(ResourceTable.Id_start_button);
        //stopAgent = (Button)findComponentById(ResourceTable.Id_stop_button);
        getInfo = (Button)findComponentById(ResourceTable.Id_connect_button);
        serverText = (Text)findComponentById(ResourceTable.Id_server_text);
        clientText = (Text)findComponentById(ResourceTable.Id_client_text);
        startAgent.setClickedListener(component ->{
            ddaRemoteObject.start();
        });
//        stopAgent.setClickedListener(component ->{
//            ddaRemoteObject.stop();
//
//        });
        getInfo.setClickedListener(component -> {
            serverText.setText("Current device IP:" + System.lineSeparator() + getLocationIpAddress());
            clientText.setText("Current socket port:"+ddaRemoteObject.getMap());
        });
    }

    private void initSubscribeEvent() {
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent("com.hit.dda");
        CommonEventSubscribeInfo subscribeInfo = new CommonEventSubscribeInfo(matchingSkills);
        subscriber = new MyCommonEventSubscriber(subscribeInfo);
        try {
            CommonEventManager.subscribeCommonEvent(subscriber);
        } catch (RemoteException e) {
            HiLog.error(LABEL_LOG, "Exception occurred during subscribeCommonEvent invocation.");
        }
    }

    private void startAgentService()
    {
        HiLog.info(LABEL_LOG, "mainAbilitySlice::startAgentService");
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName("com.hit.dda")
                .withAbilityName("com.hit.dda.AgentServiceAbility")
                .build();
        intent.setOperation(operation);
        startAbility(intent);
        connectAbility(intent, connection);
    }

    private String getLocationIpAddress() {
        WifiDevice wifiDevice = WifiDevice.getInstance(this);
        Optional<WifiLinkedInfo> linkedInfo = wifiDevice.getLinkedInfo();
        int ip = linkedInfo.get().getIpAddress();
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);
    }
    @Override
    public void onActive() {
        HiLog.info(LABEL_LOG, "mainAbilitySlice::onActive");
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        HiLog.info(LABEL_LOG, "mainAbilitySlice::onForeground");
        super.onForeground(intent);
    }

    @Override
    protected void onStop() {
        HiLog.info(LABEL_LOG, "mainAbilitySlice::onStop");
        super.onStop();
        try {
            CommonEventManager.unsubscribeCommonEvent(subscriber);
        } catch (RemoteException e) {
            HiLog.error(LABEL_LOG, "Exception occurred during unsubscribeCommonEvent invocation.");
        }
        disconnectAbility(connection);
    }

    private void setState(int state) {
        switch (state) {
            case PLAY_STATE: {
                handleButtonState(PLAY_STATE);
                startAgent.setText(ResourceTable.String_start_over);
                //stopAgent.setText(ResourceTable.String_stop);
                break;
            }
            case STOP_STATE: {
                handleButtonState(STOP_STATE);
                //stopAgent.setText(ResourceTable.String_stop_over);
                startAgent.setText(ResourceTable.String_start);
                break;
            }
            default:
        }
        lastState = state;
    }

    private void handleButtonState(int status) {
        if ( status != STOP_STATE) {
            startAgent.setEnabled(false);
            //stopAgent.setEnabled(true);
        } else {
            startAgent.setEnabled(true);
            //stopAgent.setEnabled(false);
        }
    }


    class MyCommonEventSubscriber extends CommonEventSubscriber {
        MyCommonEventSubscriber(CommonEventSubscribeInfo info) {
            super(info);
        }

        @Override
        public void onReceiveEvent(CommonEventData commonEventData) {
            Intent intent = commonEventData.getIntent();
            int state = intent.getIntParam("state", DEFAULT_STATE);
            if (state != DEFAULT_STATE) {
                setState(state);
            }
        }
    }
}
