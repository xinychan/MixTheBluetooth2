package com.test.assistant.fragments;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.assistant.ImplantFragment;
import com.test.assistant.R;
import com.test.assistant.commonAdapter.ItemClickListener;
import com.test.assistant.commonAdapter.MulitiTypeSupport;
import com.test.assistant.commonAdapter.RecyclerCommonAdapter;
import com.test.assistant.commonAdapter.ViewHolder;
import com.test.assistant.dialog.CommonDialog;
import com.test.assistant.recyclerAdapter.Resou;
import com.test.assistant.storage.FragmentButtonData;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.OnLongClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;
import com.test.connectservicelibrary.connectInternet.ToolClass;

import java.util.ArrayList;
import java.util.List;

import static com.test.connectservicelibrary.connectInternet.ToolClass.changeHexString;


public class FragmentCustom extends Fragments {

    @ViewById(R.id.first_group)
    private LinearLayout mFirstGroupLinearLayout;
    @ViewById(R.id.second_group)
    private LinearLayout mSecondGroupLinearLayout;
    @ViewById(R.id.three_group)
    private LinearLayout mThreeGroupLinearLayout;
    @ViewById(R.id.fourth_group)
    private LinearLayout mFourthGroupLinearLayout;

    @ViewById(R.id.custom_recycler)
    private RecyclerView mRecyclerView;

    @ViewById(R.id.custom_circlelight)
    private ImageView mImageViewCircle;

    @ViewById(R.id.custom_lighttip)
    private TextView mCircleLight;


    @ViewById(R.id.fragment_custom_getdata)
    private TextView mReceiveTv;

    @ViewById(R.id.fragment_custom_senddata)
    private TextView mSendNumber;

    @ViewById(R.id.fragment_custom_click_accept)
    private ImageView mClickAcceptImage;

    @ViewById(R.id.fragment_custom_click_send)
    private ImageView mClickSendImage;

    private List<Resou> mDatas = new ArrayList<>();

    private View mView;

    private static String mIp = "192.168.4.1";

    private LocalReceiver mLocalReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;

    private FragmentButtonData mFragmentButtonData;

