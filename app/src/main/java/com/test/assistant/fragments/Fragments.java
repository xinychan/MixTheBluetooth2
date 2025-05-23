package com.test.assistant.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class Fragments extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    void toast(final String string) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendHandler(int what, Object data) {
        Handler handler = FragmentSingMessage.getFragmentSingMessage().getSendFragmentHandler();
        Message message = handler.obtainMessage();
        message.what = what;
        if (data != null)
            message.obj = data;
        handler.sendMessage(message);
    }

    void sendServerHandler(int what, Object data) {
        Handler handler = FragmentSingMessage.getFragmentSingMessage().getSendServerHandler();
        Message message = handler.obtainMessage();
        message.what = what;
        if (data != null)
            message.obj = data;
        handler.sendMessage(message);
    }

}
