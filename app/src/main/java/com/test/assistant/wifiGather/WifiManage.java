package com.test.assistant.wifiGather;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.test.assistant.storage.WifiMessage;
import com.test.assistant.wifiGather.network.SetPrivateNetwork;
import com.test.assistant.wifiGather.tool.SortList;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.WIFI_SERVICE;

/*   这个类WifiManage在初始化后（包括接口,广播），调用connect(final String targetSsid)方法即可，会自动连接4次.(最后一次是反射)
 *    密码保存在WifiMessage类里，当局域网通信时，在打开数据的情况下，会导致数据发送失败，需要配置专网（指定局域网通信）
 *    所以实现SetPrivateNetwork.setNetwork(final Context context,final SetNetworkListener listener,final int againNumber)
 *    指定网络
 *
 *    void initBroadcast()//连接前先初始化广播
 *
 *    boolean connect(final String targetSsid)//调用此方法连接WiFi即可，targetSsid:目标WiFi的名字
 *
 *    void setOnScanWifiListener(OnScanWifiListener listener)
 *    interface OnScanWifiListener//实现扫描接口
 *
 *    void setOnScanListener(WifiListener listener)
 *    interface WifiListener//实现连接信息返回的接口（成功，失败，密码错误的回调）
 *
 *    void unBroadcast()结束时记得注销广播;
 * */
public class WifiManage {

    private boolean debug;//是否打印日志

    private Context mContext;
    private WifiManager mWifiManager;

    private MyBroadcast myBroadcast;
    private WifiMessage mWifiMessage;

    private WifiListener mWifiListener;//接口回调
    private OnScanWifiListener mOnScanWifiListener;//扫描结果接口回调

    private int mBroadcastReceptionNumber = 0;//每次连接信息都是两次，所以添加过滤

    private Handler mTimeHandler;

    private String mTargetNetwork = null;//需要连接的目标网络

    private int mConventionalMethodNumber = 0;//重用常规方法连接的次数，2次
    private int mSystemMethodNumber = 0;//使用反射方法，1次

    private String mNowRouter = null;

    private enum State {Work, Leisure}//工作与空闲模式

    private State mState = State.Leisure;

    private Handler mHandler;//延时检测WiFi连接状态

    private Map<String, ScanResult> mHistoryMap = new HashMap<>();//缓存所有扫描过的列表数据


    public WifiManage(Context context, boolean debug) {
        this.debug = debug;
        this.mContext = context;
        init();
    }


    //调用此方法连接WiFi即可
    boolean connect(final String targetSsid) {//调用此方法，只是初步连接，后续根据是否连接上目标，采取另外方法
        if (targetSsid == null) {
            mState = State.Leisure;
            return true;
        }
        Log.e("AppRun" + getClass().getSimpleName(), "调用一次");
        mTargetNetwork = targetSsid;

        //如果所需连接的目标是已连接的了，则直接回调返回
        if (mNowRouter != null && mNowRouter.equals(targetSsid)) {
            mWifiListener.connectWifi(mNowRouter, "");
            log("系统已经帮我们连接好，现在回去发广播..");
            return true;
        }

        mState = State.Work;//设置为工作状态
        sendHandler(0x01, 4000);//4秒后检测是否还在连接WiFi，防止卡死

        WifiConfiguration wifiConfiguration = isExsits(targetSsid);
        if (wifiConfiguration != null) {//查看手机是否连接过此网络，是则直接连接
            //mWifiManager.disconnect();
            mWifiManager.enableNetwork(wifiConfiguration.networkId, true);
            return true;
        }

        final String enc = getEnc(targetSsid);
        if ("[ESS]".equals(enc)) {//开放网络
            connectWifi(targetSsid, "", enc);
            return true;
        } else {// 加密类型为WPA
            if (mWifiMessage == null)
                mWifiMessage = new WifiMessage(mContext);
            if (!mWifiMessage.getWifiData(targetSsid).equals("")) {
                connectWifi(targetSsid, mWifiMessage.getWifiData(targetSsid), enc);
                return true;
            }
            mHandler.removeMessages(0x01);//取消延时检查，准备退出
            return false;//表示目标有密码，但是没保存密码
        }
    }

