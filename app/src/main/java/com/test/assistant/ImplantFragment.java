package com.test.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.test.assistant.baseUse.DefaultNavigationBar;
import com.test.assistant.fragments.FragmentCustom;
import com.test.assistant.fragments.FragmentList;
import com.test.assistant.fragments.FragmentManage;
import com.test.assistant.fragments.FragmentMessage;
import com.test.assistant.fragments.FragmentSingMessage;
import com.test.assistant.keyboardListener.KeyboardChangeListener;
import com.test.assistant.storage.DataMemory;
import com.test.assistant.wifiGather.connectSocket.ConnectSocket;
import com.test.baselibrary.ioc.CheckWifi;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;
import com.test.connectservicelibrary.connectInternet.ConnectInternetManage;

import java.util.ArrayList;
import java.util.List;

import static com.test.assistant.fragments.FragmentMessage.mIsConnectInternet;


public class ImplantFragment extends BasActivity {

    @ViewById(R.id.bottom_group)
    private RadioGroup mRadioGroup;

    private FragmentManage mFragmentManage;
    private LocalReceiver mLocalReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;

    private String ModuleName;

    private ConnectSocket mConnectSocket;

    private ConnectInternetManage mConnectInternetManage;//连接服务器的管理类

    public static boolean mIsAllConnectService = false;//是否启用配置多个模块到服务器

    public DefaultNavigationBar mDefaultNavigationBar;

    private DataMemory mDataMemory;

