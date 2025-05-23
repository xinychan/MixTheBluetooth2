package com.test.assistant.mqtt;

public class ServiceData {

    //此服务的名称
    private String mName = "admin";
    //服务IP
    private String mUrl = "192.168.1.121";
    //服务端口
    private String mPost = "8080";
    //订阅的QoS
    private int mQos = 0;
    //服务的登陆名称
    private String mServiceName = "admin";
    //登陆密码
    private String mServicePassword = "password";
    //使用的客户ID
    private String mUseId = "114455";
    //订阅的主题
    private String mSubtopic = null;
    //断线重连
    private boolean mReconnection = true;
    //连接超时
    private int mConnectTimeout = 3;
    //存活心跳
    private int mKeepAliveInterval = 30;
    //遗言LWT
    private LWTSave mLWTSave = null;


    public void setName(String mName) {
        this.mName = mName;
    }

    public String getName() {
        return mName;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getPost() {
        return mPost;
    }

    public void setPost(String mPost) {
        this.mPost = mPost;
    }

    public int getQos() {
        return mQos;
    }

    public void setQos(int mQos) {
        this.mQos = mQos;
    }

    public String getServiceName() {
        return mServiceName;
    }

    public void setServiceName(String mServiceName) {
        this.mServiceName = mServiceName;
    }

    public String getServicePassword() {
        return mServicePassword;
    }

    public void setServicePassword(String mServicePassword) {
        this.mServicePassword = mServicePassword;
    }

    public String getUseId() {
        return mUseId;
    }

    public void setUseId(String mUseId) {
        this.mUseId = mUseId;
    }

    public void setSubtopic(String mSubtopic) {
        this.mSubtopic = mSubtopic;
    }

    public String getSubtopic() {
        return mSubtopic;
    }

    public void setReconnection(boolean mReconnection) {
        this.mReconnection = mReconnection;
    }

    public boolean isReconnection() {
        return mReconnection;
    }

    public void setConnectTimeout(int mConnectTimeout) {
        this.mConnectTimeout = mConnectTimeout;
    }

    public int getConnectTimeout() {
        return mConnectTimeout;
    }

    public void setKeepAliveInterval(int mKeepAliveInterval) {
        this.mKeepAliveInterval = mKeepAliveInterval;
    }

    public int getKeepAliveInterval() {
        return mKeepAliveInterval;
    }

    public void setLWTSave(LWTSave mLWTSave) {
        this.mLWTSave = mLWTSave;
    }

    public LWTSave getLWTSave() {
        return mLWTSave;
    }

    class LWTSave {
        public String topic = "topic";
        public String message = "message";
        public int qos = 2;
        public boolean retained = true;

        public LWTSave() {
        }

        public LWTSave(String topic, String message, int qos, boolean retained) {
            this.topic = topic;
            this.message = message;
            this.qos = qos;
            this.retained = retained;
        }
    }

}
