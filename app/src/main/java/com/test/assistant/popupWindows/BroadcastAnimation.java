package com.test.assistant.popupWindows;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.test.assistant.R;
import com.test.assistant.view.BroadcastView;
import com.test.assistant.view.ChangeColorTextView;
import com.test.assistant.view.SlewingRingView;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;

public class BroadcastAnimation extends LinearLayout {

    @ViewById(R.id.broadcast_view)
    private BroadcastView mBroadcastView;

    @ViewById(R.id.broadcast_anim_hint)
    private TextView mHint;

    @ViewById(R.id.broadcast_anim_linear)
    private LinearLayout mLinear;

    @ViewById(R.id.broadcast_module_loading)
    private SlewingRingView mModuleLoding;

    @ViewById(R.id.broadcast_router_loading)
    private SlewingRingView mRouterLoading;//加载图标

    @ViewById(R.id.broadcast_module_text)
    private ChangeColorTextView mModuleText;

    @ViewById(R.id.broadcast_router_text)
    private ChangeColorTextView mRouterText;

    @ViewById(R.id.broadcast_anim_self_linear)
    private LinearLayout mSelfLinear;//退出配网的LinearLayout

    private String mRouter, mModule;

    private int mTime = 4500;

    private ValueAnimator animator;

    private enum State {Leisure, Broadcast, SetModule}//空闲，广播，配置模块

    private State mState = State.Leisure;

    private boolean mIsEndOfScan = true;


    public BroadcastAnimation(Context context) {
        this(context, null);
    }

    public BroadcastAnimation(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BroadcastAnimation(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.broadcast_animation_menu, this);
        ViewUtils.inject(this);
        initData();
        initView();
    }

    private void initData() {

        //showManualUI();
    }

    private void initView() {
        mRouterLoading.setVisibility(GONE);
        mLinear.setVisibility(GONE);
        mBroadcastView.setVisibility(VISIBLE);
    }

    public BroadcastAnimation startAnim() {//开启广播动画
        start();
        return this;
    }

    public void stopView() {
        if (mListener != null) {
            mListener.end();
            mListener = null;
        }
    }

    public BroadcastAnimation setTime(int time) {
        if (time == 0)
            mTime = 4500;
        else
            mTime = 8500;
        return this;
    }

    @SuppressLint("SetTextI18n")
    public void succeedModule() {//成功配置好模块
        mModuleLoding.stop();//模块图标停下
        mRouterLoading.setVisibility(VISIBLE);//路由图标展示
        mRouterLoading.start();//路由图标旋转
        mRouterText.setText("连接" + mRouter);//设置路由名字信息
        mRouterText.start();//开启字体颜色变化
    }

    public void succeedRouter() {//成功连接上路由
        mRouterLoading.stop();//图标停下
        mState = State.Leisure;
    }

    @SuppressLint("SetTextI18n")
    public void startLongBroadcast() {//连接上路由才发的广播，其他时候尽量不用
        this.mTime = 8500;
        initView();
        mHint.setText("正在搜索模块：" + mModule);
        startAnim();
    }

    @SuppressLint("SetTextI18n")
    public void error(String name) {

        mState = State.Leisure;
        showManualUI();
        mState = State.SetModule;

        if (name.equals(mModule)) {
            mModuleLoding.error();
            mModuleText.setText("配置出错:" + name);
            mHint.setText("建议在手机系统里删除" + name + "，然后用APP再次连接试试");
        } else {
            mRouterLoading.error();
            mRouterText.setText("连接失败，建议退出重连路由");
        }
    }


    public void setConnectMessage(String router, String module) {
        this.mRouter = router;
        this.mModule = module;
    }

    @SuppressLint("SetTextI18n")
    public void setHint(String hint) {
        mHint.setText("正在搜索" + hint + "模块，请稍等..");
    }

    @OnClick(R.id.broadcast_anim_manual)
    private void scan(View view) {
        if (mListener == null)
            return;
        Toast.makeText(view.getContext(), "请重新配网..", Toast.LENGTH_SHORT).show();
        mListener.end();
    }

    private void showManualUI() {
        mSelfLinear.setVisibility(VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void setModule() {

        mState = State.SetModule;
        if (animator != null) {
            animator.end();
            mListener.scanEnd();
        }

        initCustomView();

        mListener.setModule();//回调通知配置模块

        mHint.setText("正在配置模块" + mModule + "中，请稍等...");
        mBroadcastView.setVisibility(GONE);
        mLinear.setVisibility(VISIBLE);
        mModuleText.setText("配置" + mModule);
        mModuleText.start();
        mModuleLoding.start();
    }

    private void initCustomView() {
        mModuleLoding.init();
        mRouterLoading.init();
        mModuleText.init();
        mRouterText.init();
        mRouterLoading.setVisibility(GONE);
    }

    private void start() {
        mIsEndOfScan = true;
        if (mHint.getText().toString().trim().equals("搜索不到模块"))
            mHint.setText("再次搜索模块，请稍等..");

        if (mTime == 4500)
            animator = ObjectAnimator.ofFloat(0, 2500);
        else
            animator = ObjectAnimator.ofFloat(0, 5500);
        animator.setInterpolator(new MyInterpolator());
        animator.setDuration(mTime);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float number = (float) animation.getAnimatedValue();
                mBroadcastView.setPhaseValue(number);
                if (number == 2500 && mListener != null) {
                    mListener.scanEnd();
                    if (mIsEndOfScan) {
                        mIsEndOfScan = false;
                        setModule();
                    }
                } else if (number == 5500 && mListener != null) {

                    showManualUI();

                    mHint.setText("搜索不到模块");
                    mListener.scanEnd();
                }
                if (mTime == 8500) {
                    mTime = 4500;
                }
            }
        });
        animator.start();
    }


    private OnAnimListener mListener;

    public void setOnAnimListener(OnAnimListener listener) {
        this.mListener = listener;
    }

    public interface OnAnimListener {
        void end();

        void scan();

        void setModule();

        void scanEnd();
    }

    class MyInterpolator implements TimeInterpolator {
        @Override
        public float getInterpolation(float input) {
            return input;
        }
    }

}