    public static int mAcceptNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        ViewUtils.inject(this);
        initAll();
        messageGroup();
        connectSocket(false);
    }

    private void initAll() {
        mFragmentManage = new FragmentManage(R.id.frag);
        ModuleName = getIntent().getStringExtra("Name");
        mDataMemory = new DataMemory(this);
        FragmentSingMessage.getFragmentSingMessage().setSendFragmentHandler(mSendFragmentHandler);
        initBroadcast();
        initTitle();
        initKeyboardChangeListener();//监听软键盘是否弹出
    }

    private void initBroadcast() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.test.huichengwifi.STOP_LIST");
        mIntentFilter.addAction("com.test.huichengwifi.DISSERVICE");
        mIntentFilter.addAction("com.test.huichengwifi.REFRESH_LIST");
        mLocalReceiver = new LocalReceiver();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mLocalReceiver, mIntentFilter);
    }

    //设置头部
    private void initTitle() {

        if (ModuleName == null)
            ModuleName = "HC-25";

        mDefaultNavigationBar = new DefaultNavigationBar
                .Builder(this, (ViewGroup) findViewById(R.id.activity_fragment_name))
                .setTitle(ModuleName)
                .hideLeftText()
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

    //建立socket连接
    private void connectSocket(boolean isReconnect) {
        Log.e("AppRunTime", "connectSocket执行..");
        if (mConnectSocket != null) {
            mConnectSocket.showdown();
        }
        mConnectSocket = null;
        if (isReconnect)
            mConnectSocket = new ConnectSocket(ImplantFragment.this, mMessageHandler, ModuleName);
        else
            mConnectSocket = new ConnectSocket(ImplantFragment.this, mMessageHandler, ModuleName, 0);
    }

    //接收socket传来的信息,并传给显示的fragment
    private Handler mMessageHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            try {
                receive(msg.what, msg.obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    });

    //处理从fragment发送过来的信息
    private Handler mSendFragmentHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 0x00) {
                send(msg.obj.toString().trim());//发送给模块或服务器的数据
            }
            if (msg.what == 0x01) {
                //断开局域网，并请求服务器等初始化
                if (mConnectInternetManage == null)
                    mConnectInternetManage = new ConnectInternetManage(ImplantFragment.this, FragmentMessage.mIp, mServiceHandler);
                mConnectSocket.showdown();
                mConnectInternetManage.setReceiveTv((TextView) msg.obj);//设置接收的数据数的TextView
                mConnectInternetManage.getServices();
            }
            if (msg.what == 0x02) {
                //连接局域网
                connectSocket(true);
                log("复位模块回局域网...");
            }

            if (msg.what == 0x03) {
                //断开服务器
                if (mConnectInternetManage == null)
                    mConnectInternetManage = new ConnectInternetManage(ImplantFragment.this, FragmentMessage.mIp, mServiceHandler);
                mConnectInternetManage.disConnectService();
            }

            if (msg.what == 0x04) {
                //连接服务器
                mConnectInternetManage.getServices();
            }

            if (msg.what == 0x05) {
                //设置收发hex格式
                setServiceHex(msg.obj.toString());
            }

            if (msg.what == 0x06) {
                //在配置多个模块上服务器时，设置的接收数的TextView
                try {
                    mConnectInternetManage.setReceiveTv((TextView) msg.obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (msg.what == 0x09) {
                finish();//退出Activity
            }

            if (msg.what == 0x10) {
                //配置多个模块上服务器时的连接测试
                if (mConnectInternetManage != null) {
                    //预防重复连接
                    mConnectInternetManage.unConnect();
                    mConnectInternetManage = null;
                }
                List<String> list = (ArrayList<String>) msg.obj;
                mConnectInternetManage = new ConnectInternetManage(ImplantFragment.this, list.get(0),
                        list.get(1), mServiceHandler);
            }

            return false;
        }
    });

    //中转而已
    private Handler mServiceHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            receive(msg.what, msg.obj);
            return false;
        }
    });

    //发送函数
    private void send(String data) {
        if (mIsConnectInternet && mConnectInternetManage != null) {
            mConnectInternetManage.sendData(data);//广域网发送
            log("单独广域网发送");
            return;
        }
        if (mIsAllConnectService && mConnectInternetManage != null) {
            mConnectInternetManage.sendData(data);//广域网发送
            log("多个模块广域网发送");
            return;
        }
        //局域网发送
        log("局域网发送");
        mConnectSocket.send(data);
    }

    //接收函数,回馈到Fragment的函数
    private void receive(int what, Object data) {
        FragmentSingMessage fragmentSingMessage = FragmentSingMessage.getFragmentSingMessage();
        if (mFragmentManage.getFragmentId() != -1) {
            if (mFragmentManage.getFragmentId() == R.id.message_group) {
                Message mess = fragmentSingMessage.getReceiveFragMessageHandler().obtainMessage();
                mess.what = what;
                if (data != null)
                    mess.obj = data;
                fragmentSingMessage.getReceiveFragMessageHandler().sendMessage(mess);
            }
            if (mFragmentManage.getFragmentId() == R.id.custom_group) {
                Message mess = fragmentSingMessage.getReceiveFragCustomHandler().obtainMessage();
                mess.what = what;
                if (data != null)
                    mess.obj = data;
                fragmentSingMessage.getReceiveFragCustomHandler().sendMessage(mess);
            }
            if (mFragmentManage.getFragmentId() == R.id.discovery_group) {
                Message mess = fragmentSingMessage.getReceiveFragConnectHandler().obtainMessage();
                mess.what = what;
                if (data != null)
                    mess.obj = data;
                if (mess.what == 0x04 && data != null && data.equals("connected")) {
                    mIsAllConnectService = true;
                    mConnectSocket.showdown();
                }
                fragmentSingMessage.getReceiveFragConnectHandler().sendMessage(mess);
            }
        } else {
            Toast.makeText(this, "出错了", Toast.LENGTH_SHORT).show();
        }
    }

    //设置服务器Hex模式
    private void setServiceHex(String hex) {
        if (hex.equals("SendHex")) {
            mConnectInternetManage.setSendHex(FragmentMessage.mSendHex);
        }

        if (hex.equals("Accept")) {
            mConnectInternetManage.setAccept(FragmentMessage.mAccept);
        }
    }


    @OnClick(R.id.message_group)
    private void messageGroup() {
        if (FragmentList.mConnectWork) {
            Toast.makeText(this, "正在配置中，暂不能切换，请稍后", Toast.LENGTH_SHORT).show();
            restorationClick();
            return;
        }
        if (mFragmentManage.getFragmentId() == R.id.message_group) {
            return;
        }
        mFragmentManage.initFragment(R.id.message_group, new FragmentMessage(),
                getSupportFragmentManager().beginTransaction());
    }

    @OnClick(R.id.custom_group)
    private void customGroup() {
        if (FragmentList.mConnectWork) {
            Toast.makeText(this, "正在配置中，暂不能切换，请稍后", Toast.LENGTH_SHORT).show();
            restorationClick();
            return;
        }
        if (mFragmentManage.getFragmentId() == R.id.custom_group) {
            return;
        }
        if (!FragmentMessage.mConnectModule) {
            Toast.makeText(this, "连接中，暂不可以切换", Toast.LENGTH_SHORT).show();
            restorationClick();
            return;
        }

        mFragmentManage.initFragment(R.id.custom_group, new FragmentCustom(),
                getSupportFragmentManager().beginTransaction());
    }

    @OnClick(R.id.discovery_group)
    @CheckWifi
    private void discoveryGroup() {
        if (mIsConnectInternet) {
            Toast.makeText(this, "连接服务器的情况下，暂不能进入此界面", Toast.LENGTH_SHORT).show();
            restorationClick();
            return;
        }
        if (mFragmentManage.getFragmentId() == R.id.discovery_group) {
            return;
        }
        if (!FragmentMessage.mConnectModule) {
            Toast.makeText(this, "连接中，暂不可以切换", Toast.LENGTH_SHORT).show();
            restorationClick();
            return;
        }
        mFragmentManage.delete(R.id.discovery_group, getSupportFragmentManager().beginTransaction());
        mFragmentManage.initFragment(R.id.discovery_group, new FragmentList(),
                getSupportFragmentManager().beginTransaction());
    }

    private void restorationClick() {
        if (mFragmentManage.getFragmentId() == R.id.message_group)
            mRadioGroup.check(R.id.message_group);
        if (mFragmentManage.getFragmentId() == R.id.custom_group)
            mRadioGroup.check(R.id.custom_group);
        if (mFragmentManage.getFragmentId() == R.id.discovery_group)
            mRadioGroup.check(R.id.discovery_group);
    }

    //app内广播
    private class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("com.test.huichengwifi.STOP_LIST")) {
                //将模块配置到服务器完成，返回FragmentMessage
                mFragmentManage.delete(R.id.discovery_group, getSupportFragmentManager().beginTransaction());
                mFragmentManage.initFragment(R.id.message_group, new FragmentList(), getSupportFragmentManager().beginTransaction());
                mRadioGroup.check(R.id.message_group);
                mDefaultNavigationBar.updateText(intent.getStringExtra("message"));//更新头部的名称
            }
            if (intent.getAction() != null && intent.getAction().equals("com.test.huichengwifi.DISSERVICE")) {
                //将模块配置到局域网完成，返回FragmentMessage
                mFragmentManage.delete(R.id.discovery_group, getSupportFragmentManager().beginTransaction());
                mFragmentManage.initFragment(R.id.message_group, new FragmentMessage(), getSupportFragmentManager().beginTransaction());
                mRadioGroup.check(R.id.message_group);
                mIsAllConnectService = false;
                connectSocket(false);
                receive(0x11, "");
                if (mConnectInternetManage != null) {
                    mConnectInternetManage.unConnect();
                }
                mDefaultNavigationBar.updateText(ModuleName);//更新头部的名称
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        //断开服务器会自动清空JsonRootBean类，如果没清空，证明非正常退出，所以保存端口
        if (mConnectInternetManage != null && mConnectInternetManage.getJsonRootBean() != null && mIsConnectInternet)
            mDataMemory.saveServicePort(mConnectInternetManage.getJsonRootBean().getAddress(), mConnectInternetManage.getJsonRootBean().getPort(), "名称: " + ModuleName + "\n" + "IP: " + FragmentMessage.mIp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mLocalReceiver);
        mConnectSocket.showdown();
        if (mConnectInternetManage != null) {
            mConnectInternetManage.unConnect();
        }
        FragmentMessage.mIp = "192.168.4.1";
        FragmentList.mConnectWork = false;
        mIsAllConnectService = false;
        FragmentMessage.mSendsNumber = 0;
        mAcceptNumber = 0;
    }
}