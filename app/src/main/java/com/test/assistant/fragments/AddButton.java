package com.test.assistant.fragments;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.test.assistant.R;
import com.test.assistant.storage.FragmentButtonData;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;
import com.test.baselibrary.ioc.ViewUtils;


public class AddButton extends LinearLayout {

    @ViewById(R.id.button_name)
    private EditText mButtonName;
    @ViewById(R.id.button_data)
    private EditText mButtonData;


    public AddButton(Context context) {
        this(context, null);
    }

    public AddButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.add_button_menu, this);
        ViewUtils.inject(this);
    }


    @OnClick(R.id.add_button_back)
    private void back() {
        //退出
        FragmentSingMessage.getFragmentSingMessage().getAddButtonBuilder().dismiss();
    }

    @OnClick(R.id.add_button_affirm)
    private void affirm() {
        //设置
        FragmentSingMessage fragmentSingMessage = FragmentSingMessage.getFragmentSingMessage();
        FragmentButtonData fragmentButtonData = fragmentSingMessage.getFragmentButtonData();
        fragmentButtonData.saveButtonName(fragmentSingMessage.getButtonId(),
                mButtonName.getText().toString().trim(),
                mButtonData.getText().toString().trim());
        Button btn = ((Activity) fragmentSingMessage.getAddButtonBuilder().getContext()).findViewById(fragmentSingMessage.getButtonId());
        btn.setText(mButtonName.getText().toString().trim());
        fragmentSingMessage.getAddButtonBuilder().dismiss();
    }
}
