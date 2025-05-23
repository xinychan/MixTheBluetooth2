package com.test.assistant.mqtt.fragments;


import androidx.fragment.app.FragmentTransaction;

import com.test.assistant.R;

import java.util.ArrayList;
import java.util.List;

public class MQTTFragmentManage {

    private List<FragmentMessage> mFragments = new ArrayList<>();
    private int mViewId;
    private int mPreviousFragment;

    public MQTTFragmentManage(int viewId) {
        mViewId = viewId;
    }

    public void initFragment(int buttonId, Fragments fragments, FragmentTransaction transaction) {
        boolean isCommit = false;
        hideFragment(transaction);//隐藏所有的Fragment
        for (FragmentMessage mFragment : mFragments) {
            if (mFragment.getViewId() == buttonId) {
                transaction.setCustomAnimations(selectAnim(buttonId), 0);
                transaction.show(mFragment.getFragment());//展示指定的Fragment
                transaction.commit();//提交
                mFragment.setHide(false);//设置为非隐藏
                isCommit = true;
                break;
            }
        }

        if (!isCommit) {
            mFragments.add(new FragmentMessage(buttonId, fragments));
            transaction.setCustomAnimations(selectAnim(buttonId), 0);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(mViewId, fragments);
            transaction.show(fragments);//展示指定的Fragment
            transaction.commit();//提交
        }

    }


    //隐藏所有
    private void hideFragment(FragmentTransaction transaction) {
        mPreviousFragment = getFragmentId();
        for (FragmentMessage mFragment : mFragments) {
            transaction.hide(mFragment.getFragment());
            mFragment.setHide(true);//设置标志为隐藏
        }
    }

    public void delete(int viewId, FragmentTransaction transaction) {
        for (FragmentMessage mFragment : mFragments) {
            if (mFragment.getViewId() == viewId) {
                transaction.remove(mFragment.getFragment());
                mFragments.remove(mFragment);
                transaction.commit();
                break;
            }
        }
    }

    //获取没有被隐藏的Fragment
    public int getFragmentId() {
        for (FragmentMessage mFragment : mFragments) {
            if (!mFragment.getHide()) {
                return mFragment.getViewId();
            }
        }
        return -1;
    }


    private int selectAnim(int buttonId) {
        int left = R.id.send;
        if (left == buttonId)
            return R.anim.fragment_left;
        else
            return R.anim.fragment_right;
    }


    private class FragmentMessage {
        private int mViewId;
        private Fragments mFragment;
        private boolean mHide = false;//初始为非隐藏

        FragmentMessage(int viewId, Fragments fragments) {
            this.mFragment = fragments;
            this.mViewId = viewId;
        }

        Fragments getFragment() {
            return mFragment;
        }

        int getViewId() {
            return mViewId;
        }

        void setHide(boolean mHide) {
            this.mHide = mHide;
        }

        boolean getHide() {
            return mHide;
        }
    }


}
