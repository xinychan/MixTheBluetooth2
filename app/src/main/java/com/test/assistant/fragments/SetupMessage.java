package com.test.assistant.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.test.assistant.R;
import com.test.assistant.dialog.CommonDialog;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;

import java.util.Objects;

public class SetupMessage extends LinearLayout {

    @ViewById(R.id.fragment_exit_message)
    private TextView mMessageTv;

    @ViewById(R.id.fragment_menu_hint_text)
    private TextView mHintTv;

    @ViewById(R.id.fragment_menu_hint_icon)
    private ImageView mHintIv;

    private CommonDialog.Builder mBuilder;

    private Intent mMessageIntent;

    public SetupMessage(Context context) {
        this(context, null);
    }

    public SetupMessage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SetupMessage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.fragment_exit_menu, this);
        ViewUtils.inject(this);
    }

    @SuppressLint("SetTextI18n")
    public SetupMessage setMessageTv(int succeed, int error) {
        mMessageTv.setText("    配置完成，成功配置" + succeed + "个，配置失败" + error + "个，是否继续配置模块，或是返回消息传输？");
        return this;
    }

    public SetupMessage setBuilder(CommonDialog.Builder mBuilder) {
        this.mBuilder = mBuilder;
        return this;
    }

    public void setMessageIntent(Intent mMessageIntent) {
        this.mMessageIntent = mMessageIntent;
    }

    public SetupMessage hideHint(boolean setServer) {
        if (!setServer) {
            mHintIv.setVisibility(GONE);
            mHintTv.setVisibility(GONE);
        }
        return this;
    }

    @OnClick(R.id.fragment_exit_continue)
    private void continueSetup() {
        if (mBuilder == null)
            return;
        mBuilder.dismiss();
        FragmentList.mConnectWork = false;
        Intent intent = new Intent("com.test.huichengwifi.ContinueSetup");
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(Objects.requireNonNull(getContext()));
        localBroadcastManager.sendBroadcast(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @OnClick(R.id.fragment_exit_return)
    private void exit() {
        if (mBuilder == null)
            return;
        mBuilder.dismiss();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(Objects.requireNonNull(getContext()));
        localBroadcastManager.sendBroadcast(mMessageIntent);
    }

}
