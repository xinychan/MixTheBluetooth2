package com.test.assistant.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.assistant.ImplantFragment;
import com.test.assistant.R;
import com.test.assistant.commonAdapter.ItemClickListener;
import com.test.assistant.commonAdapter.MulitiTypeSupport;
import com.test.assistant.commonAdapter.RecyclerCommonAdapter;
import com.test.assistant.commonAdapter.ViewHolder;
import com.test.assistant.dialog.CommonDialog;
import com.test.assistant.popupWindows.ServiceHint;
import com.test.assistant.recyclerAdapter.Resou;
import com.test.assistant.singleton.SingMessage;
import com.test.baselibrary.ioc.CheckNet;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;
import com.test.connectservicelibrary.connectInternet.JsonsRootBean;

import java.util.ArrayList;
import java.util.List;

import static com.test.connectservicelibrary.connectInternet.ToolClass.changeHexString;

public class FragmentMessage extends Fragments implements ServiceHint.ServiceHintCallback {


    public static String mIp = "192.168.4.1";

    @ViewById(R.id.edit_sends)
    private EditText mEditTextSend;

    @ViewById(R.id.edit_sends_hex)
    private EditText mEditTextSendHex;

    private EditText mSendET;

    @ViewById(R.id.myRecycler)
    private RecyclerView mRecyclerView;

    @ViewById(R.id.circlelight)
    private ImageView mImageViewCircle;

    @ViewById(R.id.lighttip)
    private TextView mCircleLight;

    @ViewById(R.id.fragment_getdata)
    private TextView mReceiveTv;

    @ViewById(R.id.fragment_senddata)
    private TextView mSendNumber;

    @ViewById(R.id.fragment_click_accept)
    private ImageView mClickAcceptImage;

    @ViewById(R.id.fragment_click_send)
    private ImageView mClickSendImage;

    @ViewById(R.id.fragment_internet)
    private TextView mConnectInternetTv;

    private List<Resou> mDatas = new ArrayList<>();

    public static int mSendsNumber = 0;

    public static boolean mIsConnectInternet = false;

    public static boolean mSendHex = false;
    public static boolean mAccept = false;

    private SingMessage mSingMessage = SingMessage.getSingMessage();

    private Handler mTimeHandler = new Handler();

    private CommonDialog.Builder mServiceBuilder;

    private boolean mServiceError = false;

    private JsonsRootBean mJsonRootBean;

