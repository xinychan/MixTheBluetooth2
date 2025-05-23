package com.test.assistant.wifiGather.udpScanTarget;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class ScanModuleBroadcast {

    private String IP;
    private String APNAME;

    private MulticastSocket mMulticastSocket;

    private boolean mWorkSign = false;

    private String mTarget;

    private int mTime = 2;

    public ScanModuleBroadcast(String target) {
        mTarget = target;
    }

    public ScanModuleBroadcast(String target, int time) {
        mTarget = target;
        mTime = time;
    }

    public void start() {
        initAll();
        listenerThread();
        sendThread();
    }

    private void initAll() {
        try {
            mMulticastSocket = new MulticastSocket();
        } catch (IOException e) {
            Log.e("AppRunTime", "创建socket失败.." + e.toString());
            e.printStackTrace();
            return;
        }
        mWorkSign = true;
    }

    private void listenerThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mWorkSign) {
                    listener();
                }
            }
        }).start();
    }

    private void sendThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    send();
                    Thread.sleep(500 * mTime);
                    send();
                    Thread.sleep(500 * mTime);
                    send();
                    Thread.sleep(500 * mTime);
                    send();
                    Thread.sleep(500 * mTime);
                    mWorkSign = false;
                    Thread.sleep(200 * mTime);


                    if (mListener != null) {
                        Log.d("AppRun", "扫描不到，回调");
                        mListener.callback(false, "");
                        unBroadcast();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    mWorkSign = false;
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    if (mListener != null)
                        mListener.callback(false, "");
                    unBroadcast();
                }
            }
        }).start();
    }

    private void send() {
        //init();
        DatagramPacket dataPacket;
        try {
            if (mMulticastSocket == null)
                return;
            mMulticastSocket.setTimeToLive(155);
            //将本机的IP（这里可以写动态获取的IP）地址放到数据包里，其实server端接收到数据包后也能获取到发包方的IP的
            byte[] data = "HC-25".getBytes();
            //224.0.0.1为广播地址
            InetAddress address = InetAddress.getByName("255.255.255.255");
            //这个地方可以输出判断该地址是不是广播类型的地址
            dataPacket = new DatagramPacket(data, data.length, address,
                    54321);
            //ms.joinGroup(address);
            if (mWorkSign)
                mMulticastSocket.send(dataPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listener() {
        byte[] receiveBuf = new byte[4096];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
        try {
            mMulticastSocket.receive(receivePacket);
        } catch (IOException e) {
            if (!e.toString().equals("java.net.SocketException: Socket closed"))
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
            } catch (Exception e) {
                return;
            }
        }

        try {
            APNAME = APNAME.trim();
            IP = IP.trim();
            Log.d("AppRun", "扫描到:" + APNAME);
            if (APNAME.equals(mTarget) && mListener != null) {
                Log.d("AppRun", "扫描到目标");
                mListener.callback(true, IP);
                unBroadcast();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void unBroadcast() {
        mWorkSign = false;
        mListener = null;
        try {
            if (mMulticastSocket != null) {
                mMulticastSocket.close();
                mMulticastSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        IP = tempStr.substring(0, tempStr.indexOf(","));
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
        End = IP.length();
        length = IP.substring(0, IP.indexOf(":")).length();
        IP = IP.substring(length + 1, End);

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

    public interface OnUdpListener {
        void callback(boolean state, String ip);
    }

    private OnUdpListener mListener;

    public ScanModuleBroadcast setOnUdpListener(OnUdpListener listener) {
        this.mListener = listener;
        return this;
    }

}
