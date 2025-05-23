package com.test.assistant;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.test.assistant.baseUse.DefaultNavigationBar;


public class AboutActivity extends BasActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initTitle();
    }

    //设置头部
    private void initTitle() {
        new DefaultNavigationBar
                .Builder(this, (ViewGroup) findViewById(R.id.about_me))
                .setTitle("关于我们")
                .builer();
    }

    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.about_me_copy: //复制官网
                copy("http://www.hc01.com/home");
                break;
        }
        Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show();

    }

    private void copy(String data) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", data);
        // 将ClipData内容放到系统剪贴板里。
        if (mClipData != null)
            cm.setPrimaryClip(mClipData);
    }
}
