package com.test.assistant.mqtt.fragments;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cy.cyflowlayoutlibrary.FlowLayoutAdapter;
import com.cy.cyflowlayoutlibrary.FlowLayoutScrollView;
import com.test.assistant.MQTTActivity;
import com.test.assistant.R;
import com.test.assistant.mqtt.expandableRecycler.GenreAdapter;
import com.test.assistant.mqtt.expandableRecycler.MyRecyclerData;
import com.test.assistant.mqtt.fragments.tool.TopicBox;
import com.test.baselibrary.ioc.OnClick;
import com.test.baselibrary.ioc.ViewById;

import java.util.ArrayList;
import java.util.List;

/*
 * 订阅主题
 * */
public class FragmentSubscribeTopic extends Fragments {

    @ViewById(R.id.fragment_subscribe_topic_list)
    private RecyclerView mRecyclerView;

    @ViewById(R.id.fragment_subscribe_topic_et)
    private EditText mTopicET;

    @ViewById(R.id.fragment_subscribe_topic_flow)
    private FlowLayoutScrollView mScrollView;

    private MyRecyclerData myRecyclerData = new MyRecyclerData();

    private GenreAdapter mAdapter;

    private FlowLayoutAdapter<String> mFlowLayoutAdapter;

    private List<String> mLabelList = new ArrayList<>();

    private Handler mHandler;

    private int mItemNumber = 0;

    @Override
    int setFragmentViewId() {
        return R.layout.fragment_subscribe_topic;
    }

    @Override
    void initAll() {
        initRecycler();
        initFlow();
    }

    @OnClick(R.id.fragment_subscribe_topic_bt)
    private void onClick() {
        String topic = mTopicET.getText().toString().trim();
        if (topic.equals("")) {
            toast("不能为空");
            return;
        }

        for (String s : mLabelList) {
            if (s.equals(topic)) {
                toast("这个主题已被订阅，不需要重复订阅");
                mTopicET.setText("");
                return;
            }
        }

        sendHandler(MQTTActivity.SUBSCRIBE_TOPIC, topic);

        mFlowLayoutAdapter.add(topic);
        mTopicET.setText("");
        if (mScrollView.getHeight() > dip2px(mScrollView.getContext(), 150)) {
            ViewGroup.LayoutParams layoutParams = mScrollView.getLayoutParams();
            layoutParams.height = dip2px(mScrollView.getContext(), 150);
            mScrollView.setLayoutParams(layoutParams);
            mItemNumber = mFlowLayoutAdapter.getCount();
        }
    }

    private void initRecycler() {
        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        mAdapter = new GenreAdapter(myRecyclerData.getList());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initFlow() {
        mFlowLayoutAdapter = new FlowLayoutAdapter<String>(mLabelList) {
            @Override
            public void bindDataToView(ViewHolder holder, int position, String bean) {
                holder.setText(R.id.tv, bean);
            }

            @Override
            public void onItemClick(int position, String bean) {
                remove(position);
                sendHandler(MQTTActivity.UNSUBSCRIBE_TOPIC, bean);
                if (mFlowLayoutAdapter.getCount() <= mItemNumber - 1) {
                    ViewGroup.LayoutParams layoutParams = mScrollView.getLayoutParams();
                    if (layoutParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        mScrollView.setLayoutParams(layoutParams);
                        mItemNumber = 0;
                    }
                }
            }

            @Override
            public int getItemLayoutID(int position, String bean) {
                return R.layout.item_fragment_flow;
            }
        };
        mScrollView.setAdapter(mFlowLayoutAdapter);
    }

    private void readMsg(String subtopic, String data, int qos) {
        if (myRecyclerData.addData(subtopic, data)) {
            mAdapter = new GenreAdapter(myRecyclerData.getList());
            mRecyclerView.setAdapter(mAdapter);
        } else {
            try {
                mAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendHandler(int msg, String data) {
        Message message = mHandler.obtainMessage();
        message.what = msg;
        message.obj = data;
        mHandler.sendMessage(message);
    }

    @Override
    public void setHandler(Handler handler) {
        //设置handler
        mHandler = handler;
    }

    @Override
    public void setTopicMessage(TopicBox topicBox) {
        //获得主题
        readMsg(topicBox.getTopic(), topicBox.getMessage(), topicBox.getQoS());
        log(getClass(), "收到消息");
    }

    @Override
    public void subscribeFailure() {
        //订阅失败
        if (mFlowLayoutAdapter.getCount() != 0)
            mFlowLayoutAdapter.remove(mFlowLayoutAdapter.getCount() - 1);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
