package com.test.assistant.fragments.udpBroadcast;

import android.util.Log;

import com.test.assistant.fragments.FragmentSingMessage;
import com.test.assistant.recyclerAdapter.Resou;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

public class ScanBroadcast {

    private String MAC;
    private String IP;
    private String MID;
    private String VERSION;
    private String APNAME;

    private FragmentSingMessage mFragmentSingMessage = FragmentSingMessage.getFragmentSingMessage();
    private MulticastSocket mMulticastSocket;
    private List<Resou> mModelList = new ArrayList<>();

    private boolean mWorkSign = false;

    public ScanBroadcast() {
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
        mFragmentSingMessage.clearList();
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
                send();
                try {
                    Thread.sleep(200);
                    send();
                    Thread.sleep(500);
                    send();
                    Thread.sleep(100);
                    mWorkSign = false;
                    Thread.sleep(100);
                    mFragmentSingMessage.setModelList(mModelList);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void send() {
        //init();
        DatagramPacket dataPacket = null;
        try {
            mMulticastSocket.setTimeToLive(3);
            //将本机的IP（这里可以写动态获取的IP）地址放到数据包里，其实server端接收到数据包后也能获取到发包方的IP的
            byte[] data = "HC-25".getBytes();
            //224.0.0.1为广播地址
            InetAddress address = InetAddress.getByName("255.255.255.255");
            //这个地方可以输出判断该地址是不是广播类型的地址
            dataPacket = new DatagramPacket(data, data.length, address,
                    54321);
            //ms.joinGroup(address);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        addModel();
    }

    private void addModel() {
        for (Resou resou : mModelList) {
            if (resou.getmIP().equals(IP)) {
                return;
            }
        }
        mModelList.add(new Resou(IP, APNAME));
    }

    /*
     * 解析UDP广播
     * */
    private void mUDPresolver(String mUDP) {
        int End = mUDP.length();
        int length = 0;
        String tempStr;
        MAC = mUDP.substring(0, mUDP.indexOf(","));
        length += MAC.length() + 1;
        tempStr = mUDP.substring(length, End);
        IP = tempStr.substring(0, tempStr.indexOf(","));
        length += IP.length() + 1;
        tempStr = mUDP.substring(length, End);
        MID = tempStr.substring(0, tempStr.indexOf(","));
        length += MID.length() + 1;
        tempStr = mUDP.substring(length, End);
        VERSION = tempStr.substring(0, tempStr.indexOf(","));
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

}
