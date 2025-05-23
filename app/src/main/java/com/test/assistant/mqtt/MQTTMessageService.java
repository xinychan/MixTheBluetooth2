package com.test.assistant.mqtt;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MQTTMessageService extends Service {

    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;
    private IMQTTCallBack mCallBack;

    private String mUrl = "tcp://mqtt.heclouds.com";
    private String mPost = ":6002";
    private int mQos = 2;
    private String mServiceName = "144661";
    private String mServicePassword = "YVcvXW7CSRo91=Zp64WgUwIg4E0=";
    private String mUseId = "31835883";
    private String mSubtopic;//订阅的主题
    private int mTimeout = 30;//超时
    private boolean mReconnect = false;
    private int mKeepAlive = 30;//心跳间隔
    private ServiceData.LWTSave mLWTSave = null;//遗言

    private final int SERVICE_CONNECT_ERROR = 1;//连接失败
    private final int SERVICE_CONNECT_SUCCESS = 2;//连接成功
    private final int SERVICE_CONNECT_DEL = 3;//断线
    private final int SERVICE_SUBSCRIBE_FAILURE = 4;//订阅失败
    private final int SERVICE_SUBSCRIBE_SUCCESS = 5;//订阅成功
    private final int SERVICE_UNSUBSCRIBE_FAILURE = 6;//取消订阅失败
    private final int SERVICE_SEND_ERROR = 7;//发送失败

    private boolean mReadGBK = true;//是否用GBK转码 --- 默认为true


    @Override
    public void onCreate() {
        super.onCreate();
        log("本地服务启动就绪..");
    }


    /**
     * 连接mqtt
     */
    private void connectMqtt() {

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    log("连接成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    callback(SERVICE_CONNECT_ERROR, exception.getMessage());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            callback(SERVICE_CONNECT_ERROR, e.getMessage());
        }
    }


    /**
     * mqtt初始化
     */
    private void initMqtt() {
        try {
            mqttAndroidClient = new MqttAndroidClient(
                    getApplicationContext(),
                    mUrl + mPost, mUseId);
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    if (reconnect) {
                        callback(SERVICE_CONNECT_SUCCESS, "MQTT重新连接成功！serverURI:" + serverURI);
                    } else {
                        callback(SERVICE_CONNECT_SUCCESS, "MQTT连接成功！serverURI:" + serverURI);
                    }
                    if (mSubtopic != null)
                        subscribeToTopic(mSubtopic, mQos);
                }

                @Override
                public void connectionLost(Throwable cause) {//连接断开
                    callback(SERVICE_CONNECT_DEL, cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {//收到消息
                    try {
                        String tempMessage = message.toString();
                        if (pattern(tempMessage)) {
                            byte[] bytes = message.getPayload();
                            tempMessage = new String(bytes, "GBK");
                        }
                        readBackMsg(tempMessage, topic, String.valueOf(message.getQos()));
                        //Toast.makeText(MQTTMessageService.this, "收到主题："+topic+ " 消息："+tempMessage+" 服务质量："+ message.getQos(), Toast.LENGTH_SHORT).show();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    try {
                        String s = token.getMessage().toString();
                        if (pattern(s)) {
                            byte[] bytes = token.getMessage().getPayload();
                            log("确认收到:" + new String(bytes, "GBK"));
                            return;
                        }
                        log("确认收到:" + s);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            // 新建连接设置
            mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_DEFAULT);
            mqttConnectOptions.setUserName(mServiceName);
            mqttConnectOptions.setPassword(mServicePassword.toCharArray());
            //断开后，是否自动连接
            mqttConnectOptions.setAutomaticReconnect(mReconnect);
            //是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
            mqttConnectOptions.setCleanSession(true);
            //设置超时时间，单位为秒
            mqttConnectOptions.setConnectionTimeout(mTimeout);
            //心跳时间，单位为秒。即多长时间确认一次Client端是否在线
            mqttConnectOptions.setKeepAliveInterval(mKeepAlive);
            //允许同时发送几条消息（未收到broker确认信息）
            mqttConnectOptions.setMaxInflight(30);
            //遗言
            if (mLWTSave != null) {
                try {
                    mqttConnectOptions.setWill(mLWTSave.topic, mLWTSave.message.getBytes("GBK"), mLWTSave.qos, mLWTSave.retained);
                    log("LWT设置:" + mLWTSave.topic + " message" + mLWTSave.message + " qos:" + mLWTSave.qos + " retained:" + mLWTSave.retained);
                } catch (Exception e) {
                    Toast.makeText(this, "LWT设置失败..", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
            log("初始化完成..");
        } catch (Exception e) {
            callback(SERVICE_CONNECT_ERROR, e.toString());
            e.printStackTrace();
        }
    }

    /*
     * 取消订阅
     * */

    private void unSubscribeTopic(String topic) {
        try {
            IMqttActionListener listener = new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //取消成功
                    log("取消订阅成功");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //取消失败
                    callback(SERVICE_UNSUBSCRIBE_FAILURE, exception.getMessage());
                }
            };
            mqttAndroidClient.unsubscribe(topic, this, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 订阅一个主主题
     *
     * @param subTopic 主题名称
     */
    private void subscribeToTopic(final String subTopic, int qos) {
        try {
            mqttAndroidClient.subscribe(subTopic, qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    callback(SERVICE_SUBSCRIBE_SUCCESS, subTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    callback(SERVICE_SUBSCRIBE_FAILURE, subTopic + "订阅消息失败,失败原因: " + exception.getMessage());
                }
            });
        } catch (MqttException ex) {
            log("subscribeToTopic: Exception whilst subscribing");
            callback(SERVICE_SUBSCRIBE_FAILURE, subTopic + " 失败原因: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * 发布主题
     *
     * @param topic 主题
     * @param msg   内容
     * @param qos   qos
     */
    public void publishMessage(String topic, String msg, int qos, boolean isRetain) {
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            try {
                log("publishMessage: 发送" + msg + "  QoS:" + qos + " isRetain:" + isRetain);
                if (mReadGBK)
                    mqttAndroidClient.publish(topic, msg.getBytes("GBK"), qos, isRetain);
                else
                    mqttAndroidClient.publish(topic, msg.getBytes(), qos, isRetain);
            } catch (Exception e) {
                callback(SERVICE_SEND_ERROR, "publishMessage: Error Publishing: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            callback(SERVICE_SEND_ERROR, "发送数据失败，MQTT未连接");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }


    private DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void log(String log) {
        Log.d("AppRun" + getClass().getSimpleName(), log);
    }

    public class DownloadBinder extends Binder {
        public void setMessage(String topic, String msg, int qos, boolean isRetain) {
            publishMessage(topic, msg, qos, isRetain);
        }

        public void start() {
            //初始化mqtt配置
            initMqtt();
            //连接mqtt
            connectMqtt();
        }

        public DownloadBinder setCallback(IMQTTCallBack callback) {
            mCallBack = callback;
            return this;
        }

        public DownloadBinder setService(String url, String post, String name, String password) {
            mUrl = "tcp://" + url;
            mPost = ":" + post;
            mServiceName = name;
            mServicePassword = password;
            return this;
        }

        public DownloadBinder setClientId(String id) {
            mUseId = id;
            return this;
        }

        public DownloadBinder setQos(int qos) {
            mQos = qos;
            return this;
        }

        public DownloadBinder setTimeoutAndKeepAlive(int timeout, int keepAlive) {
            mTimeout = timeout;
            mKeepAlive = keepAlive;
            return this;
        }

        public DownloadBinder setLWT(ServiceData.LWTSave lwt) {
            mLWTSave = lwt;
            return this;
        }

        public DownloadBinder setReconnection(boolean reconnect) {
            mReconnect = reconnect;
            return this;
        }

        public DownloadBinder setSubtopic(String subtopic) {
            mSubtopic = subtopic;
            return this;
        }

        public void unSubscribeTopic(String topic) {
            MQTTMessageService.this.unSubscribeTopic(topic);
        }

        public void subscribeTopic(String topic, int QoS) {
            subscribeToTopic(topic, QoS);
        }

        public void stop() {
            //断开mqtt连接
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mqttAndroidClient.unregisterResources();
                            Thread.sleep(200);
                            log("关闭MQTT");
                            mqttAndroidClient.disconnect();
                            mqttAndroidClient = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        }
    }

    private void callback(int value, String data) {
        if (mCallBack == null)
            return;
        switch (value) {

            case SERVICE_CONNECT_ERROR:
                mCallBack.connectError(data);
                break;

            case SERVICE_CONNECT_SUCCESS:
                mCallBack.connectService(data);
                break;

            case SERVICE_CONNECT_DEL:
                mCallBack.connectServiceDel(data);
                break;

            case SERVICE_SUBSCRIBE_SUCCESS:
                mCallBack.subscribeSuccess(data);
                break;

            case SERVICE_SUBSCRIBE_FAILURE:
                mCallBack.subscribeFailure(data);
                break;

            case SERVICE_SEND_ERROR:
                mCallBack.sendError(data);
                break;

            case SERVICE_UNSUBSCRIBE_FAILURE:
                mCallBack.unSubscribeFailure(data);
                break;

        }
    }

    private void readBackMsg(String data, String subtopic, String qos) {
        if (mCallBack != null) {
            mCallBack.readBackMsg(data, subtopic, qos);
        }
    }

    private boolean pattern(String str) {
        int number = str.length();
        for (int i = 1; i < number; i++) {
            if (str.substring(i - 1, i).equals("�")) {
                return true;
            }
        }
        return false;
    }

}

