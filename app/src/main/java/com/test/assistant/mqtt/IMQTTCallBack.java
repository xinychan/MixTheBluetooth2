package com.test.assistant.mqtt;

public interface IMQTTCallBack {

    void connectError(String error);//连接错误

    void connectService(String param1String);//连接成功

    void connectServiceDel(String error);//监听断开（服务断开）

    void readBackMsg(String message, String subtopic, String qos);//收到消息

    void sendError(String error);//发送时的异常

    void subscribeSuccess(String subtopic);//订阅主题成功

    void subscribeFailure(String error);//订阅主题失败

    void unSubscribeFailure(String error);//取消订阅失败

}
