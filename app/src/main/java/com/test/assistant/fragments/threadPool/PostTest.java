package com.test.assistant.fragments.threadPool;

import android.app.Activity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PostTest {
    private Socket mSocket;
    private static final int TMO_CONNECTTION = 2500;

    public PostTest(final String ip, final String post, final TextPostCallback textPostCallback, final Activity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ip != null && post != null)
                        mSocket = buildConnection(ip, Integer.parseInt(post));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSocket != null) {
                            textPostCallback.state(true);
                            try {
                                mSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            textPostCallback.state(false);
                        }
                    }
                });
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

    public interface TextPostCallback {
        void state(boolean state);
    }

}
