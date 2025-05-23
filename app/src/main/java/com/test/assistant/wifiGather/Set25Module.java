package com.test.assistant.wifiGather;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.test.assistant.ImplantFragment;
import com.test.assistant.R;
import com.test.assistant.dialog.CommonDialog;
import com.test.assistant.fragments.FragmentMessage;
import com.test.assistant.popupWindows.BroadcastAnimation;
import com.test.assistant.popupWindows.GetPassword;
import com.test.assistant.storage.WifiMessage;
import com.test.assistant.view.LoadingCircleView;
import com.test.assistant.wifiGather.network.SetPrivateNetwork;
import com.test.assistant.wifiGather.tool.TaskThread;
import com.test.assistant.wifiGather.udpScanTarget.ScanModuleBroadcast;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/*
 *  Set25Module(WifiManage wifiManage,Context context,boolean debug) 构造方法
 *  setTargetModule(String targetModule) 配网
 *  connectTarget(String targetModule)   直连网络
 *
 *  initListener() WiFi连接情况回调
 *  selectWorkMode() 每次连上WiFi进入工作模式选择的方法（工作模式：0：直连；1：配网，发数据；2：路由中搜索配置好的模块）
 *  startBroadcastAnim(final int time) 自定义View的运行 与 和用户的事件交互
 * */

public class Set25Module {

    private boolean debug;//是否打印日志

    private WifiManage mWifiManage;
    private Context mContext;
    private String mTargetModule;//目标模块
    private String mNowRouter;//当前路由（可能为空）
    private String mNowRouterPass;

    private int mState = 0;//(工作模式：0：直连；1：配网，发数据；2：路由中搜索配置好的模块)

    private boolean mIsScan = false; //是否处于UDP扫描中

    private Handler mTimeHandler = new Handler();

    private BroadcastAnimation mBroadcastAnimation;//广播扫描的界面
    private CommonDialog.Builder mAnimBuilder;
    private CommonDialog mLoadingDialog;//直连模式下的dialog
    private LoadingCircleView mLoadingCircleView;//直连模式下的动画View
    private ScanModuleBroadcast mBroadcast;//UDP广播

    public Set25Module(WifiManage wifiManage, Context context, boolean debug) {
        this.mContext = context;
        this.mWifiManage = wifiManage;
        this.debug = debug;
        initListener();
    }

    public void setTargetModule(String targetModule) {

        //确定连接的目标
        mTargetModule = targetModule;

        //1.检测当前网络，判断是否直连
        //  直连返回true 否则返回false
        if (unRouter(targetModule))
            return;

        //2.UDP广播搜索局域网 并根据返回扫描结果，
        //  调用连接startActivity(ip)方法
        //  或是 connectModule()连接配置模块
        //  startBroadcastAnim()广播扫描动画
        startBroadcastAnim(0);
        udpBroadcastScan(targetModule, 0);

    }

    public void connectTarget(String targetModule) {
        initLoadingView();
        mState = 0;
        mTargetModule = targetModule;
        log("强制直连");
        boolean b = mWifiManage.connect(targetModule);
        if (!b) {
            getPass(targetModule);
        }
    }

    //根据是否存在路由来判断是否连接模块
    private boolean unRouter(String targetModule) {
        mState = 0;
        //没有WiFi，直连
        if (mWifiManage.getNowRouter().equals("unknown ssid")) {

            initLoadingView();

            log("进入直连模式");
            boolean b = mWifiManage.connect(targetModule);
            if (!b) {
                getPass(targetModule);
            }
            return true;
        }

        //选择的模块已被连接，直接开启sock连接
        if (mWifiManage.getNowRouter().equals(targetModule)) {
            startActivity("192.168.4.1");
            return true;
        }
        log("取到的路由是" + mWifiManage.getNowRouter());
        return false;
    }

    //直连模式下的加载动画..
    private void initLoadingView() {
        mLoadingDialog = new CommonDialog.Builder(mContext)
                .setView(R.layout.connect_module_loading)
                .loadAnimation().setCancelable(false).fullWidth().create();
        mLoadingCircleView = mLoadingDialog.getView(R.id.loading_circle);
        mLoadingCircleView.start();
        mLoadingDialog.show();
    }

