package com.test.assistant.fragments;


import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.test.assistant.R;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewUtils;


public class ButtonManage extends LinearLayout {

    private int mGroupNumber = 0;

    private int mSwitchWeight = 0;

    public ButtonManage(Context context) {
        this(context, null);
    }

    public ButtonManage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ButtonManage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.button_manage_menu, this);
        ViewUtils.inject(this);
        initAll();
    }

    private void initAll() {
        mGroupNumber = FragmentSingMessage.getFragmentSingMessage().getFragmentButtonData().getButtonGroupNumber();
    }

    @OnClick({R.id.button_group_add, R.id.button_group_hide, R.id.button_group_delete})
    private void buttonGroup(View v) {
        switch (v.getId()) {
            case R.id.button_group_add:
                mSwitchWeight = 0;
                break;
            case R.id.button_group_hide:
                mSwitchWeight = 1;
                break;
            case R.id.button_group_delete:
                mSwitchWeight = 2;
                break;
        }

    }

    @OnClick(R.id.add_button_back)
    private void back() {
        //退出
        FragmentSingMessage.getFragmentSingMessage().getButtonManageBuilder().dismiss();
    }

    @OnClick(R.id.add_button_affirm)
    private void affirm(View view) {
        //设置
        FragmentSingMessage fragmentSingMessage = FragmentSingMessage.getFragmentSingMessage();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.
                getInstance(fragmentSingMessage.getButtonManageBuilder().getContext());
        if (mSwitchWeight == 0)
            ++mGroupNumber;
        if (mSwitchWeight == 1)
            --mGroupNumber;
        if (mSwitchWeight == 2) {
            mGroupNumber = 0;
            fragmentSingMessage.getFragmentButtonData().delete();
            Intent intent = new Intent("com.test.huichengwifi.DELETE_BUTTON_GROUP");
            localBroadcastManager.sendBroadcast(intent);
        }
        mGroupNumber = mGroupNumber > 5 ? 5 : mGroupNumber;
        Intent intent = new Intent("com.test.huichengwifi.ADD_BUTTON_GROUP");
        intent.putExtra("Update", mGroupNumber);
        localBroadcastManager.sendBroadcast(intent);


        mGroupNumber = mGroupNumber < 0 ? 0 : mGroupNumber;
        mGroupNumber = mGroupNumber > 4 ? 4 : mGroupNumber;
        fragmentSingMessage.getFragmentButtonData().saveButtonGroupNumber(mGroupNumber);
        fragmentSingMessage.getButtonManageBuilder().dismiss();
    }
}
