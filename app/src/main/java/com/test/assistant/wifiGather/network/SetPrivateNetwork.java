package com.test.assistant.wifiGather.network;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

public class SetPrivateNetwork {

    private static Handler mTimeHandler = new Handler();
    private static boolean mIsReset = true;

    public static synchronized void setNetwork(final Context context, final SetNetworkListener listener, final int againNumber) {

        if (Build.VERSION.SDK_INT >= 21) {

            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            /*if (!isMobile(connectivityManager)){
                log("没有蜂窝网，不需要配网");
                listener.succeed();
                return;
            }*/

            NetworkRequest.Builder builder = new NetworkRequest.Builder();

            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

            builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

            NetworkRequest request = builder.build();

            ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {

                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    boolean state = false;

                    if (Build.VERSION.SDK_INT >= 23) {
                        try {
                            state = connectivityManager.bindProcessToNetwork(network);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            state = ConnectivityManager.setProcessDefaultNetwork(network);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    connectivityManager.unregisterNetworkCallback(this);
                    if (state) {
                        mIsReset = true;
                        listener.succeed();
                    } else if (againNumber > 0) {
                        mTimeHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setNetwork(context, listener, againNumber - 1);
                                log("第一次配置失败，现在开始第二次配置,mIsAgainNumber -> " + (againNumber - 1));
                            }
                        }, 1000);
                    } else {
                        listener.defeated();
                    }
                }
            };
            connectivityManager.requestNetwork(request, callback);
        } else {
            listener.succeed();
        }
    }


    public static synchronized void resetNetwork(Context context) {

        if (!mIsReset) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 21) {

            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkRequest.Builder builder = new NetworkRequest.Builder();

            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

            builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

            NetworkRequest request = builder.build();

            if (Build.VERSION.SDK_INT >= 23) {
                connectivityManager.bindProcessToNetwork(null);
            } else {
                ConnectivityManager.setProcessDefaultNetwork(null);
            }
            ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {

                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    boolean state;
                    if (Build.VERSION.SDK_INT >= 23) {
                        state = connectivityManager.bindProcessToNetwork(null);
                    } else {
                        state = ConnectivityManager.setProcessDefaultNetwork(null);
                    }
                    connectivityManager.unregisterNetworkCallback(this);
                    log("重置网络结果：" + state);
                    mIsReset = false;
                }
            };
            connectivityManager.requestNetwork(request, callback);
        }
    }

    //判断移动数据是否打开(貌似有问题)
    private static boolean isMobile(ConnectivityManager connectivityManager) {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public interface SetNetworkListener {
        void succeed();

        void defeated();
    }

    private static void log(String log) {
        Log.d("AppRunSetPrivateNetwork", log);
    }

}
