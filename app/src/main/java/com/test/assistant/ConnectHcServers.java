package com.test.assistant;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.test.assistant.baseUse.DefaultNavigationBar;
import com.test.assistant.fragments.FragmentManage;
import com.test.assistant.fragments.FragmentServersCustom;
import com.test.assistant.fragments.FragmentServersMessage;
import com.test.assistant.fragments.FragmentSingMessage;
import com.test.assistant.keyboardListener.KeyboardChangeListener;
import com.test.assistant.singleton.SingMessage;
import com.test.assistant.storage.DataMemory;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;
import com.test.connectservicelibrary.connectInternet.ConnectInternetManage;
import com.test.connectservicelibrary.connectInternet.JsonsRootBean;


public class ConnectHcServers extends BasActivity {

    @ViewById(R.id.servers_bottom_group)
    private RadioGroup mRadioGroup;

    private SingMessage mSingMessage = SingMessage.getSingMessage();

    private ConnectInternetManage mConnectInternetManage;

    private DataMemory mDataMemory = mSingMessage.getDataMemory();

    private FragmentManage mFragmentManage;

    public static JsonsRootBean mJsonsRootBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_servers);
        ViewUtils.inject(this);
        initAll();
        messageGroup();
    }

    private void initAll() {
        mFragmentManage = new FragmentManage(R.id.connect_servers_fragment);
        initTitle();
        initConnectManage();
        initAcceptHandler();
        initKeyboardChangeListener();//监听软键盘弹出
    }

    private void initAcceptHandler() {
        FragmentSingMessage.getFragmentSingMessage().setSendServerHandler(mAcceptHandler);
    }

    private void initConnectManage() {
        if (!(mSingMessage.mServicePort.isEmpty() && mSingMessage.mServiceIp.isEmpty()))
            mConnectInternetManage = new ConnectInternetManage(this, mSingMessage.mServiceIp, mSingMessage.mServicePort, mTransferHandler);
        else
            mConnectInternetManage = new ConnectInternetManage(this, mDataMemory.getServiceIp(), mDataMemory.getServicePort(), mTransferHandler);
        mJsonsRootBean = mConnectInternetManage.getJsonRootBean();
    }

    //设置头部
    private void initTitle() {
        String string = "服务器端口号:";
        if (mSingMessage.mServiceIp.equals("120.25.163.9"))
            string = "汇承官方服务器端口";
        DefaultNavigationBar navigationBar = new DefaultNavigationBar
                .Builder(this, (ViewGroup) findViewById(R.id.activity_servers))
                .setTitle(string + mSingMessage.mServicePort)
                .builer();
    }

    private void initKeyboardChangeListener() {
        KeyboardChangeListener softKeyboardStateHelper = new KeyboardChangeListener(this);
        softKeyboardStateHelper.setKeyBoardListener(new KeyboardChangeListener.KeyBoardListener() {
            @Override
            public void onKeyboardChange(boolean isShow, int keyboardHeight) {
                if (isShow) {
                    //键盘的弹出
                    mRadioGroup.setVisibility(View.GONE);
                } else {
                    //键盘的收起
                    mRadioGroup.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    //中转Handler
    private Handler mTransferHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            receive(msg.what, msg.obj);
            return false;
        }
    });

    //接收处理fragment发来的数据
    private Handler mAcceptHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0x00:
                    sendData(msg.obj.toString());
                    break;

                case 0x04:
                    mConnectInternetManage.setSendHex((boolean) msg.obj);
                    break;

                case 0x05:
                    mConnectInternetManage.setAccept((boolean) msg.obj);
                    break;

                case 0x06:
                    setTv(msg.obj);
                    break;
            }
            return false;
        }
    });

    @OnClick(R.id.servers_message_group)
    private void messageGroup() {
        mFragmentManage.initFragment(R.id.servers_message_group, new FragmentServersMessage(),
                getSupportFragmentManager().beginTransaction());
    }

    @OnClick(R.id.servers_custom_group)
    private void customGroup() {
        mFragmentManage.initFragment(R.id.servers_custom_group, new FragmentServersCustom(),
                getSupportFragmentManager().beginTransaction());
    }

    public void sendData(String data) {
        mConnectInternetManage.sendData(data);
    }

    //接收函数,回馈到Fragment的函数
    private void receive(int what, Object data) {
        FragmentSingMessage fragmentSingMessage = FragmentSingMessage.getFragmentSingMessage();
        if (mFragmentManage.getFragmentId() != -1) {
            if (mFragmentManage.getFragmentId() == R.id.servers_message_group) {
                Message mess = fragmentSingMessage.getReceiveServerMessageHandler().obtainMessage();
                mess.what = what;
                if (data != null)
                    mess.obj = data;
                fragmentSingMessage.getReceiveServerMessageHandler().sendMessage(mess);
            }
            if (mFragmentManage.getFragmentId() == R.id.servers_custom_group) {
                Message mess = fragmentSingMessage.getReceiveServerCustomHandler().obtainMessage();
                mess.what = what;
                if (data != null)
                    mess.obj = data;
                fragmentSingMessage.getReceiveServerCustomHandler().sendMessage(mess);
            }
        } else {
            Toast.makeText(this, "出错了", Toast.LENGTH_SHORT).show();
        }
    }

    //设置FragmentServersMessage 的接收数
    private void setTv(Object o) {
        try {
            if (mConnectInternetManage != null)
                mConnectInternetManage.setReceiveTv((TextView) o);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConnectInternetManage.unConnect();
        mConnectInternetManage.clearReceiveNumber();
        FragmentServersMessage.mSendsNumber = 0;
    }
}
