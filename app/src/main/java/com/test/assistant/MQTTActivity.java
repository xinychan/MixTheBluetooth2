package com.test.assistant;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.test.assistant.baseUse.DefaultNavigationBar;
import com.test.assistant.mqtt.IMQTTCallBack;
import com.test.assistant.mqtt.MQTTMessageService;
import com.test.assistant.mqtt.ServiceData;
import com.test.assistant.mqtt.fragments.FragmentReleaseTheme;
import com.test.assistant.mqtt.fragments.FragmentSubscribeTopic;
import com.test.assistant.mqtt.fragments.MQTTFragmentManage;
import com.test.assistant.mqtt.fragments.tool.MessageInterface;
import com.test.assistant.mqtt.fragments.tool.TopicBox;
import com.test.assistant.view.CustomButtonView;
import com.test.assistant.view.UnderlineTextView;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;

public class MQTTActivity extends AppCompatActivity {

    public static final int SUBSCRIBE_TOPIC = 0x00;//订阅
    public static final int UNSUBSCRIBE_TOPIC = 0x01;//取消订阅
    public static final int RELEASE_THEME = 0x02;//发布主题消息

    @ViewById(R.id.mqtt_service_switch)
    private CustomButtonView mSwitchView;

    @ViewById(R.id.send)
    private UnderlineTextView mSendTopic;

    @ViewById(R.id.read)
    private UnderlineTextView mReadTopic;

    private MQTTMessageService.DownloadBinder mBinder;

    private IMQTTCallBack mCallBack;

    private ServiceData mServiceData;

    private ServiceConnection mConnection;

    private MQTTFragmentManage mFragmentManage;

