package com.test.assistant.fragments.threadPool;

import android.app.Activity;
import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GetModuleDate {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public void postJson(final String ip, final int position, final ModuleMessage moduleMessage, Activity activity) {

        //创建一个OkHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(JSON, "AT+SOCK");

        //创建一个请求对象
        Request request = new Request.Builder().url("http://" + ip + ":80/web_at")
                .post(requestBody)
                .build();

        //发送请求获取响应
        try {
            Response response = okHttpClient.newCall(request).execute();
            //判断请求是否成功
            if (response.isSuccessful()) {
                String s = response.body().string();
                Log.i("AppRunOkHttp", s);//打印服务端返回结果
                shearString(s, position, moduleMessage, activity);
            }
        } catch (final IOException e) {
            Log.e("TAG", "错误: " + e);
            //e.printStackTrace();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        moduleMessage.error(e.toString(), position);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void shearString(final String data, final int position, final ModuleMessage moduleMessage, Activity activity) {
        final String s = "OK\n" +
                "    SOCK=TCPC,120.25.163.9,56860";
        String temp = data.substring(data.indexOf(",") + 1);
        final String ip = temp.substring(0, temp.indexOf(","));
        final String post = temp.substring(temp.indexOf(",") + 1);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("AppRunGetModule", "" + data.substring(0, data.indexOf(",")));
                try {
                    if (data.substring(data.indexOf("="), data.indexOf(",")).equals(s.substring(s.indexOf("="), s.indexOf(","))))
                        moduleMessage.getMessage(ip, post.replaceAll("[\r\n]", ""), position);
                    else
                        moduleMessage.getMessage("没有配置到服务器", "没有配置到服务器", position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
