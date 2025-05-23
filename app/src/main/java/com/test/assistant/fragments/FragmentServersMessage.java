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

import com.test.assistant.ConnectHcServers;
import com.test.assistant.R;
import com.test.assistant.commonAdapter.ItemClickListener;
import com.test.assistant.commonAdapter.MulitiTypeSupport;
import com.test.assistant.commonAdapter.RecyclerCommonAdapter;
import com.test.assistant.commonAdapter.ViewHolder;
import com.test.assistant.recyclerAdapter.Resou;
import com.test.assistant.singleton.SingMessage;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import static com.test.connectservicelibrary.connectInternet.ToolClass.changeHexString;

public class FragmentServersMessage extends Fragments {

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

    private FragmentSingMessage mFragmentSingMessage = FragmentSingMessage.getFragmentSingMessage();

    private SingMessage mSingMessage = SingMessage.getSingMessage();

    private boolean mIsPortNull = true;

    static boolean mSendHex = false;
    static boolean mAccept = false;

    public static int mSendsNumber = 0;

    static boolean mConnectModule = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_message, container, false);
        ViewUtils.inject(view, this);
        initAll();
        return view;
    }

    private void initAll() {
        initHandler();
        initView();
        initRecycler();
        initHex();
    }

    private void initHandler() {
        mFragmentSingMessage.setReceiveServerMessageHandler(mExecuteHandler);
    }

    private void initView() {
        mConnectInternetTv.setVisibility(View.INVISIBLE);
        sendServerHandler(0x06, mReceiveTv);
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
    }

    //处理从ConnectServers发过来的数据
    private Handler mExecuteHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0x00:
                    updateRecycler(msg.obj.toString());//接收的数据
                    break;
                case 0x04:
                    setIsOnLine(msg.obj.toString());//改变在线状态
                    break;
            }
            return false;
        }
    });

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

    //点击发送事件
    @SuppressLint("SetTextI18n")
    @OnClick(R.id.btn_send)
    private void sendClick(View view) {
        sendServerHandler(0x00, mSendET.getText().toString());
        int number = mSendET.getText().toString().length();
        if (mSendHex)
            number = number / 3;
        else
            number = changeHexString(true, mSendET.getText().toString()).length() / 3;
        mSendsNumber += number;
        mSendNumber.setText("S: " + mSendsNumber);
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

    private void updateRecycler(String data) {
        mDatas.add(new Resou("", data));
        initRecyclerState();
        mRecyclerView.setAdapter(
                new RecyclerAdapter(getContext(), mDatas));
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

    /***
     * 判断是否在Android6.0以上
     * ***/
    private Boolean versions() {
        //Build.VERSION_CODES.M   Android6.0 的代码号
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    //设置在线断线状态
    private void setIsOnLine(String state) {

        if (state.equals("connected")) {
            //图片在drawable文件夹下
            mConnectModule = true;
            mImageViewCircle.setBackground(setDrawable(R.drawable.circle1));
            mCircleLight.setTextColor(Color.parseColor("#7E7E7E"));
            mCircleLight.setText(" 已连接");
            mIsPortNull = false;
            String service = "已连接上服务器：";
            if (mSingMessage.mServiceIp.equals("120.25.163.9"))
                service = "已连接上汇承官方服务器：";
            String string = "\n服务器的IP是：" + ConnectHcServers.mJsonsRootBean.getAddress() + "\n" + "服务器的端口号是: " + ConnectHcServers.mJsonsRootBean.getPort();
            updateRecycler(service + string);
        } else {
            //图片在drawable文件夹下
            mConnectModule = false;
            mImageViewCircle.setBackground(setDrawable(R.drawable.circle2));
            mCircleLight.setTextColor(Color.parseColor("#AE071B"));
            mCircleLight.setText(" 断线了..");

            if (mIsPortNull) {
                updateRecycler("连接失败，端口不存在，可能已经被服务器回收,请再次申请服务器资源");
            }
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

    private void getHex() {
        FragmentServersMessage.mAccept = !FragmentServersMessage.mAccept;
        acceptHex();
        FragmentServersMessage.mSendHex = !FragmentServersMessage.mSendHex;
        sendHex();
    }

    private void acceptHex() {
        if (mAccept) {
            mClickAcceptImage.setBackground(setDrawable(R.drawable.circle_click_false));
            mAccept = false;
        } else {
            mClickAcceptImage.setBackground(setDrawable(R.drawable.circle_click_true));
            mAccept = true;
        }
        sendServerHandler(0x05, mAccept);
    }

    private void sendHex() {
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
        sendServerHandler(0x04, mSendHex);
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {//fragment显示
            getHex();
            sendServerHandler(0x06, mReceiveTv);
            mSendNumber.setText("S: " + FragmentServersMessage.mSendsNumber);
        }
    }
}
