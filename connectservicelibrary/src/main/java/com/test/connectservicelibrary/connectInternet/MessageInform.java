package com.test.connectservicelibrary.connectInternet;

public interface MessageInform {
    void serviceCallback(boolean result, String data);

    void sendModuleCallback(boolean result, int pattern);
}
