package com.test.assistant.mqtt.fragments.tool;

import android.os.Handler;

public interface MessageInterface {
    void setHandler(Handler handler);

    void setTopicMessage(TopicBox topicBox);

    void subscribeFailure();//订阅失败
}
