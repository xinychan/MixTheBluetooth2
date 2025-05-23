package com.test.assistant.fragments.threadPool;

public interface ModuleMessage {
    void getMessage(String ip, String post, int position) throws Exception;

    void error(String e, int position) throws Exception;
}
