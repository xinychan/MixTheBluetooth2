package com.test.assistant.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.test.assistant.R;
import com.test.assistant.fragments.udpBroadcast.ScanBroadcast;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;

import java.util.Objects;

public class FragmentScan extends Fragments {
    private Handler mTimeHandler = new Handler();
    private LocalBroadcastManager mLocalBroadcastManager;
    @ViewById(R.id.radar_view)
    private MyRadarView myRadarView;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_scan, container, false);
        ViewUtils.inject(view, this);
        initAll();
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initAll() {
        initUdpBroadcast();
        initView();
    }

    private void initUdpBroadcast() {
        new ScanBroadcast();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initView() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(Objects.requireNonNull(getContext()));
        mTimeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                myRadarView.start();
            }
        }, 50);
        mTimeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                myRadarView.setDropNumber(5);
            }
        }, 200);
        mTimeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, 1200);
    }


    private void stopScan() {
        myRadarView.stop();
        Intent intent = new Intent("com.test.huichengwifi.STOP_BROADCAST");
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimeHandler.removeMessages(0);
    }
}
