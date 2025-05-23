package com.test.assistant.wifiGather.connectSocket;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.test.assistant.fragments.FragmentMessage;
import com.test.connectservicelibrary.connectInternet.DormancyManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;

import static com.test.connectservicelibrary.connectInternet.ToolClass.bytesToHexString;
import static com.test.connectservicelibrary.connectInternet.ToolClass.hexString2ByteArray;


public class ConnectSocket {

    private static int TMO_CONNECTTION = 3500;//建立socket连接超时的时间，0代表无限
    private String APNAME;

    private String ip = FragmentMessage.mIp;
    private int port = 8080;
    private BufferedReader in;
    private OutputStream outputStream;//发送流
    private Socket socket;

    private Thread sendSocketThread;
    private Thread listenerSocketThread;

    private Thread sendBroadThread;
    private Thread listenerBroadThread;
    private Thread watchThread;

    private String AS = "AppRunConnectSocket";

    private String SSID;

    //1.空闲状态  2.正常工作  3.连接断开  4.连接出错
    private enum State {Leisure, Work, Disconnect, ConnectError}

    private State mState = State.Leisure;

    private ArrayList<String> list = new ArrayList<>();

    private boolean sign = true;

    private Handler handler;

    private int getDatas = 0;

    private int time = 1000 * 60;//一分钟
    private MulticastSocket ms;

    private int surplus = 0;

    private boolean listenerException = true;

    //缓存需要发送的数据，当主动发送造成断线的时候，重连后再把数据发送给模块，
    // 若是发送测试网络数据，则会被清除缓存
    private String stringTemp;

    private DormancyManager dormancyManager;//使 CPU 30分钟内不休眠，防止锁屏时 socket 断开


    public ConnectSocket(Context context, Handler handler, String SSID) {
        this.handler = handler;
        this.SSID = SSID;

        open();//开启socket连接
        init();//初始化广播
        setListenerBroadThread();//设置广播监听线程
        setSendBroadThread();//设置广播发送线程
        setWatchThread();//设置监听socket连接线程

        dormancyManager = new DormancyManager();
        dormancyManager.wakeAndUnlock(context);
    }

    public ConnectSocket(Context context, Handler handler, String SSID, int time) {
        this.handler = handler;
        this.SSID = SSID;

        open();//开启socket连接
        init();//初始化广播
        setListenerBroadThread();//设置广播监听线程
        setSendBroadThread();//设置广播发送线程
        setWatchThread();//设置监听socket连接线程

        dormancyManager = new DormancyManager();
        dormancyManager.wakeAndUnlock(context);
        TMO_CONNECTTION = time;

    }

