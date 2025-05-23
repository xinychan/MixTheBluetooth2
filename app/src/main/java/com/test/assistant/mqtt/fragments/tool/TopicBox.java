package com.test.assistant.mqtt.fragments.tool;

public class TopicBox {
    private String topic;
    private String message;
    private int QoS;
    private boolean isRetain = false;

    public TopicBox(String topic, String message, int QoS) {
        this.topic = topic;
        this.message = message;
        this.QoS = QoS;
    }

    public TopicBox(String topic, String message, int QoS, boolean isRetain) {
        this.topic = topic;
        this.message = message;
        this.QoS = QoS;
        this.isRetain = isRetain;
    }

    public int getQoS() {
        return QoS;
    }

    public String getMessage() {
        return message;
    }

    public String getTopic() {
        return topic;
    }

    public boolean isRetain() {
        return isRetain;
    }
}
