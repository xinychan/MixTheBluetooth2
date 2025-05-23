package com.test.assistant.fragments.threadPool;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/*
 * 说明:自定义的发送线程池：最大发送线程数量，5个，当任务量超过最大发送数量时，将会进入排队状态，
 *       等待所有当前线程完成任务后，会从mModuleIps中取出数据继续任务，结束时回调接口mThreadCallBack.accomplish()
 * */
public class HCThreadPool {

    private Thread mThread1, mThread2, mThread3, mThread4, mThread5;
    private int mWork;
    private List<String> mModuleIps;
    private List<Thread> mThreads;
    private ThreadCallBack mThreadCallBack;
    private int mAccomplishNumber = 0;
    private Context mContext;
    private boolean mIsSetServiceMess = false;
    private String mIp;
    private String mPort;

    /*
     * 模块恢复为普通模式
     * moduleIps:需要配网的IP集合
     * threadCallBack：接口回调，当所有的发送完，将会调用接口里的唯一方法
     * context：this上下文
     * */
    public HCThreadPool(List<String> moduleIps, ThreadCallBack threadCallBack, Context context) {
        this.mWork = moduleIps.size();
        this.mModuleIps = moduleIps;
        this.mThreadCallBack = threadCallBack;
        this.mContext = context;
        initData();
        build();
    }

    /*
     * 模块配置到服务器模式
     * moduleIps:需要配网的IP集合
     * threadCallBack：接口回调，当所有的发送完，将会调用接口里的唯一方法
     * context：this上下文
     * */
    public HCThreadPool(List<String> moduleIps, ThreadCallBack threadCallBack, String Ip, String port, Context context) {
        this.mWork = moduleIps.size();
        this.mModuleIps = moduleIps;
        this.mThreadCallBack = threadCallBack;
        this.mContext = context;
        this.mIp = Ip;
        this.mPort = port;
        mIsSetServiceMess = true;
        initData();
        build();
    }

    private void initData() {
        mThreads = new ArrayList<>();
        mThreads.add(mThread1);
        mThreads.add(mThread2);
        mThreads.add(mThread3);
        mThreads.add(mThread4);
        mThreads.add(mThread5);
    }

    private void build() {
        int number = 0;
        for (Thread mThread : mThreads) {
            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    work();
                }
            });
            mThread.start();
            ++number;
            if (number == mWork) {
                break;
            }
        }
    }

    private void work() {
        String str = "";
        synchronized (this) {
            if (mModuleIps.size() <= 0) {
                log("任务做完，退出");
                ++mAccomplishNumber;
                if (mWork >= 5) {
                    if (mAccomplishNumber == 5)
                        mThreadCallBack.accomplish();
                } else {
                    if (mAccomplishNumber == mWork)
                        mThreadCallBack.accomplish();
                }
                exit();
                return;
            }
            str = mModuleIps.get(0);
            log(mModuleIps.get(0) + " 开始执行");
            mThreadCallBack.dispose(mModuleIps.get(0));
            mModuleIps.remove(0);
        }
        if (!mIsSetServiceMess)
            new SendDataModule().postJson(2, "", str, "", messageInform, (Activity) mContext);
        else
            new SendDataModule().postJson(1, mIp, str, mPort, messageInform, (Activity) mContext);
        log(str + "执行完成");
        work();
    }

    private void log(String str) {
        Log.d("AppRunThreadPool", str);
    }

    private void exit() {
        for (Thread mThread : mThreads) {
            if (mThread != null)
                mThread.interrupt();
        }
    }

    private MessageInform messageInform = new MessageInform() {
        @Override
        public void serviceCallback(boolean result, String data) {

        }

        @Override
        public void sendModuleCallback(boolean result, String pattern) {
            if (result)
                mThreadCallBack.setOk(pattern);
            else
                mThreadCallBack.setError(pattern);
        }
    };

}
