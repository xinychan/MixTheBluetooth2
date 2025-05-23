package com.test.assistant.popupWindows;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.test.assistant.R;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;


public class GetPassword extends LinearLayout {

    @ViewById(R.id.service_ip)
    private EditText mName;
    @ViewById(R.id.service_port)
    private EditText mPassword;

    @ViewById(R.id.service_more)
    private TextView mMoreTv;

    @ViewById(R.id.service_title)
    private TextView mTitle;

    @ViewById(R.id.service_explain1)
    private TextView mExplain1;

    @ViewById(R.id.service_explain2)
    private TextView mExplain2;

    @ViewById(R.id.service_message_connect)
    private TextView mText;


    public GetPassword(Context context) {
        this(context, null);
    }

    public GetPassword(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GetPassword(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.connect_service_menu, this);
        ViewUtils.inject(this);
        initView();
    }

    private void initView() {
        mMoreTv.setVisibility(GONE);
        mTitle.setText("输入密码");
        mExplain1.setText("名称:");
        mExplain2.setText("密码:");
        mText.setText("确认");
    }

    @OnClick(R.id.service_message_back)
    private void back() {
        //退出
        mListener.getMessage(false, "", "");
    }

    @OnClick(R.id.service_message_connect)
    private void connect() {
        //确认
        mListener.getMessage(true, mName.getText().toString().trim(),
                mPassword.getText().toString().trim());
    }

    public void setName(String name) {
        mName.setText(name);
    }

    public void setFocusable() {
        mName.setFocusableInTouchMode(false);//不可编辑
        mName.setClickable(false);//不可点击，
        mName.setKeyListener(null);//不可粘贴，长按不会弹出粘贴框
    }


    private OnGetPasswordListener mListener;

    public interface OnGetPasswordListener {
        void getMessage(boolean b, String name, String password);
    }

    public void setOnGetPasswordListener(OnGetPasswordListener listener) {
        this.mListener = listener;
    }

}
