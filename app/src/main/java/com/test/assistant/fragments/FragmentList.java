package com.test.assistant.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.test.assistant.R;
import com.test.assistant.commonAdapter.ItemClickListener;
import com.test.assistant.dialog.CommonDialog;
import com.test.assistant.fragments.threadPool.GetModuleDate;
import com.test.assistant.fragments.threadPool.HCThreadPool;
import com.test.assistant.fragments.threadPool.ModuleMessage;
import com.test.assistant.fragments.threadPool.PostTest;
import com.test.assistant.fragments.threadPool.ThreadCallBack;
import com.test.assistant.recyclerAdapter.FragmentListAdapter;
import com.test.assistant.recyclerAdapter.Resou;
import com.test.assistant.storage.DataMemory;
import com.test.baselibrary.ioc.CheckNet;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;
import com.test.connectservicelibrary.connectInternet.ConnectInternetManage;
import com.test.connectservicelibrary.connectInternet.JsonsRootBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FragmentList extends Fragments {

    @ViewById(R.id.fragment_list_recycler)
    private RecyclerView mRecyclerView;

    @ViewById(R.id.fragment_list_ip)
    private TextView mServiceIpTv;

    @ViewById(R.id.fragment_list_port)
    private TextView mServicePortTv;

    @ViewById(R.id.swipe_refresh_layout)
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @ViewById(R.id.fragment_list_scan)
    private FrameLayout mScanFragment;

    @ViewById(R.id.fragment_list_group)
    private LinearLayout mButtonGroup;

    private FragmentManage mFragmentManage;
    private final int mFragmentId = 32117;

    private LocalReceiver mLocalReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;

    private List<Resou> mDatas = new ArrayList<>();

    private Handler mListenerHandler = new Handler();

    private FragmentListAdapter mListAdapter;

    private ConnectInternetManage mConnectInternetManage;//连接服务器的管理类

    private DataMemory mDataMemory;

    private boolean mSetServiceMess = true;

    private final String mServiceIP = "120.25.163.9";

    public static boolean mConnectWork = false;//是否在配置模块中

    private int mSucceedNumber = 0, mErrorNumber = 0;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_list, container, false);
        ViewUtils.inject(view, this);

        initAll();//初始化所有

        testServersPost();//测试服务器端口

        updateRecycler();//更新recycler

        setClickListener();//设置点击事件

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initAll() {
        FragmentSingMessage.getFragmentSingMessage().setReceiveFragConnectHandler(mServiceHandler);
        initFragments();
        initBroadcast();
        initService();
        initRefresh();
        initRecycler();
    }

    private void initFragments() {
        mFragmentManage = new FragmentManage(R.id.fragment_list_scan);
        mFragmentManage.initFragment(mFragmentId, new FragmentScan(), getActivity().getSupportFragmentManager().beginTransaction());
    }

    private void initRefresh() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {//设置刷新监听器
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
                refresh();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initService() {
        mConnectInternetManage = new ConnectInternetManage(getContext(),
                mServiceIP, mServiceHandler);
        mDataMemory = new DataMemory(Objects.requireNonNull(getContext()));
        mServiceIpTv.setText(mDataMemory.getServiceIp());
    }

    private void initRecycler() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        //设置数据
        mListAdapter = new FragmentListAdapter(getContext(), mDatas, R.layout.fragment_item);
        mRecyclerView.setAdapter(mListAdapter);
    }

    private void initData() {
        //mDatas = FragmentSingMessage.getFragmentSingMessage().getModelList();
        /*mDatas.add(new Resou("192.168.1.121","111",true));
        mDatas.add(new Resou("192.168.1.120","222",true));
        mDatas.add(new Resou("192.168.1.118","333",true));
        mDatas.add(new Resou("192.168.1.125","444",true));
        mDatas.add(new Resou("192.168.1.126","555",true));
        mDatas.add(new Resou("192.168.1.127","666",true));
        mDatas.add(new Resou("192.168.1.128","777",true));
        mDatas.add(new Resou("192.168.1.12","888",true));
        mDatas.add(new Resou("192.168.1.13","999",true));
        mDatas.add(new Resou("192.168.1.14","000",true));
        mDatas.add(new Resou("192.168.1.11","aaa",true));
        mDatas.add(new Resou("192.168.1.10","bbb",true));
        mDatas.add(new Resou("192.168.1.9","ccc",true));*/
        mDatas.addAll(FragmentSingMessage.getFragmentSingMessage().getModelList());
        if (mDatas.size() == 0)
            toast("没有搜索到模块，按住屏幕中间下拉，再次刷新试试");
    }

    private void initBroadcast() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.test.huichengwifi.STOP_BROADCAST");
        mIntentFilter.addAction("com.test.huichengwifi.ContinueSetup");
        mLocalReceiver = new LocalReceiver();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        mLocalBroadcastManager.registerReceiver(mLocalReceiver, mIntentFilter);
    }

    private void testServersPost() {
        PostTest.TextPostCallback textPostCallback = new PostTest.TextPostCallback() {
            @Override
            public void state(boolean state) {
                if (state) {
                    mServicePortTv.setText(mDataMemory.getServicePort());
                    mButtonGroup.setVisibility(View.GONE);
                } else {
                    toast(mDataMemory.getServicePort() + "端口已失效");
                }
            }
        };
        if (mDataMemory.getServiceIp() != null && mDataMemory.getServicePort() != null)
            new PostTest(mDataMemory.getServiceIp(), mDataMemory.getServicePort(), textPostCallback, getActivity());
    }

    //刷新所执行的方法
    private void refresh() {
        if (mConnectWork) {
            toast("配置中，不可以刷新..");
            return;
        }
        mDatas.clear();
        mSwipeRefreshLayout.setVisibility(View.GONE);
        mScanFragment.setVisibility(View.VISIBLE);
        mFragmentManage.delete(mFragmentId, getActivity().getSupportFragmentManager().beginTransaction());
        mFragmentManage.initFragment(mFragmentId, new FragmentScan(), getActivity().getSupportFragmentManager().beginTransaction());
    }

    @CheckNet
    @OnClick(R.id.fragment_list_get_service)
    private void getService() {
        if (mConnectWork) {
            toast("请等配置完成再进行其他操作...");
            return;
        }
        toast("稍等..");
        mConnectInternetManage.getServiceMessage();
    }

    private boolean isConnectService = false;//经常发生切回此界面就开始配网，所以再加一个判断

    @CheckNet
    @OnClick(R.id.fragment_list_connect)
    private void connectService() {

        if (mConnectWork) {
            toast("请等配置完成再进行其他操作...");
            return;
        }
        if (mServiceIpTv.getText().toString().length() < "1.1.1.1".length() || mServicePortTv.getText().toString().isEmpty()) {
            toast("请先申请服务器，或输入正确的IP和端口");
            return;
        }

        log("开始配置多个模块的服务器");
        int clickNumber = 0;
        for (Resou mData : mDatas) {
            if (mData.getClickBoolean())
                clickNumber++;
        }
        if (clickNumber == 0) {
            toast("请选择想要配置的模块");
            return;
        }
        List<String> list = new ArrayList<>();
        list.add(mServiceIpTv.getText().toString());
        list.add(mServicePortTv.getText().toString());
        sendHandler(0x10, list);
        isConnectService = true;
    }

    @OnClick(R.id.fragment_list_disconnect)
    private void disconnectService() {
        if (mConnectWork) {
            toast("请等配置完成再进行其他操作...");
            return;
        }
        final List<String> list = new ArrayList<>();
        List<Resou> models = new ArrayList<>();
        for (Resou mData : mDatas) {
            if (mData.getClickBoolean()) {
                list.add(mData.getmIP());
                models.add(mData);
                mData.setState(1);
            }
        }
        mDatas = models;
        if (models.size() == 0) {
            toast("请选择想要配置的模块");
            return;
        }
        initRecycler();
        toast("开始配置模块所选的模块，预计需要" + ((models.size() / 5 + 1) * 2) + "秒钟");
        new HCThreadPool(list, mThreadCallBack, getContext());
        mConnectWork = true;
        mSetServiceMess = false;
    }

    @OnClick(R.id.fragment_list_more)
    private void more() {
        if (mConnectWork) {
            toast("请等配置完成再进行其他操作...");
            return;
        }
        Map<String, String> map = mDataMemory.getServersMap();//*
        if (map == null || map.size() < 2) {
            toast("没有记录更多的端口，请申请服务器");
            return;
        }
        CommonDialog.Builder builder = new CommonDialog.Builder(getContext());
        builder.setView(R.layout.fragment_more_vessel).fullWidth().create().show();
        MoreServersMessage setupMessage = builder.getView(R.id.fragment_more_id);
        setupMessage.setBuilder(builder).setServersMessage(mServiceIpTv, mServicePortTv);
    }

    private Handler mServiceHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 0x01) {
                JsonsRootBean jsonsRootBean = (JsonsRootBean) msg.obj;
                if (jsonsRootBean.getErrCode() != 0) {
                    toast("服务器申请失败...");
                    return false;
                }
                mServiceIpTv.setText(mServiceIP);
                mServicePortTv.setText(String.valueOf(jsonsRootBean.getIntPort()));
            }
            if (msg.what == 0x04 && isConnectService) {
                if (msg.obj.toString().equals("connected")) {
                    setServiceMessage();
                } else if (msg.obj.toString().equals("delConnect")) {
                    toast("无效IP或端口");
                }
            }
            if (msg.what == 0x08) {
                toast(msg.obj.toString());
            }
            return false;
        }
    });

    //配置多个模块的回调
    private ThreadCallBack mThreadCallBack = new ThreadCallBack() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void accomplish() {
            log("所有任务完成...");
            getMessageWindows();//弹出配置完成的选择窗口
        }

        @Override
        public void setOk(String ip) {
            setUpdateRecyclerView(ip, 3);//更新，配置成功信息
            ++mSucceedNumber;
        }

        @Override
        public void setError(String ip) {
            setUpdateRecyclerView(ip, 4);//更新，配置失败信息
            ++mErrorNumber;
        }

        @Override
        public void dispose(String ip) {
            setUpdateRecyclerView(ip, 2);//更新，配置中消息
        }
    };

    private ModuleMessage mModuleMessage = new ModuleMessage() {
        @Override
        public void getMessage(String ip, String post, int position) {
            mDatas.get(position).setServersIP(ip);
            mDatas.get(position).setServersPost(post);
            mListAdapter.notifyDataSetChanged();
        }

        @Override
        public void error(String e, int position) {
            log("e:" + e);
            mDatas.get(position).setServersIP("获取失败(1.6以下版本请用AT指令查询)");
            mDatas.get(position).setServersPost("获取失败");
            mListAdapter.notifyDataSetChanged();
        }
    };


    private void setServiceMessage() {
        final List<String> list = new ArrayList<>();
        List<Resou> models = new ArrayList<>();
        for (Resou mData : mDatas) {
            if (mData.getClickBoolean()) {
                mData.setState(1);
                models.add(mData);
                list.add(mData.getmIP());
            }
        }
        mDatas = models;
        initRecycler();
        mSetServiceMess = true;
        mConnectWork = true;
        String names = "";
        toast("开始配置模块所选的模块，预计需要" + ((models.size() / 5 + 1) * 2) + "秒钟");
        for (Resou model : models) {
            names += "名称: " + model.getmStr() + "\n" + "IP: " + model.getmIP() + "\n" + "\n";
        }
        mDataMemory.saveServicePort(mServiceIpTv.getText().toString(),
                mServicePortTv.getText().toString(), names);//保存IP和端口号
        new HCThreadPool(list, mThreadCallBack, mServiceIpTv.getText().toString(),
                mServicePortTv.getText().toString(), getContext());
    }

    private void updateRecycler() {
        mRecyclerView.setAdapter(mListAdapter);
        setClickListener();
    }

    private void setClickListener() {
        mListenerHandler.post(new Runnable() {
            @Override
            public void run() {
                showListData();
            }
        });
    }

    //配置完成后，弹出选择窗口
    private void getMessageWindows() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (mSetServiceMess) {
                    intent = new Intent("com.test.huichengwifi.STOP_LIST");
                    String string = mServiceIpTv.getText().toString().equals(mServiceIP) ? "汇承官方服务器" : mServiceIpTv.getText().toString();
                    intent.putExtra("message", string + ":" + mServicePortTv.getText().toString());
                } else {
                    intent = new Intent("com.test.huichengwifi.DISSERVICE");
                }
                mConnectWork = false;
                isConnectService = false;
                CommonDialog.Builder builder = new CommonDialog.Builder(getContext());
                builder.setView(R.layout.fragment_exit_vessel).fullWidth().create().show();
                SetupMessage setupMessage = builder.getView(R.id.setup_message);
                setupMessage.setBuilder(builder).hideHint(mSetServiceMess).setMessageTv(mSucceedNumber, mErrorNumber).setMessageIntent(intent);
            }
        });
    }

    private void setUpdateRecyclerView(String ip, int state) {
        for (Resou mData : mDatas) {
            if (mData.getmIP().equals(ip)) {
                mData.setState(state);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    //item 点击事件
    private void showListData() {
        mRecyclerView.setAdapter(mListAdapter);
        //设置点击事件
        mListAdapter.setOnItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(final int position, View view) {
                switch (view.getId()) {
                    case R.id.fragment_item_message:
                        getModuleMessage(position);
                        break;
                    case R.id.fragment_item_unfold:
                        getModuleMessage(position);
                        mDatas.get(position).setHintLinearLayout(View.VISIBLE);
                        mListAdapter.notifyDataSetChanged();
                        break;
                    case R.id.fragment_item_hide:
                        mDatas.get(position).setHintLinearLayout(View.GONE);
                        mListAdapter.notifyDataSetChanged();
                        break;
                    case R.id.fragment_item_fill:
                        if (!(mDatas.get(position).getServersPost().equals("没有配置到服务器") ||
                                mDatas.get(position).getServersPost().equals("获取失败")))
                            mServicePortTv.setText(mDatas.get(position).getServersPost());
                        else
                            toast("此端口号不存在");
                        break;
                    default:
                        mDatas.get(position).setClick();
                        mListAdapter.notifyDataSetChanged();
                        setButtonGroup();
                }
            }
        });
    }

    private class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("com.test.huichengwifi.STOP_BROADCAST")) {
                //停止扫描，打开FragmentList界面
                //mFragmentManage.delete(mFragmentId, getActivity().getSupportFragmentManager().beginTransaction());
                mScanFragment.setVisibility(View.GONE);
                mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                initData();
                updateRecycler();
            }
            if (intent.getAction() != null && intent.getAction().equals("com.test.huichengwifi.ContinueSetup")) {
                toast("按在屏幕中间并下拉，可以刷新");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
            }
        }
    }

    private void getModuleMessage(final int position) {
        mDatas.get(position).setServersPost("");
        mDatas.get(position).setServersIP("查询中...");
        mListAdapter.notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new GetModuleDate().postJson(mDatas.get(position).getmIP(), position, mModuleMessage, getActivity());
            }
        }).start();
    }

    private void setButtonGroup() {
        if (mServiceIpTv.getText().toString().length() < "1.1.1.1".length() ||
                mServicePortTv.getText().toString().isEmpty()) {
            mButtonGroup.setVisibility(View.VISIBLE);
            return;
        }
        for (Resou mData : mDatas) {
            if (mData.getClickBoolean()) {
                mButtonGroup.setVisibility(View.VISIBLE);
                return;
            }
        }
        mButtonGroup.setVisibility(View.GONE);
    }

    private void log(String log) {
        Log.d("AppRunFragmentList", log);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatas.clear();
        FragmentSingMessage.getFragmentSingMessage().clearModelList();
        mLocalBroadcastManager.unregisterReceiver(mLocalReceiver);
    }
}
