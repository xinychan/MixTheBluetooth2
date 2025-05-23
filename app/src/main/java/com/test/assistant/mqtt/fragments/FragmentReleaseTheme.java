package com.test.assistant.mqtt.fragments;

import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.widget.PopupWindowCompat;

import com.test.assistant.MQTTActivity;
import com.test.assistant.R;
import com.test.assistant.mqtt.fragments.tool.TopicBox;
import com.test.assistant.view.CustomButtonView;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;

/*
 * 发布主题
 * */
public class FragmentReleaseTheme extends Fragments {

    @ViewById(R.id.fragment_release_theme_data)
    private EditText mData;

    @ViewById(R.id.fragment_release_theme_subtopic)
    private EditText mSubtopic;

    @ViewById(R.id.fragment_release_theme_QoS)
    private TextView mQos;

    @ViewById(R.id.fragment_release_theme_retain_switch)
    private CustomButtonView mRetainSwitch;

    private Handler mHandler;

    @Override
    int setFragmentViewId() {
        return R.layout.fragment_release_theme;
    }

    @Override
    void initAll() {

    }

    @OnClick({R.id.fragment_release_theme_QoS, R.id.fragment_release_theme_QoS_icon})
    private void clickQoS() {
        showPopupWindow(mQos);
    }

    @OnClick(R.id.fragment_release_theme_send)
    private void sendData() {
        String subtopic = mSubtopic.getText().toString().trim();
        String data = mData.getText().toString().trim();
        Message message = mHandler.obtainMessage();
        message.what = MQTTActivity.RELEASE_THEME;
        message.obj = new TopicBox(subtopic, data, Integer.parseInt(mQos.getText().toString()), mRetainSwitch.getState() == CustomButtonView.State.Open);
        mHandler.sendMessage(message);
    }


    @OnClick(R.id.fragment_release_theme_retain_switch)
    private void isRetain() {
        if (mRetainSwitch.getState() == CustomButtonView.State.Open) {
            mRetainSwitch.closed();
        } else if (mRetainSwitch.getState() == CustomButtonView.State.Close) {
            mRetainSwitch.staysOn();
        }
    }


    private void showPopupWindow(View view) {

        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(view.getContext()).inflate(
                R.layout.qos_fold_menu, null);

        final PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        //需要先测量，PopupWindow还未弹出时，宽高为0
        contentView.measure(makeDropDownMeasureSpec(popupWindow.getWidth()),
                makeDropDownMeasureSpec(popupWindow.getHeight()));

        //可以退出
        popupWindow.setTouchable(true);

        int offsetX = view.getWidth() - popupWindow.getContentView().getMeasuredWidth();
        int offsetY = 0;
        PopupWindowCompat.showAsDropDown(popupWindow, view, offsetX, offsetY, Gravity.START);

        View.OnClickListener viewListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                switch (v.getId()) {
                    case R.id.qos_fold_menu_zero:
                        mQos.setText(String.valueOf(0));
                        break;
                    case R.id.qos_fold_menu_one:
                        mQos.setText(String.valueOf(1));
                        break;
                    case R.id.qos_fold_menu_two:
                        mQos.setText(String.valueOf(2));
                        break;
                }
            }
        };

        // 设置按钮的点击事件
        setItemClickListener(contentView, viewListener);

        // 设置好参数之后再show
        popupWindow.showAsDropDown(view);

    }

    /**
     * 设置子View的ClickListener
     */
    private void setItemClickListener(View view, View.OnClickListener listener) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                //不断的递归给里面所有的View设置OnClickListener
                View childView = viewGroup.getChildAt(i);
                setItemClickListener(childView, listener);
            }
        } else {
            view.setOnClickListener(listener);
        }
    }


    @SuppressWarnings("ResourceType")
    private static int makeDropDownMeasureSpec(int measureSpec) {
        int mode;
        if (measureSpec == ViewGroup.LayoutParams.WRAP_CONTENT) {
            mode = View.MeasureSpec.UNSPECIFIED;
        } else {
            mode = View.MeasureSpec.EXACTLY;
        }
        return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(measureSpec), mode);
    }


    @Override
    public void setHandler(Handler handler) {
        //获得handler
        mHandler = handler;
    }

    @Override
    public void setTopicMessage(TopicBox topicBox) {
        //获得主题
        log(getClass(), "收到消息");
    }

    @Override
    public void subscribeFailure() {
        //订阅失败
    }
}