    //获取当前的路由器，有可能为空或unknown ssid
    String getNowRouter() {
        return mNowRouter;
    }


    //wifi扫描（刷新）
    public boolean scanWifi() {
        if (!isOpenGPS(mContext)) {//防止某些手机在给了位置权限后，还不放行
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
            builder.setTitle("提示")
                    .setMessage("请前往打开手机的位置权限!")
                    .setCancelable(false)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            ((Activity) mContext).startActivityForResult(intent, 10);
                        }
                    }).show();
            return false;
        }
        return mWifiManager.startScan();//返回刷新结果
    }

    //返回现有的WiFi列表
    public List<ScanResult> getWifiList() {
        return mWifiManager.getScanResults();
    }


    //初始化广播（通过接收系统广播来获取连接与刷新的返回）
    public void initBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        myBroadcast = new MyBroadcast();
        mContext.registerReceiver(myBroadcast, filter);
    }

    //广播注销
    public void unBroadcast() {
        mContext.unregisterReceiver(myBroadcast);
    }

    //连接结果的接口回调
    void setOnScanListener(WifiListener listener) {
        mWifiListener = listener;
    }

    public interface WifiListener {
        void connectWifi(String name, String mac);//回调连接上的WiFi 名字与物理地址，物理地址有可能空

        void connectError(String name);//连接失败，密码错误;

        void connectionFail(String name);//连接失败，重复连接不上
    }

    //刷新结果接口监听
    public void setOnScanWifiListener(OnScanWifiListener listener) {
        this.mOnScanWifiListener = listener;
    }

    public interface OnScanWifiListener {
        void scanCallback(List<ScanResult> list);//回调给出Wifi列表
    }


    //判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
    public boolean isOpenGPS(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // GPS定位
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 网络服务定位
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps || network;
    }

    //判断是否为5G频段
    boolean is5GBandLimits() {
        int freq = 0;
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {
            freq = wifiInfo.getFrequency();
        } else {
            ScanResult scanResult = getScanResult(mNowRouter);
            if (scanResult != null) {
                freq = scanResult.frequency;
            }
        }
        return freq > 4900 && freq < 5900;
    }


    /**
     * 连接wifi
     *
     * @param targetSsid wifi的SSID
     * @param targetPsd  密码
     * @param enc        加密类型
     */
    @SuppressLint("WifiManagerLeak")
    private void connectWifi(final String targetSsid, final String targetPsd, final String enc) {

        log("进入常规方法连接");
        if (getScanResult(targetSsid) != null && mConventionalMethodNumber <= 1) {
            if (!addNetwork(CreateWifiInfo(getScanResult(targetSsid), targetPsd))) {
                log("常规方法连接失败!");
                mTimeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AlternateConnection(targetSsid, targetPsd, enc);
                    }
                }, 1000);
            }
        } else {
            if (mWifiMessage == null)
                mWifiMessage = new WifiMessage(mContext);
            try {
                isConnect(getScanResult(targetSsid), mWifiMessage.getWifiData(targetSsid));
            } catch (Exception e) {
                e.printStackTrace();
            }
            //AlternateConnection(targetSsid,targetPsd,enc);
        }
    }

    private void AlternateConnection(String targetSsid, String targetPsd, String enc) {
        log("进入备用连接");
        // 1、注意热点和密码均包含引号，此处需要需要转义引号
        String ssid = "\"" + targetSsid + "\"";
        String psd = "\"" + targetPsd + "\"";

        //2、配置wifi信息
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = ssid;
        if ("[ESS]".equals(enc)) {//开放网络
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else {// 加密类型为WPA
            conf.preSharedKey = psd;
        }
        log("选择完成，enc -> " + enc);

        //3、链接wifi
        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        int Network = mWifiManager.addNetwork(conf);
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals(ssid)) {
                mWifiManager.disconnect();
                log("断开WiFi..");
                mWifiManager.enableNetwork(i.networkId, true);
                boolean a = mWifiManager.reconnect();
                log("wifiManager.reconnect: " + a);
                break;
            }
        }
        log("wifiManager.addNetwork: " + Network);
    }


    //通过BSSID获取WiFi的NAME，获取失败返回BSSID
    private String getWifiName(String bSSID) {
        String name;
        try {
            name = mHistoryMap.get(bSSID).SSID;
            if (name != null) {
                return name;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (ScanResult scanResult : mWifiManager.getScanResults()) {
            if (scanResult.BSSID.equals(bSSID)) {
                return scanResult.SSID;
            }
        }
        return bSSID;
    }

    private void setHistoryList(List<ScanResult> scanResults) {

        if (scanResults.size() == 0) {
            return;
        }

        for (ScanResult scanResult : scanResults) {
            mHistoryMap.put(scanResult.BSSID, scanResult);
        }
        log("缓存一次,size-> " + mHistoryMap.size());

    }

    //获取加密类型
    String getEnc(String ssid) {
        ScanResult scanResult = getScanResult(ssid);
        if (scanResult != null) {
            return scanResult.capabilities;
        }
        return null;
    }

    //获取ScanResult
    private ScanResult getScanResult(String name) {
        for (ScanResult scanResult : mWifiManager.getScanResults()) {
            if (name.equals(scanResult.SSID)) {
                return scanResult;
            }
        }

        for (String key : mHistoryMap.keySet()) {
            try {
                if (mHistoryMap.get(key).SSID.equals(name)) {
                    return mHistoryMap.get(key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    //初始化WiFiManager
    private void init() {
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(WIFI_SERVICE);
            if (!openWifi())
                Toast.makeText(mContext, "打开WiFi失败，请前往手机系统打开WiFi..", Toast.LENGTH_SHORT).show();
            if (mWifiManager.getConnectionInfo().getSSID() != null)
                mNowRouter = mWifiManager.getConnectionInfo().getSSID().substring(1, mWifiManager.getConnectionInfo().getSSID().length() - 1);
            log("目前连接为：" + mNowRouter);
        }
        initHandler();
        setHistoryList(mWifiManager.getScanResults());
        mTimeHandler = new Handler();
        mState = State.Leisure;
    }

    private void initHandler() {//防止连接超时导致卡死的延时检测
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0x01:
                        connectState();
                        break;
                    case 0x02:
                        testConnection();
                        break;
                }
                return false;
            }
        });
    }

    private void testConnection() {
        if (mState == State.Work) {
            log("再次超时");
            reconnectNetwork();
            sendHandler(0x02, 4000);
        }
    }

    private void connectState() {
        log("延时检测：" + mConventionalMethodNumber);
        if (mState == State.Work && mConventionalMethodNumber == 0) {
            log("连接超时..");
            mWifiManager.disconnect();
            reconnectNetwork();
            sendHandler(0x02, 4000);
        } else if (mState == State.Work) {
            sendHandler(0x02, 4500);
        }
    }

    private void sendHandler(int what, int time) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessageDelayed(message, time);
    }


    private class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null)
                return;
            switch (intent.getAction()) {
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION://刷新回调
                    if (mOnScanWifiListener != null) {
                        setHistoryList(mWifiManager.getScanResults());
                        mOnScanWifiListener.scanCallback(new SortList(mWifiManager.getScanResults()).sorList());
                    }
                    if (mNowRouter.equals("unknown ssid")) {
                        mNowRouter = mWifiManager.getConnectionInfo().getSSID().substring(1, mWifiManager.getConnectionInfo().getSSID().length() - 1);
                    }
                    break;

                case WifiManager.NETWORK_STATE_CHANGED_ACTION://连接上WiFi
                    NetworkInfo info = intent
                            .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                        ++mBroadcastReceptionNumber;
                        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                        if (mWifiListener != null && mBroadcastReceptionNumber % 2 == 0) {//过滤奇数广播
                            log("连接上网络：" + getWifiName(wifiInfo.getBSSID()));
                            SetPrivateNetwork.resetNetwork(context);//取消网络配置
                            mNowRouter = getWifiName(wifiInfo.getBSSID());
                            try {
                                if (mTargetNetwork != null && getWifiName(wifiInfo.getSSID()) != null && getWifiName(wifiInfo.getBSSID()).equals(mTargetNetwork)) {
                                    mState = State.Leisure;
                                    mWifiListener.connectWifi(getWifiName(wifiInfo.getBSSID()), wifiInfo.getBSSID());
                                    restoration();
                                } else {
                                    reconnectNetwork();
                                }
                            } catch (Exception e) {
                                reconnectNetwork();
                                log("莫名其妙的错误:" + e.toString());
                                e.printStackTrace();
                            }
                        }
                    } else if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                        //网络断开
                        mNowRouter = "unknown ssid";
                    }
                    break;

                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION://密码错误
                    int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 123);
                    if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
                        if (mWifiListener != null) {
                            mState = State.Leisure;
                            mWifiListener.connectError(mTargetNetwork);
                            restoration();
                        }
                    }
                    break;
            }
        }
    }

    //重新连接目标网络
    private void reconnectNetwork() {
        if (mTargetNetwork == null)
            return;
        if (mConventionalMethodNumber < 2) {
            ++mConventionalMethodNumber;
            log("进入第" + mConventionalMethodNumber + "次重连");
            if (mWifiMessage == null)
                mWifiMessage = new WifiMessage(mContext);
            connectWifi(mTargetNetwork, mWifiMessage.getWifiData(mTargetNetwork), getEnc(mTargetNetwork));
        } else if (mSystemMethodNumber < 1) {
            log("尝试调用反射连接");
            ++mSystemMethodNumber;
            try {
                connectByNetworkId(mWifiManager, isExsits(mTargetNetwork).networkId);
            } catch (Exception e) {
                e.printStackTrace();
                mState = State.Leisure;
                if (mWifiListener != null)
                    mWifiListener.connectionFail(mTargetNetwork);
                restoration();
            }
        } else {
            mState = State.Leisure;
            if (mWifiListener != null)
                mWifiListener.connectionFail(mTargetNetwork);
            restoration();
        }
    }

    //复位所有目标
    private void restoration() {
        if (mState == State.Leisure) {
            mConventionalMethodNumber = 0;
            mSystemMethodNumber = 0;
            mTargetNetwork = null;
            mHandler.removeMessages(0x01);
            mHandler.removeMessages(0x02);
            log("复位清除缓存...");
        }
    }


    //有密码连接
    private void isConnect(final ScanResult scanResult, final String pass) {

        log("进入老方法，配置密码网络");
        WifiConnectUtils.WifiCipherType type = null;
        if (scanResult.capabilities.toUpperCase().contains("WPA")) {
            type = WifiConnectUtils.WifiCipherType.WIFICIPHER_WPA;
        } else if (scanResult.capabilities.toUpperCase()
                .contains("WEP")) {
            type = WifiConnectUtils.WifiCipherType.WIFICIPHER_WEP;
        } else {
            type = WifiConnectUtils.WifiCipherType.WIFICIPHER_NOPASS;
        }

        final WifiConnectUtils.WifiCipherType finalType = type;
        mWifiManager.disconnect();//网络断开
        boolean isConnect = connect(scanResult.SSID, pass, finalType);
        if (isConnect) {
            mWifiMessage.saveData(scanResult.SSID, pass);
        }
    }


    private boolean connect(String SSID, String Password, WifiConnectUtils.WifiCipherType Type) {

        // 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
        while (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            try {
                // 为了避免程序一直while循环，让它睡个100毫秒在检测……
                Thread.currentThread();
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        if (SSID == null || Password == null || SSID.equals("")) {
            Log.e(this.getClass().getName(),
                    "addNetwork() ## nullpointer error!");
            return false;
        }
        WifiConfiguration wifiConfig = createWifiInfo(SSID, Password, Type);
        // wifi的配置信息
        if (wifiConfig == null) {
            return false;
        }
        // 查看以前是否也配置过这个网络
        WifiConfiguration tempConfig = this.isExsits(SSID);
        if (tempConfig != null) {

//            forgetNetwork(mWifiManager,tempConfig.networkId);
//            Boolean b = mWifiManager.saveConfiguration();
//            Log.e("TAG_saveConfiguration","saveConfiguration的返回:"+b);
            log("删除网络的返回:" + mWifiManager.removeNetwork(tempConfig.networkId));
//            Log.e("TAG_saveConfiguration",
//                    "保存网络返回:"+mWifiManager.saveConfiguration());
        }
        // 添加一个新的网络描述为一组配置的网络。
        int netID = mWifiManager.addNetwork(wifiConfig);
        Log.d("WifiListActivity", "wifi的netID为：" + netID);
        // 断开连接
        mWifiManager.disconnect();
        // 重新连接
        if (netID == -1)
            netID = mWifiManager.addNetwork(wifiConfig);
        Log.d("WifiListActivity", "Wifi的重新连接netID为：" + netID);

        // 设置为true,使其他的连接断开
        boolean mConnectConfig = mWifiManager.enableNetwork(netID, true);
        mWifiManager.reconnect();
        return mConnectConfig;
    }


    private WifiConfiguration createWifiInfo(String SSID, String Password,
                                             WifiConnectUtils.WifiCipherType Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (Type == WifiConnectUtils.WifiCipherType.WIFICIPHER_NOPASS) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
            //自己后面加的
            config.preSharedKey = null;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedAuthAlgorithms.clear();
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        }
        if (Type == WifiConnectUtils.WifiCipherType.WIFICIPHER_WEP) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
            //加入修改的
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

        }
        if (Type == WifiConnectUtils.WifiCipherType.WIFICIPHER_WPA) {
            // 修改之后配置
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            //加入修改的
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        } else {
            return null;
        }
        return config;
    }


    // 这是一个老方法，只能连接首次连接的WiFi
    private WifiConfiguration CreateWifiInfo(ScanResult scan, String Password) {
        WifiConfiguration config = new WifiConfiguration();
        config.hiddenSSID = false;
        config.status = WifiConfiguration.Status.ENABLED;

        if (scan.capabilities.contains("WEP")) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);

            config.SSID = "\"" + scan.SSID + "\"";

            config.wepTxKeyIndex = 0;
            config.wepKeys[0] = Password;
            // config.preSharedKey = "\"" + SHARED_KEY + "\"";
        } else if (scan.capabilities.contains("PSK")) {
            //
            config.SSID = "\"" + scan.SSID + "\"";
            config.preSharedKey = "\"" + Password + "\"";
        } else if (scan.capabilities.contains("EAP")) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.SSID = "\"" + scan.SSID + "\"";
            config.preSharedKey = "\"" + Password + "\"";
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            config.SSID = "\"" + scan.SSID + "\"";
            // config.BSSID = info.mac;
            config.preSharedKey = null;
            //
        }
        return config;
    }

    private boolean addNetwork(WifiConfiguration wcg) {
        if (wcg == null) {
            return false;
        }
        // receiverDhcp = new ReceiverDhcp(ctx, mWifiManager, this,
        // wlanHandler);
        // ctx.registerReceiver(receiverDhcp, new
        // IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b = mWifiManager.enableNetwork(wcgID, true);
        mWifiManager.saveConfiguration();
        log("addNetwork:" + b);
        return b;
    }


    // 查看以前是否也配置过这个网络
    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    //通过反射调用系统的保存的WiFi密码
    private void connectByNetworkId(WifiManager manager, int networkId) {
        log("通过反射连接");
        mWifiManager.disconnect();
        try {
            @SuppressLint("PrivateApi") Method connect = manager.getClass().getDeclaredMethod("connect",
                    int.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
            connect.setAccessible(true);
            connect.invoke(manager, networkId, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 打开wifi
    private boolean openWifi() {
        boolean bRet = true;
        if (!mWifiManager.isWifiEnabled()) {
            bRet = mWifiManager.setWifiEnabled(true);
        }
        return bRet;
    }

    private void log(String log) {
        if (debug)
            Log.d("AppRun" + getClass().getSimpleName(), log);

    }

}