    private synchronized void open() {
        if (socket != null || !sign || mState == State.ConnectError) {
            return;
        }
        Log.e(AS + "Socket", "创建一次socket");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    Log.d(AS, "开始socket连接 " + ip);
                    socket = buildConnection(ip, port);
                    if (socket == null) {
                        mState = State.ConnectError;
                        Log.e(AS, "socket开启失败");
                        return;
                    }
                    outputStream = socket.getOutputStream();
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "ISO-8859-15"));
                    Log.d(AS, "socket: " + socket);
                    listenerException = true;
                    if (socket != null) {
                        if (sendSocketThread == null)
                            setSendSocketThread();
                        if (listenerSocketThread == null)
                            setListenerSocketThread();
                        if (outputStream != null && in != null) {
                            sendHandler(0x04, "connected");
                            mState = State.Work;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mState = State.ConnectError;
                    sendHandler(0x04, "del");//标记
                    close("打开socket失败");
                }
            }
        }).start();
    }

    private static Socket buildConnection(String ip, int port) {
        if (ip == null || port <= 0) {
            return null;
        }
        Socket sock = new Socket();
        try {
            sock.connect(new InetSocketAddress(ip, port), TMO_CONNECTTION);
            sock.setKeepAlive(true);
            if (!sock.isConnected()) {
                sock.close();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                sock.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }

        return sock;
    }

    private synchronized void close(String string) {
        Log.e(AS, string + "关闭socket");
        try {
            if (dormancyManager != null) {
                dormancyManager.unLock();
            }
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            if (in != null) {
                in.close();
                in = null;
            }
            if (socket != null) {
                socket.close();
                Log.e(AS, "成功关闭socket");
                socket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //初始化广播
    private void init() {

        try {
            /*创建socket实例*/
            if (ms == null)
                ms = new MulticastSocket();
        } catch (Exception e) {
            Log.e("AppRunTime", "创建socket失败.." + e.toString());
            e.printStackTrace();
        }

    }


    private void setListenerSocketThread() {
        Log.d(AS, "设置接收线程工作");
        listenerSocketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int len;
                char[] chars = new char[4096];
                String str;
                while (sign) {
                    try {
                        if (socket != null && !socket.isClosed() &&
                                socket.isConnected() && !socket.isInputShutdown() && in != null && mState == State.Work)
                            while (((len = in.read(chars)) != -1)) {
                                try {
                                    Log.d("AppRunTime", "收到数据: " + new String(chars));

                                    if (FragmentMessage.mAccept) {
                                        str = bytesToHexString(getInfoBuff(chars, len).getBytes("ISO-8859-15"));
                                    } else {
                                        str = new String(getInfoBuff(chars, len).getBytes("ISO-8859-15"),
                                                "GB18030");
                                    }
                                    sendHandler(0x00, str);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (listenerException) {
                            listenerException = false;
                            mState = State.Disconnect;
                            close("接收线程");//接收线程
                            Log.e(AS, "连接断开");
                            sendHandler(0x04, "delSocket");//标记
                            try {
                                Thread.sleep(1000);
                                if (sign) {
                                    open();
                                    if (stringTemp != null)
                                        send(stringTemp);//由于发送导致断线，所以补充发送。
                                }
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        listenerSocketThread.start();
    }

    private void setSendSocketThread() {
        Log.d(AS, "设置发送线程工作");
        sendSocketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (sign) {

                    try {
                        if (socket != null && !socket.isClosed() &&
                                socket.isConnected() && !socket.isInputShutdown() && mState == State.Work) {
                            // 如果消息集合有东西，并且发送线程在工作。
                            if (list.size() > 0 && !socket.isOutputShutdown()) {
                                if (FragmentMessage.mSendHex) {
                                    try {
                                        outputStream.write(hexString2ByteArray(list.get(0)));
                                        outputStream.flush();
                                        stringTemp = list.remove(0);//缓存下主动发送的数据
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    try {
                                        String str = list.get(0);
                                        outputStream.write(str.getBytes("GBK"));
                                        outputStream.flush();
                                        stringTemp = list.remove(0);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mState = State.Disconnect;
                        close("发送线程");//发送线程出错
                        try {
                            Thread.sleep(1000);
                            if (sign)
                                open();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }

                }
            }
        });
        sendSocketThread.start();
    }


    private void setSendBroadThread() {
        Log.d(AS, "开启广播发送");
        if (sendBroadThread != null) {
            sendBroadThread.interrupt();
            sendBroadThread = null;
        }
        sendBroadThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (sign && mState != State.ConnectError) {
                    try {
                        ++surplus;
                        send();
                        Thread.sleep(1000);
                        send();
                        Thread.sleep(time);
                        if (surplus > 10) {
                            mState = State.ConnectError;
                            if (listenerException) {
                                close("广播超时");//广播超时
                                listenerException = false;
                                sendHandler(0x04, "del");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        sendBroadThread.start();
    }


    private void setListenerBroadThread() {
        Log.d(AS, "开启广播接收");
        if (listenerBroadThread != null) {
            listenerBroadThread.interrupt();
            listenerBroadThread = null;
        }
        listenerBroadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (sign) {
                    mListener();
                }
            }
        });
        listenerBroadThread.start();
    }

    private void setWatchThread() {
        watchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (sign) {
                    try {
                        Thread.sleep(time + 2000);
                        if (surplus > 4 && socket != null && mState == State.Work) {
                            //发送测试通信...
                            testConnect();
                            stringTemp = null;//清空之前发送缓存的数据，即使检测断线，也不需要把缓存的数据发给模块。
                            Thread.sleep(2000);
                            testConnect();
                            Log.d(AS, "测试通信检测完毕..");
                            if (surplus >= 9) {
                                surplus = 4;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        watchThread.start();
    }

    private void testConnect() {
        try {
            socket.sendUrgentData(0xff);
        } catch (IOException e) {
            Log.e(AS, "检测出异常");
            e.printStackTrace();
        }
    }

    //广播
    private void send() {
        //init();
        DatagramPacket dataPacket;
        try {
            ms.setTimeToLive(3);
            //将本机的IP（这里可以写动态获取的IP）地址放到数据包里，其实server端接收到数据包后也能获取到发包方的IP的
            byte[] data = "HC-25".getBytes();
            //224.0.0.1为广播地址
            InetAddress address = InetAddress.getByName(FragmentMessage.mIp);
            //这个地方可以输出判断该地址是不是广播类型的地址
            Log.d(AS, "发送测试广播");
            dataPacket = new DatagramPacket(data, data.length, address,
                    54321);
            //ms.joinGroup(address);
            ms.send(dataPacket);
        } catch (Exception e) {
            if (e.toString().equals("java.io.IOException: sendto failed: EPERM (Operation not permitted)")) {
                if (surplus > 0)
                    surplus--;
                Log.w(AS, "遇到锁屏，发送计数取消");
                return;
            }
            e.printStackTrace();
        }
    }


    //广播监听
    private void mListener() {
        init();
        byte[] receiveBuf = new byte[4096];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
        try {
            ms.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String str = new String(receivePacket.getData(), 0, receivePacket.getLength());

        if (pattern(str)) {
            byte[] datas = new byte[receivePacket.getLength()];
            System.arraycopy(receivePacket.getData(), 0, datas, 0, receivePacket.getLength());
            str = "";
            try {
                str = new String(datas, "GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String UdpName = str.trim();

        if (UdpName.length() < 60) {
            return;
        } else {
            try {
                mUDPresolver(UdpName);
                APNAME = APNAME.trim();
            } catch (Exception e) {
                return;
            }
        }

        if (SSID.equals(APNAME)) {
            APNAME = "";
            if (sign) {
                if (mState == State.Disconnect) {
                    try {
                        Thread.sleep(5000);
                        if (mState == State.Disconnect) {
                            Log.e(AS, "socket断开，开始重连");
                            if (socket != null) {
                                close("广播超时");
                                Thread.sleep(500);
                            }
                            open();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (mState == State.Work) {
                surplus = 0;
                Log.d(AS, "接收到心跳");
            }
        }
    }

    //发送数据
    public void send(String s) {
        list.add(s);
    }

    public void showdown() {
        sign = false;
        mState = State.ConnectError;
        //Log.d(AS,"退出sign= "+sign);
        if (watchThread != null)
            watchThread.interrupt();
        close("最终关闭");//最终关闭
        if (sendSocketThread != null)
            sendSocketThread.interrupt();
        if (listenerSocketThread != null)
            listenerSocketThread.interrupt();
        if (sendBroadThread != null)
            sendBroadThread.interrupt();
        if (listenerBroadThread != null)
            listenerBroadThread.interrupt();
    }

    private void sendHandler(int number, String string) {
        Message message = handler.obtainMessage();
        message.what = number;
        if (string != null) {
            message.obj = string;
        }
        handler.sendMessage(message);
    }

    //解码
    private String getInfoBuff(char[] paramArrayOfChar, int paramInt) {
        synchronized (this) {
            char[] arrayOfChar = new char[paramInt];
            int b = 0;
            try {
                for (b = 0; ; b++) {
                    if (b >= paramInt) {
                        Log.d("TAG", "数组长度: " + new String(arrayOfChar).length());
                        getDatas += new String(arrayOfChar).length();
                        sendHandler(0x03, String.valueOf(getDatas));
                        return new String(arrayOfChar);
                    }
                    arrayOfChar[b] = paramArrayOfChar[b];
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("TAG", "数组长度: " + new String(arrayOfChar).length());
                Log.e("TAG", "b= " + b + "  paramInt= " + paramInt + "   paramArrayOfChar= " + paramArrayOfChar.length);
                return new String(arrayOfChar);
            }
        }
    }

    /*
     * 解析UDP广播
     * */
    private void mUDPresolver(String mUDP) {
        int End = mUDP.length();
        int length = 0;
        String tempStr;
        String MAC = mUDP.substring(0, mUDP.indexOf(","));
        length += MAC.length() + 1;
        tempStr = mUDP.substring(length, End);
        String IP = tempStr.substring(0, tempStr.indexOf(","));
        length += IP.length() + 1;
        tempStr = mUDP.substring(length, End);
        String MID = tempStr.substring(0, tempStr.indexOf(","));
        length += MID.length() + 1;
        tempStr = mUDP.substring(length, End);
        String VERSION = tempStr.substring(0, tempStr.indexOf(","));
        length += VERSION.length() + 1;
        tempStr = mUDP.substring(length, End);

        length = tempStr.length();
        APNAME = tempStr.substring(0, length);

        //拆出IP和APNAME
        /*End = IP.length();
        length = IP.substring(0, IP.indexOf(":")).length();
        IP = IP.substring(length+1,End);*/

        End = APNAME.length();
        length = APNAME.substring(0, APNAME.indexOf(":")).length();
        APNAME = APNAME.substring(length + 1, End);

    }

    private boolean pattern(String str) {
        int number = str.length();
        for (int i = 1; i < number; i++) {
            if (str.substring(i - 1, i).equals("�")) {
                return true;
            }
        }
        return false;
    }

}
