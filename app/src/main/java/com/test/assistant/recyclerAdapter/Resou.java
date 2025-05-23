package com.test.assistant.recyclerAdapter;


import android.graphics.Color;
import android.view.View;

import com.test.assistant.R;


/**
 * Created by xngly on 2019/5/12.
 */

public class Resou {

    private String mStr;
    private String IP;

    public boolean isMe = true;

    private boolean mClick = false;

    private int mState = 0;

    private int mSurvivalValue = 3;

    private int mHintLinearLayout = View.GONE;

    private String mServersIP = "";
    private String mServersPost = "";


    public Resou(String IP, String mStr) {

        this.mStr = mStr;
        this.IP = IP;

    }

    public Resou(String IP, String mStr, boolean click) {
        this.IP = IP;
        this.mStr = mStr;
        this.mClick = click;
    }

    public void setmStr(String str) {
        this.mStr = str;
    }

    public String getmStr() {
        return mStr;
    }

    public String getmIP() {
        return IP;
    }

    public void setClick() {
        this.mClick = !this.mClick;
    }

    public int getClick() {
        return mClick ? R.mipmap.switch_true : R.mipmap.switch_false;
    }

    public boolean getClickBoolean() {
        return mClick;
    }

    public void setState(int mState) {
        this.mState = mState;
    }

    public int getState() {
        return mState;
    }

    public String getStateText() {
        if (mState == 1)
            return "等待中";
        if (mState == 2)
            return "配置中";
        if (mState == 4)
            return "配置失败";
        else
            return "配置完成";
    }

    public int getStateTextColor() {
        if (mState == 1)
            return Color.parseColor("#FF9934");
        if (mState == 2)
            return Color.parseColor("#4DB8DC");
        if (mState == 4)
            return Color.parseColor("#FF0000");
        else
            return Color.parseColor("#79D0A5");
    }

    public boolean survival() {
        --mSurvivalValue;
        return mSurvivalValue > 0;
    }

    public void setHintLinearLayout(int mHintLinearLayout) {
        this.mHintLinearLayout = mHintLinearLayout;
    }

    public int getHintLinearLayout() {
        return mHintLinearLayout;
    }

    public String getServersIP() {
        return mServersIP;
    }

    public void setServersIP(String serversIP) {
        this.mServersIP = serversIP;
    }

    public void setServersPost(String mServersPost) {
        this.mServersPost = mServersPost;
    }

    public String getServersPost() {
        return mServersPost;
    }
}