    //广播扫描局域网内模块，扫描到直接打开第二界面
    private void udpBroadcastScan(String targetModule, final int time) {
        //广播搜索模块
        log("进入广播搜索");
        mIsScan = true;//表示广播正在扫描

        if (time == 0)
            mBroadcast = new ScanModuleBroadcast(targetModule);
        else
            mBroadcast = new ScanModuleBroadcast(targetModule, time);
        ScanModuleBroadcast.OnUdpListener listener = new ScanModuleBroadcast.OnUdpListener() {
            @Override
            public void callback(final boolean state, final String ip) {
                log("广播回调state：" + state);
                mTimeHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (state)
                            startActivity(ip);
                        else
                            toast("扫描不到模块");
                    }
                });
            }
        };
        mBroadcast.setOnUdpListener(listener).start();
    }

    //UDP广播扫描不到模块，准备连接模块
    private void connectModule() {
        log("UDP扫描不到模块");
        //1. 5G判断
        if (mWifiManage.is5GBandLimits()) {
            closeWindows(null);
            Toast.makeText(mContext, "不能把HC-25模块配置到5G频段的路由器", Toast.LENGTH_LONG).show();
            mState = -1;
            return;
        }
        //2.从存储中获取路由信息,获取到返回true 否则返回false
        if (!getRouterMessage())
            return;

        //3.模块配网
        setModule(mTargetModule);
    }

    //从存储中获取路由信息,获取到返回true 否则返回false 并弹出窗口让用户输入路由信息
    private boolean getRouterMessage() {
        //1.记录路由
        mNowRouter = mWifiManage.getNowRouter();

        //2.获取密码
        mNowRouterPass = new WifiMessage(mContext).getWifiData(mNowRouter);

        //3.判断密码若为空，则检测路由是否为无密码状态
        if (!mNowRouterPass.equals("")) {
            return true;
        } else {
            if (mWifiManage.getEnc(mNowRouter) != null
                    && mWifiManage.getEnc(mNowRouter).equals("[ESS]"))
                return true;
        }

        //4.存储中获取不到密码，让用户输入,然后再设置模块
        log("弹出窗口让用户填入路由信息");
        getPasswordWindow();
        return false;
    }

    //设置工作模式1，连接模块
    private void setModule(String targetModule) {
        //判断当前状态
        if (mState == -1) {
            log("被强制中止，取消设置模块");
            return;
        }
        //1.连接模块
        mState = 1;
        log("开始连接: " + targetModule);
        if (!mWifiManage.connect(targetModule))
            getPass(targetModule);

        //配置模块的时候，再更新一次模块路由信息
        mBroadcastAnimation.setConnectMessage(mNowRouter, mTargetModule);
    }

    //获取路由器的信息（密码）
    private void getPasswordWindow() {
        final CommonDialog.Builder getPass = new CommonDialog.Builder(mContext);
        getPass.setView(R.layout.get_password_vessel).fullWidth().create().show();
        GetPassword passwordWindow = getPass.getView(R.id.get_message);
        passwordWindow.setName(mNowRouter);
        passwordWindow.setOnGetPasswordListener(new GetPassword.OnGetPasswordListener() {
            @Override
            public void getMessage(boolean b, String name, String password) {
                if (b) {
                    mNowRouter = name;
                    mNowRouterPass = password;
                    new WifiMessage(mContext).saveData(name, password);
                    setModule(mTargetModule);
                    getPass.dismiss();
                } else {
                    closeWindows(null);
                    getPass.dismiss();
                }
            }
        });
    }

    //连接WiFi的回调（成功，错误，连接被拒）
    private void initListener() {
        mWifiManage.setOnScanListener(new WifiManage.WifiListener() {
            @Override
            public void connectWifi(String name, String mac) {//成功连接目标
                toast("连接成功" + name);
                selectWorkMode();//选择工作模式
            }

            @Override
            public void connectError(String name) {//目标的密码错误
                toast(name + "密码错误");
                new WifiMessage(mContext).deleteName(name);
                getPass(name);
            }

            @Override
            public void connectionFail(String name) {//连接目标被拒
                mState = -1;
                log("连接" + name + "被拒");
                if (name != null && mNowRouter != null && !name.equals(mNowRouter))
                    mWifiManage.connect(mNowRouter);
                closeWindows(name);//name一般情况下不会为空
                toast("连接不上" + name);
            }


        });
    }

    //选择工作模式
    private void selectWorkMode() {
        log("需要执行的工作模式为：" + mState + " (注：0：直连；1：配网，发数据；2：路由中搜索配置好的模块)");
        //1.直连模式，socket连接就行
        if (mState == 0) {
            startActivity("192.168.4.1");
        }

        //2.配网模式之连上模块，发送配置信息
        if (mState == 1) {
            SetPrivateNetwork.SetNetworkListener listener = new SetPrivateNetwork.SetNetworkListener() {//设置专用网络的回调
                @Override
                public void succeed() {
                    log("配置网络成功，开始发送数据");
                    sendDataToModule();
                }

                @Override
                public void defeated() {
                    toast("请关闭手机的数据网络，再重新配网");
                    mWifiManage.connect(mNowRouter);
                    SetPrivateNetwork.resetNetwork(mContext);
                    mState = -1;
                }
            };
            SetPrivateNetwork.setNetwork(mContext, listener, 1);//设置专用网络
        }

        //3.配网模式之连上路由，UDP广播搜索即可
        if (mState == 2) {
            mTimeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mWifiManage.getNowRouter().equals(mNowRouter)) {
                        udpBroadcastScan(mTargetModule, 4);
                        if (mBroadcastAnimation == null) {
                            startBroadcastAnim(4);
                        } else {
                            mBroadcastAnimation.startLongBroadcast();
                        }
                    } else {
                        mWifiManage.connect(mNowRouter);
                    }
                }
            }, 3000);
            if (mBroadcastAnimation != null)
                mBroadcastAnimation.succeedRouter();
        }
    }

    //开启第二页面，进行sock连接
    private void startActivity(final String ip) {
        mState = 0;
        log("socket连接：" + ip);
        mTimeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                closeWindows(null);
                FragmentMessage.mIp = ip;
                Intent intent = new Intent(mContext, ImplantFragment.class);
                intent.putExtra("Name", mTargetModule);
                mContext.startActivity(intent);
            }
        }, 500);
    }

    //发送路由信息给模块
    private void sendDataToModule() {
        //需要有回调 来通知是否发送成功
        new TaskThread(mContext).setWorkCall(new TaskThread.WorkCallBack() {
            @Override
            public void succeed() {
                connectRouter();//配置成功
            }

            @Override
            public void defeated() {
                log("数据发送出错，尝试重新连接模块");
                mWifiManage.connect(mTargetModule);
            }

            @Override
            public boolean work() throws Exception {

                Response response;
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = getRequest();
                //发送数据
                Thread.sleep(800);//连接上模块后，等待网络稳定再发送数据
                if (mWifiManage.getNowRouter().equals(mTargetModule))
                    response = okHttpClient.newCall(request).execute();
                else
                    return false;

                log("发送http数据..");
                return response.isSuccessful();//返回接收情况
            }

            @Override
            public void error(Exception e) {

                if (!isAndroidK()) {
                    //小于Android5.0的不予判断
                    log("发送成功");
                    connectRouter();
                    return;
                }

                log("e:" + e.toString());
                if (!e.toString().equals("java.net.SocketException: Software caused connection abort")) {
                    log("发送失败");
                    Toast.makeText(mContext, "配置模块失败...", Toast.LENGTH_LONG).show();
                    mState = -1;
                    if (mBroadcastAnimation != null)
                        mBroadcastAnimation.error(mTargetModule);
                    mWifiManage.connect(mNowRouter);
                } else {
                    log("发送成功");
                    connectRouter();
                }
            }
        });
    }

    //设置OKHttp请求体的信息
    private Request getRequest() throws JSONException {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject user = new JSONObject();
        user.put("staN", mNowRouter);
        user.put("staP", mNowRouterPass);

        //创建一个RequestBody(参数1：数据类型 参数2传递的json串)
        String json = user.toString();
        RequestBody requestBody = RequestBody.create(JSON, json);
        //创建一个请求对象
        return new Request.Builder().url("http://192.168.4.1:80/config")
                .post(requestBody).build();
    }

    //把工作模式设置为2，连接路由
    private void connectRouter() {
        if (mState == -1) {
            log("被强制退出，取消连接路由");
            return;
        }
        mState = 2;
        if (mBroadcastAnimation != null)
            mBroadcastAnimation.succeedModule();//配置好模块，更新BroadcastAnimation里的内容
        mTimeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                log("数据发送成功，返回路由");
                mWifiManage.connect(mNowRouter);
            }
        }, 2000);
    }

    //获取模块密码
    private void getPass(final String ssid) {
        if (ssid == null)
            return;
        final CommonDialog.Builder getPass = new CommonDialog.Builder(mContext);
        getPass.setView(R.layout.get_password_vessel).loadAnimation().setCancelable(false).fullWidth().create().show();
        GetPassword passwordWindow = getPass.getView(R.id.get_message);
        passwordWindow.setName(ssid);
        passwordWindow.setFocusable();
        passwordWindow.setOnGetPasswordListener(new GetPassword.OnGetPasswordListener() {
            @Override
            public void getMessage(boolean b, String name, String password) {
                if (b) {
                    new WifiMessage(mContext).saveData(name, password);
                    mWifiManage.connect(ssid);
                    getPass.dismiss();
                } else {
                    closeWindows(name);
                    getPass.dismiss();
                }
            }
        });
    }

    //打开广播搜索时的动画窗口
    private void startBroadcastAnim(final int time) {

        if (mAnimBuilder == null) {
            mAnimBuilder = new CommonDialog.Builder(mContext);
        }

        mAnimBuilder.setView(R.layout.broadcast_animation_vessel).fullWidth().loadAnimation().setCancelable(false).create().show();

        mBroadcastAnimation = mAnimBuilder.getView(R.id.broadcast_anim);
        mBroadcastAnimation.setTime(time).startAnim().setHint(mTargetModule);
        mBroadcastAnimation.setOnAnimListener(new BroadcastAnimation.OnAnimListener() {
            @Override
            public void end() {//退出窗口
                mAnimBuilder.dismiss();
                mBroadcast.setOnUdpListener(null);//把广播监听回调接口设置为空，防止窗口关闭后还执行广播回调事件
                mBroadcastAnimation = null;
                if (mState == 1 || mState == 2) {
                    if (mState == 1)
                        toast("配网被强行终止，模块配网失败...");
                    else
                        toast("配网提前结束");
                    mState = -1;
                    mAnimBuilder = null;
                }
            }

            @Override
            public void scan() {//再次扫描
                if (!mIsScan) {//判断是否在扫描中
                    mBroadcastAnimation.startAnim();//再次广播扫描
                    udpBroadcastScan(mTargetModule, time);
                } else {
                    toast("已经处于扫描中，请稍等");
                }
            }

            @Override
            public void setModule() {//配置模块
                mBroadcastAnimation.setConnectMessage(mNowRouter, mTargetModule);
                connectModule();//连接模块
                mBroadcast.setOnUdpListener(null);//广播监听线程设置为空
            }

            @Override
            public void scanEnd() {//扫描结束
                mIsScan = false;//把广播扫描标志位设置false，表示此时不再扫描
                mBroadcast.setOnUdpListener(null);//把广播监听回调接口设置为空，防止窗口关闭后还执行广播回调事件
            }
        });
    }

    private void closeWindows(String name) {//回收窗口
        if (mBroadcastAnimation != null) {
            if (name != null)
                mBroadcastAnimation.error(name);
            else
                mBroadcastAnimation.stopView();
        }
        if (mLoadingDialog != null)
            mLoadingDialog.dismiss();
        if (mLoadingCircleView != null)
            mLoadingCircleView.stop();
    }

    private boolean isAndroidK() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    private void log(String log) {
        if (debug)
            Log.d("AppRun" + getClass().getSimpleName(), log);
    }

    private void toast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

}
