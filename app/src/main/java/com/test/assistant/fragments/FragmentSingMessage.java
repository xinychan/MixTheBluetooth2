package com.test.assistant.fragments;


import android.os.Handler;

import com.test.assistant.dialog.CommonDialog;
import com.test.assistant.recyclerAdapter.Resou;
import com.test.assistant.storage.FragmentButtonData;

import java.util.ArrayList;
import java.util.List;


public class FragmentSingMessage {
    private FragmentSingMessage() {
    }

    private static FragmentSingMessage mFragmentSingMessage = new FragmentSingMessage();

    public static FragmentSingMessage getFragmentSingMessage() {
        return mFragmentSingMessage;
    }

    private FragmentButtonData mFragmentButtonData;

    private int mButtonId;

    private CommonDialog.Builder mAddButtonBuilder, mButtonManageBuilder;

    //从Fragment发送消息，FragmentMessage接收信息，FragmentCustom接收信息
    private Handler mSendFragmentHandler, mReceiveFragMessageHandler, mReceiveFragCustomHandler, mReceiveFragConnectHandler;

    //这是在首页连接服务器的handler
    private Handler mSendServerHandler, mReceiveServerMessageHandler, mReceiveServerCustomHandler;

    private List<Resou> mModelList = new ArrayList<>();

    void setFragmentButtonData(FragmentButtonData mFragmentButtonData) {
        this.mFragmentButtonData = mFragmentButtonData;
    }

    FragmentButtonData getFragmentButtonData() {
        return mFragmentButtonData;
    }

    void setButtonId(int mButtonId) {
        this.mButtonId = mButtonId;
    }

    int getButtonId() {
        return mButtonId;
    }

    void setAddButtonBuilder(CommonDialog.Builder mAddButtonBuilder) {
        this.mAddButtonBuilder = mAddButtonBuilder;
    }

    CommonDialog.Builder getAddButtonBuilder() {
        return mAddButtonBuilder;
    }

    void setButtonManageBuilder(CommonDialog.Builder mButtonManageBuilder) {
        this.mButtonManageBuilder = mButtonManageBuilder;
    }

    CommonDialog.Builder getButtonManageBuilder() {
        return mButtonManageBuilder;
    }

    public void setSendFragmentHandler(Handler mSendFragmentHandler) {
        this.mSendFragmentHandler = mSendFragmentHandler;
    }

    Handler getSendFragmentHandler() {
        return mSendFragmentHandler;
    }

    void setReceiveFragCustomHandler(Handler mReceiveFragCustomHandler) {
        this.mReceiveFragCustomHandler = mReceiveFragCustomHandler;
    }

    public Handler getReceiveFragCustomHandler() {
        return mReceiveFragCustomHandler;
    }

    void setReceiveFragConnectHandler(Handler mReceiveFragConnectHandler) {
        this.mReceiveFragConnectHandler = mReceiveFragConnectHandler;
    }

    public Handler getReceiveFragConnectHandler() {
        return mReceiveFragConnectHandler;
    }

    void setReceiveFragMessageHandler(Handler mReceiveFragMessageHandler) {
        this.mReceiveFragMessageHandler = mReceiveFragMessageHandler;
    }

    public Handler getReceiveFragMessageHandler() {
        return mReceiveFragMessageHandler;
    }

    public void setModelList(List<Resou> mModelList) {
        if (this.mModelList.size() == 0) {
            this.mModelList = mModelList;
            return;
        }

        boolean b;
        for (Resou resou : this.mModelList) {
            b = true;
            for (Resou resou1 : mModelList) {
                if (resou.getmIP().equals(resou1.getmIP())) {
                    b = false;
                    break;
                }
            }
            if (b) {
                mModelList.add(resou);
            }
        }

        this.mModelList = mModelList;

    }

    List<Resou> getModelList() {
        return mModelList;
    }

    public void clearList() {
        for (Resou resou : mModelList) {
            if (!resou.survival()) {
                mModelList.remove(resou);
            }
        }
    }

    void clearModelList() {
        mModelList.clear();
    }

    public void setSendServerHandler(Handler mSendServerHandler) {
        this.mSendServerHandler = mSendServerHandler;
    }

    Handler getSendServerHandler() {
        return mSendServerHandler;
    }

    void setReceiveServerMessageHandler(Handler mReceiveServerMessageHandler) {
        this.mReceiveServerMessageHandler = mReceiveServerMessageHandler;
    }

    public Handler getReceiveServerMessageHandler() {
        return mReceiveServerMessageHandler;
    }

    void setReceiveServerCustomHandler(Handler mReceiveServerCustomHandler) {
        this.mReceiveServerCustomHandler = mReceiveServerCustomHandler;
    }

    public Handler getReceiveServerCustomHandler() {
        return mReceiveServerCustomHandler;
    }

}