    private int[] mButtonGroupIds = {R.id.zero_first, R.id.zero_second, R.id.zero_three,
            R.id.first_first, R.id.first_second, R.id.first_three, R.id.first_fourth,
            R.id.second_first, R.id.second_second, R.id.second_three, R.id.second_fourth,
            R.id.three_first, R.id.three_second, R.id.three_three, R.id.three_fourth,
            R.id.fourth_first, R.id.fourth_second, R.id.fourth_three, R.id.fourth_fourth};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_custom, container, false);
        ViewUtils.inject(view, this);
        initAll(view);
        return view;
    }

    private void initAll(View v) {
        mView = v;
        mFragmentButtonData = new FragmentButtonData(v.getContext());
        FragmentSingMessage.getFragmentSingMessage().setReceiveFragCustomHandler(mReceiveFragCustomHandler);
        initBroadcast(v);
        initButton(v);
        initLinearLayout();
        initRecycler();
        initConnectStart();
    }

    private void initConnectStart() {
        if (FragmentMessage.mConnectModule)
            setIsOnLine("connected");
        else
            setIsOnLine("dis");
    }

    private void initLinearLayout() {
        int groupNumber = mFragmentButtonData.getButtonGroupNumber();
        switch (groupNumber) {
            case 0:
                mFirstGroupLinearLayout.setVisibility(View.GONE);
                mSecondGroupLinearLayout.setVisibility(View.GONE);
                mThreeGroupLinearLayout.setVisibility(View.GONE);
                mFourthGroupLinearLayout.setVisibility(View.GONE);
                break;
            case 1:
                mSecondGroupLinearLayout.setVisibility(View.GONE);
                mThreeGroupLinearLayout.setVisibility(View.GONE);
                mFourthGroupLinearLayout.setVisibility(View.GONE);
                break;
            case 2:
                mThreeGroupLinearLayout.setVisibility(View.GONE);
                mFourthGroupLinearLayout.setVisibility(View.GONE);
                break;
            case 3:
                mFourthGroupLinearLayout.setVisibility(View.GONE);
        }
    }

    private void initBroadcast(View view) {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.test.huichengwifi.ADD_BUTTON_GROUP");
        mIntentFilter.addAction("com.test.huichengwifi.DELETE_BUTTON_GROUP");
        mLocalReceiver = new LocalReceiver();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(view.getContext());
        mLocalBroadcastManager.registerReceiver(mLocalReceiver, mIntentFilter);
    }

    private void initButton(View view) {
        FragmentSingMessage.getFragmentSingMessage().setFragmentButtonData(mFragmentButtonData);
        String name;
        for (int mButtonGroupId : mButtonGroupIds) {
            if ((name = mFragmentButtonData.getButtonName(mButtonGroupId)) != null)
                setButtonData(view, mButtonGroupId, name);
            else
                setButtonData(view, mButtonGroupId, "长按设置");
            name = null;
        }
    }

    private void initRecycler() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        //设置数据
        mRecyclerView.setAdapter(new RecyclerAdapter(this.getActivity(), mDatas));
    }

    private Handler mReceiveFragCustomHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 0x00) {
                updateRecycler(msg.obj.toString());//更新Recycler
            }
            if (msg.what == 0x03) {
                updateReceiveNumber(msg.obj.toString());//更新接收到的数据
            }
            if (msg.what == 0x04) {
                setIsOnLine(msg.obj.toString());//根据返回的值设置是否在线
            }
            return false;
        }
    });

    //如果这里想发送真正的hex数据，则不需要转换了，屏蔽if else字段即可，
    // 目前的规则是，把正常数据转成16进制，再发送出去，但是有问题，接收方是拿到正常进制的数据，并没有真正转换
    private void sendData(String data) {
        if (data == null) {
            toast("此按键是空值...");
            return;
        }
        try {
            if (FragmentMessage.mSendHex) {
                byte[] bytes = ToolClass.hexString2ByteArray(data);//需要把里面的try catch给去了
                FragmentMessage.mSendsNumber += bytes.length;
            } else {
                FragmentMessage.mSendsNumber += changeHexString(true, data).length() / 3;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Scale", "错误，修改");
            if (FragmentMessage.mSendHex) {
                data = changeHexString(true, data);
                FragmentMessage.mSendsNumber += data.length() / 3;
            } else {
                FragmentMessage.mSendsNumber += changeHexString(true, data).length() / 3;
            }
        }

        /*if (FragmentMessage.mSendHex) {
            data = changeHexString(true, data);
            FragmentMessage.mSendsNumber += data.length()/3;
        }else {
            FragmentMessage.mSendsNumber += changeHexString(true, data).length()/3;
        }*/

        //FragmentMessage.mSendsNumber += changeHexString(true, data).length()/3;
        Log.d("Scale", "mSendHex is " + FragmentMessage.mSendHex);
        sendHandler(0x00, data);
        mSendNumber.setText("S: " + FragmentMessage.mSendsNumber);
    }

    private void updateRecycler(String data) {
        mDatas.add(new Resou(mIp, data));
        initRecyclerState();
        mRecyclerView.setAdapter(
                new RecyclerAdapter(getContext(), mDatas));
    }

    private void setIsOnLine(String state) {

        if (state.equals("connected")) {
            //图片在drawable文件夹下
            mImageViewCircle.setBackground(setDrawable(R.drawable.circle1));
            mCircleLight.setTextColor(Color.parseColor("#7E7E7E"));
            mCircleLight.setText(" 已连接");
        } else {
            //图片在drawable文件夹下
            mImageViewCircle.setBackground(setDrawable(R.drawable.circle2));
            mCircleLight.setTextColor(Color.parseColor("#AE071B"));
            mCircleLight.setText(" 断线了..");
        }
    }

    @OnClick(R.id.set_button)
    private void setButton(View view) {
        CommonDialog.Builder buttonManageBuilder = new CommonDialog.Builder(view.getContext());
        buttonManageBuilder.setView(R.layout.button_manage_vessel).fullWidth().create().show();
        FragmentSingMessage.getFragmentSingMessage().setButtonManageBuilder(buttonManageBuilder);
    }

    @OnClick({R.id.zero_first, R.id.zero_second, R.id.zero_three})
    private void zeroGroupButton(View view) {
        String data = mFragmentButtonData.getButtonData(view.getId());
        sendData(data);
    }

    @OnClick({R.id.first_first, R.id.first_second, R.id.first_three, R.id.first_fourth})
    private void firstGroupButton(View view) {
        String data = mFragmentButtonData.getButtonData(view.getId());
        sendData(data);
    }

    @OnClick({R.id.second_first, R.id.second_second, R.id.second_three, R.id.second_fourth})
    private void secondGroupButton(View view) {
        String data = mFragmentButtonData.getButtonData(view.getId());
        sendData(data);
    }

    @OnClick({R.id.three_first, R.id.three_second, R.id.three_three, R.id.three_fourth})
    private void threeGroupButton(View view) {
        String data = mFragmentButtonData.getButtonData(view.getId());
        sendData(data);
    }

    @OnClick({R.id.fourth_first, R.id.fourth_second, R.id.fourth_three, R.id.fourth_fourth})
    private void fourthGroupButton(View view) {
        String data = mFragmentButtonData.getButtonData(view.getId());
        sendData(data);
    }

    @OnLongClick({R.id.zero_first, R.id.zero_second, R.id.zero_three,
            R.id.first_first, R.id.first_second, R.id.first_three, R.id.first_fourth,
            R.id.second_first, R.id.second_second, R.id.second_three, R.id.second_fourth,
            R.id.three_first, R.id.three_second, R.id.three_three, R.id.three_fourth,
            R.id.fourth_first, R.id.fourth_second, R.id.fourth_three, R.id.fourth_fourth})
    private void groupButtonLongOnClick(View view) {
        FragmentSingMessage.getFragmentSingMessage().setButtonId(view.getId());
        CommonDialog.Builder addButtonBuilder = new CommonDialog.Builder(view.getContext());
        addButtonBuilder.setView(R.layout.add_button_vessel).fullWidth().create().show();
        FragmentSingMessage.getFragmentSingMessage().setAddButtonBuilder(addButtonBuilder);
    }

    //清除Recycler
    @OnClick(R.id.delete_recycler)
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
    @OnClick({R.id.fragment_custom_clickaccept, R.id.fragment_custom_click_accept,
            R.id.fragment_custom_clicksend, R.id.fragment_custom_click_send})
    private void sendAcceptPatternClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_custom_click_accept:
                acceptHex();
                break;
            case R.id.fragment_custom_clickaccept:
                acceptHex();
                break;
            case R.id.fragment_custom_click_send:
                sendHex();
                break;
            case R.id.fragment_custom_clicksend:
                sendHex();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void initHex() {
        FragmentMessage.mAccept = !FragmentMessage.mAccept;
        acceptHex();
        FragmentMessage.mSendHex = !FragmentMessage.mSendHex;
        sendHex();
        mSendNumber.setText("S: " + FragmentMessage.mSendsNumber);
        mReceiveTv.setText("R: " + ImplantFragment.mAcceptNumber);
    }

    private void initInternetHex() {
        sendHandler(0x06, mReceiveTv);
    }

    private void acceptHex() {

        if (FragmentMessage.mAccept) {
            mClickAcceptImage.setBackground(setDrawable(R.drawable.circle_click_false));
            FragmentMessage.mAccept = false;
        } else {
            mClickAcceptImage.setBackground(setDrawable(R.drawable.circle_click_true));
            FragmentMessage.mAccept = true;
        }
        if (FragmentMessage.mIsConnectInternet)
            sendHandler(0x05, "Accept");
        if (ImplantFragment.mIsAllConnectService)
            sendHandler(0x05, "Accept");
    }

    private void sendHex() {
        if (FragmentMessage.mSendHex) {
            mClickSendImage.setBackground(setDrawable(R.drawable.circle_click_false));
            FragmentMessage.mSendHex = false;
        } else {
            mClickSendImage.setBackground(setDrawable(R.drawable.circle_click_true));
            FragmentMessage.mSendHex = true;
        }
        if (FragmentMessage.mIsConnectInternet)
            sendHandler(0x05, "SendHex");
        if (ImplantFragment.mIsAllConnectService)
            sendHandler(0x05, "SendHex");
    }

    @SuppressLint("SetTextI18n")
    private void updateReceiveNumber(String data) {
        mReceiveTv.setText("R: " + data);
        ImplantFragment.mAcceptNumber = Integer.parseInt(data);
    }

    private void setButtonData(View view, int id, String name) {
        Button btn = view.findViewById(id);
        btn.setText(name);
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

    private class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction() != null && intent.getAction().equals("com.test.huichengwifi.ADD_BUTTON_GROUP")) {
                int group = intent.getIntExtra("Update", 0);
                Log.d("AppRun", "group:" + group);
                switch (group) {
                    case -1:
                        toast("不能再隐藏了");
                        break;
                    case 0:
                        mFirstGroupLinearLayout.setVisibility(View.GONE);
                        mSecondGroupLinearLayout.setVisibility(View.GONE);
                        mThreeGroupLinearLayout.setVisibility(View.GONE);
                        mFourthGroupLinearLayout.setVisibility(View.GONE);
                        break;
                    case 1:
                        mFirstGroupLinearLayout.setVisibility(View.VISIBLE);
                        mSecondGroupLinearLayout.setVisibility(View.GONE);
                        mThreeGroupLinearLayout.setVisibility(View.GONE);
                        mFourthGroupLinearLayout.setVisibility(View.GONE);
                        break;
                    case 2:
                        mFirstGroupLinearLayout.setVisibility(View.VISIBLE);
                        mSecondGroupLinearLayout.setVisibility(View.VISIBLE);
                        mThreeGroupLinearLayout.setVisibility(View.GONE);
                        mFourthGroupLinearLayout.setVisibility(View.GONE);
                        break;
                    case 3:
                        mFirstGroupLinearLayout.setVisibility(View.VISIBLE);
                        mSecondGroupLinearLayout.setVisibility(View.VISIBLE);
                        mThreeGroupLinearLayout.setVisibility(View.VISIBLE);
                        mFourthGroupLinearLayout.setVisibility(View.GONE);
                        break;
                    case 4:
                        mFirstGroupLinearLayout.setVisibility(View.VISIBLE);
                        mSecondGroupLinearLayout.setVisibility(View.VISIBLE);
                        mThreeGroupLinearLayout.setVisibility(View.VISIBLE);
                        mFourthGroupLinearLayout.setVisibility(View.VISIBLE);
                        break;
                    default:
                        toast("已经达到上限");
                }
            }

            if (intent.getAction() != null && intent.getAction().equals("com.test.huichengwifi.DELETE_BUTTON_GROUP")) {
                initButton(mView);
            }
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

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            initConnectStart();
            initHex();
            initInternetHex();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mLocalReceiver);
    }
}