    public static boolean mConnectModule = false;//custom连接状态的指示


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_message, container, false);
        ViewUtils.inject(view, this);
        initAll();
        return view;
    }

    private void initAll() {
        FragmentSingMessage.getFragmentSingMessage().setReceiveFragMessageHandler(mReceiveFragMessageHandler);
        initRecycler();
        initHex();
        initConnectTime();
    }

    private void initRecycler() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        //设置数据
        mRecyclerView.setAdapter(new RecyclerAdapter(this.getActivity(), mDatas));
    }

    private void initHex() {
        if (mAccept) {
            acceptHex();
        }
        if (mSendHex) {
            sendHex();
        }
        mSendET = mEditTextSend;
        /*if (mConnectInternetManage == null)
            return;
        mConnectInternetManage.setAccept(mAccept);
        mConnectInternetManage.setSendHex(mSendHex);*/
    }

    private void initConnectTime() {

        mIsConnectInternet = false;
        mTimeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startServiceHint(4, null);
            }
        }, 4000);
    }

    private Handler mReceiveFragMessageHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 0x00) {
                try {
                    updateRecycler(msg.obj.toString());//更新Recycler
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (msg.what == 0x01) {
                mJsonRootBean = (JsonsRootBean) msg.obj;//获得服务器返回的信息（端口和IP）
            }

            if (msg.what == 0x03) {
                updateReceiveNumber(msg.obj.toString());//更新接收到的数据
            }

            if (msg.what == 0x04) {
                setIsOnLine(msg.obj.toString());//根据返回的值设置是否在线
            }

            if (msg.what == 0x07) {
                //模块断开服务器后配置成功  --> 可以在局域网重连模块了
                mTimeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendHandler(0x02, null);
                    }
                }, 3500);
            }
            if (msg.what == 0x08) {
                //配置配置服务器失败返回
                mServiceError = true;
                connectInternet();
                startServiceHint(1, msg.obj.toString());
            }
            if (msg.what == 0x09) {
                //配置模块失败返回
                mServiceError = true;
                if (mServiceBuilder != null)
                    mServiceBuilder.dismiss();
                if (msg.obj.toString().equals("错误：配置模块连接服务器失败，请等待app重连回模块")) {
                    connectInternet();
                }
                startServiceHint(1, msg.obj.toString());
            }
            if (msg.what == 0x10) {
                if (msg.obj.toString().equals("1")) {
                    //申请到服务器 --> 配置模块
                    mSingMessage.setServiceProgressBarEvolve(100, 850);
                }

                if (msg.obj.toString().equals("2")) {
                    //配置好模块 --> 连接服务器
                    mSingMessage.setServiceProgressBarEvolve(900, 950);
                }

                if (msg.obj.toString().equals("3")) {
                    //已经配置好模块，等待连接模块了  -->3.5秒
                    mSingMessage.setServiceProgressBarEvolve(650, 970);
                }

                if (msg.obj.toString().equals("4")) {
                    mServiceBuilder.dismiss();
                    startServiceHint(1, "已经断开服务，由于现在不处于局域网，将于3秒后自动退出");
                    mTimeHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendHandler(0x09, null);//没有WiFi情况下，直接退出
                        }
                    }, 3000);
                }
            }

            if (msg.what == 0x11) {
                mImageViewCircle.setBackground(setDrawable(R.drawable.circle3));
                mCircleLight.setText(" 正在连接..");
                mConnectInternetTv.setVisibility(View.INVISIBLE);
            }

            return false;
        }
    });

    //设置在线断线状态
    private void setIsOnLine(String state) {

        if (state.equals("connected")) {
            //图片在drawable文件夹下
            mConnectModule = true;
            mImageViewCircle.setBackground(setDrawable(R.drawable.circle1));
            mCircleLight.setTextColor(Color.parseColor("#7E7E7E"));
            mCircleLight.setText(" 已连接");
            mTimeHandler.removeMessages(0);
            if (mServiceBuilder != null) {
                mServiceBuilder.dismiss();
                if (mSingMessage.getProgressBarMax() == 950) {//代表连接模块成功
                    String string = "\n服务器的IP是：" + mJsonRootBean.getAddress() + "\n" + "服务器的端口号是: " + mJsonRootBean.getPort();
                    mDatas.add(new Resou(mIp, "已将模块连接上汇承官方服务器：" + string));
                    initRecycler();//改变转态
                    mRecyclerView.setAdapter(
                            new RecyclerAdapter(getContext(), mDatas));
                }
                mSingMessage.setServiceProgressBarEvolve(1000, 1000);
                mSingMessage.setServiceProgressBarNull();
            }
        } else {
            if (mCircleLight.getText().toString().equals(" 正在连接.."))
                return;
            //图片在drawable文件夹下
            mConnectModule = false;
            mImageViewCircle.setBackground(setDrawable(R.drawable.circle2));
            mCircleLight.setTextColor(Color.parseColor("#AE071B"));
            mCircleLight.setText(" 断线了..");
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateReceiveNumber(String data) {
        mReceiveTv.setText("R: " + data);
        ImplantFragment.mAcceptNumber = Integer.parseInt(data);
    }

    //连接服务器的按钮
    @OnClick(R.id.fragment_internet)
    @CheckNet
    private void connectInternet() {
        String modelIP = "192.168.4.1";
        if (modelIP.equals(mIp)) {
            toast("只有在模块连接到路由器的情况下,才能把模块配置到互联网");
            return;
        }
        if (!mIsConnectInternet) {//连接服务器
            mServiceError = false;
            startServiceHint(0, "");
            mSingMessage.setServiceProgressBarEvolve(0, 100);
            mConnectInternetTv.setBackground(setDrawable(R.drawable.backbox_true));
            mConnectInternetTv.setText("局域网透传");
            mIsConnectInternet = true;
            sendHandler(0x01, mReceiveTv);//断开局域网
        } else {//退出服务器
            startServiceHint(3, "");
            mSingMessage.setServiceProgressBarEvolve(0, 650);
            sendHandler(0x03, null);//断开服务器
            mConnectInternetTv.setBackground(setDrawable(R.drawable.backbox_false));
            mConnectInternetTv.setText("跨城市透传");
            mIsConnectInternet = false;
            mDatas.clear();
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mRecyclerView.setAdapter(
                    new RecyclerAdapter(getContext(), mDatas));
        }
    }

    //点击发送事件
    @SuppressLint("SetTextI18n")
    @OnClick(R.id.btn_send)
    private void sendClick(View view) {
        sendHandler(0x00, mSendET.getText().toString());
        Log.e("Scale", mSendET.getText().toString());
        int number = mSendET.getText().toString().length();
        if (mSendHex)
            number = number / 3;
        else
            number = changeHexString(true, mSendET.getText().toString()).length() / 3;
        mSendsNumber += number;
        mSendNumber.setText("S: " + mSendsNumber);
    }

    //清除输入框内容
    @OnClick(R.id.dele_edit)
    private void clearInputBox(View view) {
        mSendET.setText(null);
    }

    //清除Recycler
    @OnClick(R.id.dele_recy)
    private void clearRecycler(View view) {
        if (mDatas != null) {
            mDatas.clear();
            sign = true;
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mRecyclerView.setAdapter(
                    new RecyclerAdapter(getContext(), mDatas));
        }
    }

    //监控是否启动Hex接收与发送
    @OnClick({R.id.fragment_clickaccept, R.id.fragment_click_accept,
            R.id.fragment_clicksend, R.id.fragment_click_send})
    private void sendAcceptPatternClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_click_accept:
                acceptHex();
                break;
            case R.id.fragment_clickaccept:
                acceptHex();
                break;
            case R.id.fragment_click_send:
                sendHex();
                break;
            case R.id.fragment_clicksend:
                sendHex();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void getHex() {
        FragmentMessage.mAccept = !FragmentMessage.mAccept;
        acceptHex();
        FragmentMessage.mSendHex = !FragmentMessage.mSendHex;
        sendHex();
        mSendNumber.setText("S: " + FragmentMessage.mSendsNumber);
        mReceiveTv.setText("R: " + ImplantFragment.mAcceptNumber);
    }

    private void acceptHex() {

        if (mAccept) {
            mClickAcceptImage.setBackground(setDrawable(R.drawable.circle_click_false));
            mAccept = false;
        } else {
            mClickAcceptImage.setBackground(setDrawable(R.drawable.circle_click_true));
            mAccept = true;
        }
        if (mIsConnectInternet)
            sendHandler(0x05, "Accept");
        if (ImplantFragment.mIsAllConnectService)
            sendHandler(0x05, "Accept");
    }

    private void sendHex() {
        /*if (isOnLongClick){
            toast("发送中不能更改状态");
            return;
        }*/
        if (mSendHex) {
            mClickSendImage.setBackground(setDrawable(R.drawable.circle_click_false));
            mSendET = null;
            mEditTextSend.setVisibility(View.VISIBLE);
            mEditTextSendHex.setVisibility(View.GONE);
            mEditTextSend.setText(changeHexString(false, mEditTextSendHex.getText().toString()));
            mSendET = mEditTextSend;
            mSendHex = false;
        } else {
            mClickSendImage.setBackground(setDrawable(R.drawable.circle_click_true));
            mSendET = null;
            mEditTextSend.setVisibility(View.GONE);
            mEditTextSendHex.setVisibility(View.VISIBLE);
            mEditTextSendHex.setText(changeHexString(true, mEditTextSend.getText().toString()));
            mSendET = mEditTextSendHex;
            mSendHex = true;
        }
        if (mIsConnectInternet)
            sendHandler(0x05, "SendHex");
        if (ImplantFragment.mIsAllConnectService)
            sendHandler(0x05, "SendHex");
    }

    private void startServiceHint(int function, String errorMessage) {
        if (function == 3 && mServiceError) {
            return;
        }
        try {
            mSingMessage.setServiceManage(function);
            mServiceBuilder = new CommonDialog.Builder(getContext());
            mServiceBuilder.setView(R.layout.service_vessel).fullWidth().create().show();
            ServiceHint mServiceHint = mServiceBuilder.getView(R.id.service);
            if (function == 1)
                mServiceHint.setErrorMessage(errorMessage);
            mServiceHint.setServiceHintCallback(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置图片
    private Drawable setDrawable(int drawable) {
        Resources resources;
        try {
            resources = getActivity().getBaseContext().getResources();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return resources.getDrawable(drawable);
    }

    private boolean sign = true;

    private void initRecyclerState() {
        int i;
        if (versions()) {
            i = 14;
        } else {
            i = 8;
        }
        if (mDatas.size() >= i && sign) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setStackFromEnd(true);
            mRecyclerView.setLayoutManager(layoutManager);
            sign = false;
            Log.d("TAG_CONNEECT", "改变状态...");
        }
    }

    private void updateRecycler(String data) {
        mDatas.add(new Resou(mIp, data));
        initRecyclerState();
        mRecyclerView.setAdapter(
                new RecyclerAdapter(getContext(), mDatas));
    }

    @Override
    public void stopWindows() {
        mServiceBuilder.dismiss();
    }

    @Override
    public void forceExit(int function) {
        if (function == 2) {
            //强制退出
            mServiceBuilder.dismiss();
            mIsConnectInternet = false;
            sendHandler(0x09, null);
        } else if (function == 4) {
            //重置模块,即断开服务器
            mServiceBuilder.dismiss();
            startServiceHint(3, "");
            mSingMessage.setServiceProgressBarEvolve(0, 650);
            sendHandler(0x03, null);
        }
    }

    private class RecyclerAdapter extends RecyclerCommonAdapter<Resou> {

        RecyclerAdapter(Context context, List<Resou> list) {
            super(context, list, new MulitiTypeSupport<Resou>() {
                @Override
                public int getLayoutId(Resou item) {
                    return R.layout.item_chat_friend;
                }
            });
        }

        @Override
        protected void convert(ViewHolder holder, Resou item, int position, ItemClickListener itemClickListener) {
            holder.setText(R.id.chat_text, item.getmStr());
        }
    }

    /***
     * 判断是否在Android6.0以上
     * ***/
    private Boolean versions() {
        //Build.VERSION_CODES.M   Android6.0 的代码号
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }


    //当fragment add 与hide时，会回调下面方法
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            getHex();
            sendHandler(0x06, mReceiveTv);
            if (ImplantFragment.mIsAllConnectService) {
                mConnectInternetTv.setVisibility(View.INVISIBLE);
            }
        }
    }
}