    private MessageInterface mReleaseThemeListener, mSubscribeTopicListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);
        ViewUtils.inject(this);
        init();
        initTitle();
        subscribeTopic(0);
        mSendTopic.setState();
    }


    private Handler mMessageHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (mSwitchView.getState() != CustomButtonView.State.Open) {
                Toast.makeText(MQTTActivity.this, "请先连接，再操作", Toast.LENGTH_SHORT).show();
                if (msg.what == MQTTActivity.SUBSCRIBE_TOPIC)
                    mSubscribeTopicListener.subscribeFailure();
                return false;
            }
            switch (msg.what) {

                case SUBSCRIBE_TOPIC:
                    String topic = msg.obj.toString();
                    if (mBinder != null)
                        mBinder.subscribeTopic(topic, 0);
                    break;

                case RELEASE_THEME:
                    TopicBox topicBox = (TopicBox) msg.obj;
                    sendData(topicBox.getTopic(), topicBox.getMessage(), topicBox.getQoS(), topicBox.isRetain());
                    break;

                case UNSUBSCRIBE_TOPIC:
                    if (mBinder != null)
                        mBinder.unSubscribeTopic(msg.obj.toString());
                    break;
            }
            return false;
        }
    });


    @OnClick({R.id.send, R.id.read})
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.send:
                releaseTheme(view.getId());
                break;
            case R.id.read:
                subscribeTopic(view.getId());
                break;
        }
    }

    @OnClick(R.id.mqtt_service_switch)
    private void setService(View view) {
        CustomButtonView customButtonView = (CustomButtonView) view;
        switch (customButtonView.getState()) {
            case Open://处于打开状态，所以应当关闭
                customButtonView.closed();
                mBinder.stop();
                unbindService(mConnection);
                break;
            case Run:
                Toast.makeText(this, "开启中，请稍等..", Toast.LENGTH_SHORT).show();
                break;
            case Close:
                customButtonView.start();
                Intent bindIntent = new Intent(this, MQTTMessageService.class);
                bindService(bindIntent, mConnection, BIND_AUTO_CREATE);
                break;
        }
    }

    //发布主题
    private void releaseTheme(int id) {
        FragmentReleaseTheme theme = null;
        if (mReleaseThemeListener == null) {
            theme = new FragmentReleaseTheme();
            mReleaseThemeListener = theme;
            mReleaseThemeListener.setHandler(mMessageHandler);
        }
        if (mFragmentManage.getFragmentId() == id) {
            return;
        }
        mSendTopic.setState();
        mReadTopic.setState();
        mFragmentManage.initFragment(R.id.send, theme,
                getSupportFragmentManager().beginTransaction());
    }

    //订阅主题
    private void subscribeTopic(int id) {
        FragmentSubscribeTopic topic = null;
        if (mSubscribeTopicListener == null) {
            topic = new FragmentSubscribeTopic();
            mSubscribeTopicListener = topic;
            mSubscribeTopicListener.setHandler(mMessageHandler);
        }
        if (mFragmentManage.getFragmentId() == id) {
            return;
        }
        mSendTopic.setState();
        mReadTopic.setState();
        mFragmentManage.initFragment(R.id.read, topic,
                getSupportFragmentManager().beginTransaction());
    }

    private void sendData(String topic, String data, int QoS, boolean isRetain) {
        if (mBinder != null)
            mBinder.setMessage(topic, data, QoS, isRetain);
        else
            Toast.makeText(this, "请先连接服务再发送数据", Toast.LENGTH_SHORT).show();
    }

    private void init() {

        mFragmentManage = new MQTTFragmentManage(R.id.mqtt_fragment);

        mCallBack = new IMQTTCallBack() {
            @Override
            public void connectError(String error) {
                log("连接错误：" + error);
                Toast.makeText(MQTTActivity.this, "错误：连接不上，请稍后试试...", Toast.LENGTH_SHORT).show();
                mSwitchView.closed();
                mBinder.stop();
                try {
                    unbindService(mConnection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void connectService(String param1String) {
                log("连接成功：" + param1String);
                Toast.makeText(MQTTActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                mSwitchView.staysOn();
            }

            @Override
            public void connectServiceDel(String error) {
                log("连接断开: " + error);
                mSwitchView.closed();
                if (!mServiceData.isReconnection()) {//在非断线重连的情况下，直接关闭服务
                    mBinder.stop();
                    unbindService(mConnection);
                }
            }

            @Override
            public void readBackMsg(final String message, final String subtopic, final String qos) {
                log("主题: " + subtopic + " ,消息: " + message + " ,质量: " + qos);
                if (mSubscribeTopicListener != null) {
                    mSubscribeTopicListener.setTopicMessage(new TopicBox(subtopic, message, Integer.parseInt(qos)));
                }
            }

            @Override
            public void sendError(String error) {
                Toast.makeText(MQTTActivity.this, "发送消息失败，请再次试试", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void subscribeSuccess(String subtopic) {
                log("订阅成功：" + subtopic);
            }

            @Override
            public void subscribeFailure(String error) {
                log("订阅失败: " + error);
                Toast.makeText(MQTTActivity.this, "此次订阅失败,稍后再试试", Toast.LENGTH_SHORT).show();
                mSubscribeTopicListener.subscribeFailure();
            }

            @Override
            public void unSubscribeFailure(String error) {
                log("取消订阅失败: " + error);
                Toast.makeText(MQTTActivity.this, "取消订阅失败", Toast.LENGTH_SHORT).show();
            }
        };


        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinder = (MQTTMessageService.DownloadBinder) service;
                mBinder.setService(mServiceData.getUrl(), mServiceData.getPost(), mServiceData.getServiceName(), mServiceData.getServicePassword())
                        .setLWT(mServiceData.getLWTSave())
                        .setClientId(mServiceData.getUseId())
                        .setTimeoutAndKeepAlive(mServiceData.getConnectTimeout(), mServiceData.getKeepAliveInterval())
                        .setCallback(mCallBack)
                        .setReconnection(mServiceData.isReconnection())
                        .start();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                log("onServiceDisconnected" + name.toString());
            }
        };


        //mServiceData = new MQTTServiceData(this).getData("admin");
        mServiceData = new ServiceData();
        /*if (mServiceData == null){
            log("mServiceData == null");
            Toast.makeText(this, "mServiceData == null", Toast.LENGTH_SHORT).show();
        }*/
    }

    //设置头部
    private void initTitle() {
        new DefaultNavigationBar
                .Builder(this, (ViewGroup) findViewById(R.id.main))
                .setTitle("MQTT")
                .builer();
    }

    private void log(String log) {
        Log.d("AppRun" + getClass().getSimpleName(), log);
    }


}
