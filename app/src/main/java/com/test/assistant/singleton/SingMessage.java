package com.test.assistant.singleton;

import android.content.Context;
import android.widget.ProgressBar;

import com.test.assistant.dialog.CommonDialog;
import com.test.assistant.storage.DataMemory;


public class SingMessage {

    private SingMessage() {
    }

    private static SingMessage singMessage = new SingMessage();

    public static SingMessage getSingMessage() {
        return singMessage;
    }

    private int mServiceManage = 0;
    private ProgressBar mProgressBar;
    private int mProgressBarMax = 0, mProgressBarMin = 0;


    private DataMemory mDataMemory;
    private Context mContext;
    private CommonDialog.Builder mConnectService;

    public String mServiceIp;
    public String mServicePort;

    public void setDataMemory(DataMemory dataMemory) {
        mDataMemory = dataMemory;
    }

    public DataMemory getDataMemory() {
        return mDataMemory;
    }

    public void setConnectService(CommonDialog.Builder connectService) {
        this.mConnectService = connectService;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public CommonDialog.Builder getConnectService() {
        return mConnectService;
    }


    public void setServiceManage(int serviceManage) {
        this.mServiceManage = serviceManage;
    }

    public int getServiceManage() {
        return mServiceManage;
    }

    public void setServiceProgressBar(ProgressBar mProgressBar) {
        this.mProgressBar = mProgressBar;
    }

    public void setServiceProgressBarMax(int progressBarMax) {
        this.mProgressBarMax = progressBarMax;
    }

    public void setServiceProgressBarNull() {
        isServiceProgressBar = false;
        if (mServiceProgressBarThread != null) {
            mServiceProgressBarThread.interrupt();
            mServiceProgressBarThread = null;
        }
    }

    public void setServiceProgressBarEvolve(int min, int max) {
        this.mProgressBarMin = min;
        mProgressBar.setProgress(min);
        this.mProgressBarMax = max;
        if (min == 0) {
            isServiceProgressBar = true;
            startServiceProgressBarThread();
        }
    }

    public int getProgressBarMax() {
        return mProgressBarMax;
    }

    private boolean isServiceProgressBar = false;
    private Thread mServiceProgressBarThread;

    private void startServiceProgressBarThread() {
        if (mServiceProgressBarThread != null) {
            return;
        }
        mServiceProgressBarThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isServiceProgressBar) {
                    try {
                        Thread.sleep(35);
                        if (mProgressBar.getProgress() < mProgressBarMax) {
                            mProgressBar.incrementProgressBy(5);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mServiceProgressBarThread.start();
    }

}
