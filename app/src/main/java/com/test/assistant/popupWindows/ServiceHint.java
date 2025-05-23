package com.test.assistant.popupWindows;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.test.assistant.R;
import com.test.assistant.singleton.SingMessage;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;


public class ServiceHint extends LinearLayout {

    @ViewById(R.id.service_bar)
    private LinearLayout mServiceBar;

    @ViewById(R.id.service_error)
    private LinearLayout mServiceError;

    @ViewById(R.id.service_back)
    private LinearLayout mServiceBack;

    @ViewById(R.id.service_progressBarLarge)
    private ProgressBar mProgressBar;

    @ViewById(R.id.service_error_text)
    private TextView mErrorMessage;

    @ViewById(R.id.service_bar_text)
    private TextView mBarText;

    @ViewById(R.id.service_back_warn)
    private LinearLayout mServiceWarn;

    @ViewById(R.id.service_back_disconnect)
    private LinearLayout mServiceDisconnect;

    private int mFunction = 0;

    private SingMessage mSingMessage = SingMessage.getSingMessage();

    public ServiceHint(Context context) {
        this(context, null);
    }

    public ServiceHint(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ServiceHint(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.service_menu, this);
        ViewUtils.inject(this);
        mSingMessage.setServiceProgressBar(mProgressBar);
        setFunction(mSingMessage.getServiceManage());
    }

    private void setFunction(int function) {
        if (function == 0) {
            //显示进度条
            mServiceBar.setVisibility(View.VISIBLE);
            mServiceError.setVisibility(View.GONE);
            mServiceBack.setVisibility(View.GONE);
        } else if (function == 1) {
            //显示错误提示
            mServiceBar.setVisibility(View.GONE);
            mServiceError.setVisibility(View.VISIBLE);
            mServiceBack.setVisibility(View.GONE);
        } else if (function == 3) {
            mBarText.setText("正在断开服务器中");
            mServiceBar.setVisibility(View.VISIBLE);
            mServiceError.setVisibility(View.GONE);
            mServiceBack.setVisibility(View.GONE);
        } else if (function == 2) {
            //显示关闭提示断开服务器
            this.mFunction = function;
            mServiceBar.setVisibility(View.GONE);
            mServiceError.setVisibility(View.GONE);
            mServiceBack.setVisibility(View.VISIBLE);
            mServiceDisconnect.setVisibility(View.GONE);
        } else if (function == 4) {
            //显示是否重置模块
            this.mFunction = function;
            mServiceBar.setVisibility(View.GONE);
            mServiceError.setVisibility(View.GONE);
            mServiceWarn.setVisibility(View.GONE);
            mServiceBack.setVisibility(View.VISIBLE);
            mServiceDisconnect.setVisibility(View.VISIBLE);
        }
    }

    public void setErrorMessage(String string) {
        mErrorMessage.setText(string);
    }

    @OnClick(R.id.service_error_button)
    private void serviceError() {
        log("用户确认错误");
        callback.stopWindows();
    }

    @OnClick(R.id.service_back_true)
    private void serviceBackTrue() {
        if (mFunction == 2)
            log("用户确认强制退出");
        else
            log("用户重置模块");
        callback.forceExit(mFunction);
    }

    @OnClick(R.id.service_back_false)
    private void serviceBackFalse() {
        log("用户取消退出");
        callback.stopWindows();
    }

    public interface ServiceHintCallback {
        void stopWindows();

        void forceExit(int function);
    }

    private ServiceHintCallback callback;

    public void setServiceHintCallback(ServiceHintCallback list) {
        this.callback = list;
    }

    private void log(String s) {
        Log.d("AppRunServiceHint", s);
    }


}
