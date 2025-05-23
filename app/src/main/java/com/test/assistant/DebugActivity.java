package com.test.assistant;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.test.assistant.baseUse.DefaultNavigationBar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DebugActivity extends BasActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        initViev();
        initTitle();
        initData();
    }

    private void initData() {

    }

    private void initViev() {
        super.setContext(this);
        setButton(R.id.debug_read);
    }

    @Override
    public void onClick(View v) {
        TextView textView = findViewById(R.id.bug_log);

        textView.setText(load("errNewLog"));
    }

    private void initTitle() {
        new DefaultNavigationBar
                .Builder(this, (ViewGroup) findViewById(R.id.debug_activity))
                .setTitle("Bug日志")
                .setRightText("")
                .setRightClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                })
                .builer();
    }

    public String load(String file) {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            in = openFileInput(file);
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!content.toString().isEmpty())
            return content.toString();
        else
            return "没有记录到异常";
    }

    public void setButton(int view) {
        View textView = findViewById(view);
        textView.setOnClickListener(this);
    }

}
