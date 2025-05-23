package com.test.assistant;


import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.assistant.baseUse.DefaultNavigationBar;
import com.test.assistant.baseUse.FoldMenu;
import com.test.assistant.baseUse.GeidLayoutItemDecoration;
import com.test.assistant.commonAdapter.ItemClickListener;
import com.test.assistant.commonAdapter.ItemLongClickListener;
import com.test.assistant.dialog.CommonDialog;
import com.test.assistant.permission.PermissionUtil;
import com.test.assistant.popupWindows.ExplainMenu;
import com.test.assistant.recyclerAdapter.CategoryListAdapter;
import com.test.assistant.singleton.SingMessage;
import com.test.assistant.storage.DataMemory;
import com.test.assistant.wifiGather.Set25Module;
import com.test.assistant.wifiGather.WifiManage;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BasActivity {

    @ViewById(R.id.person_setTile)
    private TextView mRefreshBt;

    @ViewById(R.id.recycler_view)
    private RecyclerView mRecyclerView;

    @ViewById(R.id.main_list)
    private LinearLayout mListLinear;


    private List<ScanResult> mScanResults = new ArrayList<>();
    private CategoryListAdapter mListAdapter;
    private WifiManage mWifiManage;

    private Set25Module mSet25Module;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewUtils.inject(this);
        initAll();
        listListener();
    }

    private void initAll() {
        initPermission();//初始化权限
        initData();
        initView();
        initTitle();
        initSetModule();
        initScanListener();
    }

    private void initData() {
        mWifiManage = new WifiManage(this, true);
        mListAdapter = new CategoryListAdapter(this, mScanResults, R.layout.item_home);
        SingMessage.getSingMessage().setDataMemory(new DataMemory(this));
        mWifiManage.initBroadcast();
        mScanResults.addAll(mWifiManage.getWifiList());
        if (mScanResults.size() == 0)
            mRefreshBt.setText("点击扫描");
    }

    private void initView() {
        super.setContext(this);
        mListLinear.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = mRecyclerView.getLayoutParams();
                params.height = mListLinear.getHeight();
                mRecyclerView.setLayoutParams(params);
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mListAdapter);
        //绘制分隔线
        mRecyclerView.addItemDecoration(
                new GeidLayoutItemDecoration(this, R.drawable.item_dirver_01));
    }

    private void initSetModule() {
        mSet25Module = new Set25Module(mWifiManage, this, true);
    }

    private void listClick() {
        mRecyclerView.setAdapter(mListAdapter);
        mListAdapter.setOnItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                mSet25Module.setTargetModule(mScanResults.get(position).SSID);//点击配网
            }
        });
        mListAdapter.setOnItemLongClickListener(new ItemLongClickListener() {
            @Override
            public boolean onItemLongClick(int position) {
                mSet25Module.connectTarget(mScanResults.get(position).SSID);//长按直连
                return true;
            }
        });
    }

    private void listListener() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                listClick();
            }
        });
    }

    private void initScanListener() {
        mWifiManage.setOnScanWifiListener(new WifiManage.OnScanWifiListener() {
            @Override
            public void scanCallback(List<ScanResult> list) {
                mScanResults.clear();
                mScanResults.addAll(list);
                mListAdapter.notifyDataSetChanged();
                changeViewHeightAnimatorStart(mRecyclerView, mRecyclerView.getHeight(), mListLinear.getHeight());
                if (mScanResults.size() > 0)
                    mRefreshBt.setText(R.string.main_scan_button);
            }
        });
    }

    //设置头部
    private void initTitle() {
        new DefaultNavigationBar
                .Builder(this, (ViewGroup) findViewById(R.id.view_group))
                .setLeftText(this.getString(R.string.name_and_lv))
                .setLeftClickListener(null)
                .hideLeftIcon()
                .setRightIcon()
                .setFoldMenu(R.layout.title_fold_menu, new FoldMenu.OnItemClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.fold_menu_connect_service:
                                CommonDialog.Builder connectService = new CommonDialog.Builder(MainActivity.this);
                                connectService.setView(R.layout.connect_service_vessel).fullWidth().create().show();
                                SingMessage.getSingMessage().setConnectService(connectService);
                                SingMessage.getSingMessage().setContext(MainActivity.this);
                                break;

                            case R.id.fold_menu_about_me:
                                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                                break;

                            case R.id.fold_menu_hint:
                                startExplain();
                                break;
                        }
                    }
                })
                .builer();
    }

    @OnClick(R.id.person_setTile)
    private void refreshBt() {
        if (!mWifiManage.scanWifi()) {
            if (mWifiManage.isOpenGPS(this))
                toast("刷新次数达到上限，请两分钟后再尝试刷新。或调用系统WiFi尝试刷新");
        } else {
            toast("开始扫描周围WiFi");
            changeViewHeightAnimatorStart(mRecyclerView, mRecyclerView.getHeight(), 0);
        }
    }


    /**
     * 动态改变view的高度动画效果
     * 原理:动画改变view LayoutParams.height的值
     *
     * @param view        要进行高度改变动画的view
     * @param startHeight 动画前的view的高度
     * @param endHeight   动画后的view的高度
     */
    private static void changeViewHeightAnimatorStart(final View view, final int startHeight, final int endHeight) {
        if (view != null && startHeight >= 0 && endHeight >= 0) {
            ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
            animator.setDuration(800);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ViewGroup.LayoutParams params = view.getLayoutParams();
                    params.height = (int) animation.getAnimatedValue();
                    view.setLayoutParams(params);
                }
            });
            animator.start();
        }
    }

    private void startExplain() {
        CommonDialog.Builder builder = new CommonDialog.Builder(MainActivity.this);
        builder.setView(R.layout.explain_vessel).loadAnimation().create().show();
        ExplainMenu explainMenu = builder.getView(R.id.explain);
        explainMenu.setBuilder(builder);
    }

    private void initPermission() {
        PermissionUtil.requestEach(MainActivity.this, new PermissionUtil.OnPermissionListener() {
            @Override
            public void onSucceed() {
                //授权成功后打开wifi
                Log.d("AppRun", "申请成功");
            }

            @Override
            public void onFailed(boolean showAgain) {
                Log.d("AppRun", "失败");
            }
        }, PermissionUtil.LOCATION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWifiManage.unBroadcast();
    }
}