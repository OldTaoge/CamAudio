package com.sid.soundrecorderutils;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SendService {

    private static final String SERVER_STATUS_ADDR = "";
    private static final String SERVER_PLAYER_STATUS_ADDR = "";
    private static final String SERVER_AUDIO_ADDR = "";
    private static final String SERVER_VERSION_ADDR = "";

    private static final SendService instance = new SendService();

    public void getStatus(Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_STATUS_ADDR).method("GET", null).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public void getClientStatus(Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PLAYER_STATUS_ADDR).method("GET", null).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public void sendAudio(String path, final Handler handler) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SERVER_AUDIO_ADDR)
                .post(new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file",
                                path.substring(path.lastIndexOf("/")+1),
                                RequestBody.create(MediaType.parse("audio/mp4; charset=utf-8"),
                                        new File(path))
                        )
                        .build()
                )
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                Message msg = new Message();
                msg.what = MainActivity.UPDATE_SEND_STATUS;
                msg.obj = "失败";
                handler.sendMessage(msg);

                msg = new Message();
                msg.what = MainActivity.Alert;
                Map<String, Object> msgObj = new  HashMap<>();
                msgObj.put("title", "发送状态");
                msgObj.put("message", "失败:" + e.toString());

                msg.obj = msgObj;
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Message msg = new Message();
                msg.what = MainActivity.UPDATE_SEND_STATUS;
                msg.obj = "成功";
                handler.sendMessage(msg);

                msg = new Message();
                msg.what = MainActivity.Alert;
                Map<String, Object> msgObj = new  HashMap<>();
                msgObj.put("title", "发送状态");
                msgObj.put("message", "成功");

                msg.obj = msgObj;
                handler.sendMessage(msg);
            }
        });
    }

    public void GetVersion(final Handler handler) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_VERSION_ADDR).method("GET", null).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.code() == 200 && response.body() != null)
                {
                    Message msg = new Message();
                    msg.what = MainActivity.GOT_VERSION;
                    msg.obj = response.body();
                    handler.sendMessage(msg);
                }
            }
        });
    }


    public static SendService getInstance() {
        return instance;
    }
}
