package com.test.assistant.popupWindows;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.test.assistant.ConnectHcServers;
import com.test.assistant.R;
import com.test.assistant.singleton.SingMessage;
import com.test.assistant.storage.DataMemory;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.OnLongClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;

import java.util.Map;


public class ConnectService extends LinearLayout {

    @ViewById(R.id.service_ip)
    private EditText mIpEditText;
    @ViewById(R.id.service_port)
    private EditText mPortEditText;

    @ViewById(R.id.service_more)
    private TextView mMoreTv;

    @ViewById(R.id.service_hide)
    private LinearLayout mHideLinear;

    @ViewById(R.id.service_record)
    private LinearLayout mRecord;

    @ViewById(R.id.service_record_content)
    private TextView mRecordContent;

    @ViewById(R.id.service_hide_first)
    private TextView mFirstTv;

    @ViewById(R.id.service_second)
    private TextView mSecondTv;

    @ViewById(R.id.service_hide_thirdly)
    private TextView mThirdlyTv;

    @ViewById(R.id.service_hide_fourthly)
    private TextView mFourthlyTv;

    @ViewById(R.id.service_hide_fifth)
    private TextView mFifthTv;

    private DataMemory mDataMemory = SingMessage.getSingMessage().getDataMemory();

    public ConnectService(Context context) {
        this(context, null);
    }

    public ConnectService(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConnectService(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.connect_service_menu, this);
        ViewUtils.inject(this);
        initView();
    }

    private void initView() {
        if (mDataMemory == null)
            return;
        if (mDataMemory.getServiceIp() == null)
            return;
        if (!mDataMemory.getServiceIp().isEmpty())
            mIpEditText.setText(mDataMemory.getServiceIp());
        if (mDataMemory.getServicePort() == null)
            return;
        if (!mDataMemory.getServicePort().isEmpty())
            mPortEditText.setText(mDataMemory.getServicePort());
    }

    @OnClick(R.id.service_message_back)
    private void back() {
        //退出
        SingMessage.getSingMessage().getConnectService().dismiss();
    }

    @OnClick(R.id.service_message_connect)
    private void connect() {
        //连接
        Context context = SingMessage.getSingMessage().getContext();
        if (mIpEditText.getText().toString().length() < "1.1.1.1".length() && mPortEditText.getText().toString().length() <= 0) {
            Toast.makeText(context, "请输入完整的IP或port", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(context, ConnectHcServers.class);
        ((Activity) context).startActivity(intent);
        SingMessage.getSingMessage().mServiceIp = mIpEditText.getText().toString();
        SingMessage.getSingMessage().mServicePort = mPortEditText.getText().toString();
        SingMessage.getSingMessage().getConnectService().dismiss();
    }

    @OnClick(R.id.service_more)
    private void more() {
        if (mMoreTv.getText().toString().equals("收起列表")) {
            mMoreTv.setText("查看更多端口");
            mHideLinear.setVisibility(View.GONE);
            mRecord.setVisibility(GONE);
            clearTextView();
            return;
        }
        Map<String, String> map = mDataMemory.getServersMap();//*
        if (map == null || map.size() <= 1) {
            Toast.makeText(getContext(), "没有记载更多的端口号", Toast.LENGTH_SHORT).show();
            return;
        }
        mHideLinear.setVisibility(VISIBLE);
        mMoreTv.setText("收起列表");

        for (String key : map.keySet()) {
            setTextView(analysis(map.get(key), key));
        }
    }

    @OnClick({R.id.service_hide_first, R.id.service_second, R.id.service_hide_thirdly,
            R.id.service_hide_fourthly, R.id.service_hide_fifth})
    private void clickListener(View view) {
        String message = ((TextView) view).getText().toString();
        mIpEditText.setText(message.substring(0, message.indexOf(":")));
        mPortEditText.setText(message.substring(message.indexOf(":") + 1, message.indexOf("\n")));
    }

    @OnLongClick({R.id.service_hide_first, R.id.service_second, R.id.service_hide_thirdly,
            R.id.service_hide_fourthly, R.id.service_hide_fifth})
    private void longClickListener(View view) {
        String start = "+&*+";
        String message = ((TextView) view).getText().toString();
        Map<String, String> map = mDataMemory.getServersMap();
        String data = map.get(message.substring(message.indexOf("\n") + "\n".length()));
        mHideLinear.setVisibility(View.GONE);
        mRecord.setVisibility(VISIBLE);
        mRecordContent.setText(data.substring(data.indexOf(start) + start.length()));
    }

    @OnClick(R.id.service_back_list)
    private void backList() {
        mRecord.setVisibility(GONE);
        mRecordContent.setText("");
        mHideLinear.setVisibility(VISIBLE);
    }

    private void setTextView(String data) {
        if (mFirstTv.getText().toString().isEmpty()) {
            mFirstTv.setText(data);
            mFirstTv.setVisibility(VISIBLE);
        } else if (mSecondTv.getText().toString().isEmpty()) {
            mSecondTv.setText(data);
            mSecondTv.setVisibility(VISIBLE);
        } else if (mThirdlyTv.getText().toString().isEmpty()) {
            mThirdlyTv.setText(data);
            mThirdlyTv.setVisibility(VISIBLE);
        } else if (mFourthlyTv.getText().toString().isEmpty()) {
            mFourthlyTv.setText(data);
            mFourthlyTv.setVisibility(VISIBLE);
        } else if (mFifthTv.getText().toString().isEmpty()) {
            mFifthTv.setText(data);
            mFifthTv.setVisibility(VISIBLE);
        }
    }

    private void clearTextView() {
        mFirstTv.setText("");
        mSecondTv.setText("");
        mThirdlyTv.setText("");
        mFourthlyTv.setText("");
        mFifthTv.setText("");
    }

    private String analysis(String data, String time) {
        String key = "+&*+";
        String v = data.substring(0, data.indexOf(key));
        return v + "\n" + time;
    }

}
