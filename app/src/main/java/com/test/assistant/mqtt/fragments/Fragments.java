package com.test.assistant.mqtt.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.test.assistant.mqtt.fragments.tool.MessageInterface;
import com.test.baselibrary.ioc.ViewUtils;

public abstract class Fragments extends Fragment implements MessageInterface {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int id = setFragmentViewId();
        View view = LayoutInflater.from(getActivity()).inflate(id, container, false);
        ViewUtils.inject(view, this);
        initAll();
        return view;
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

    void log(Class clazz, String log) {
        Log.d("AppRun" + clazz.getSimpleName(), log);
    }

    abstract int setFragmentViewId();

    abstract void initAll();

}
