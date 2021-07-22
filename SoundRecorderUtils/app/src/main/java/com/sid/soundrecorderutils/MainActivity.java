package com.sid.soundrecorderutils;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final String CURRECT_VERSION = "0.0.1";

    private Button mBtnRecordAudio;
    private Button mBtnPlayAudio;
    private Button mBtnSend;
    private TextView main_text_server_status;
    private TextView main_text_send_status;
    private TextView main_text_player_status;
    private boolean cliConnStat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        main_text_server_status = (TextView) findViewById(R.id.main_text_server_status);
        main_text_send_status = (TextView) findViewById(R.id.main_text_send_status);
        main_text_player_status = (TextView) findViewById(R.id.main_text_player_status);


//        checkServer(main_text_server_status);
        Timer mTimer = new Timer();
        TimerTask mTimerTask = new TimerTask() {//创建一个线程来执行run方法中的代码
            @Override
            public void run() {
                checkServer();
                checkClient();
            }
        };
        mTimer.schedule(mTimerTask, 0, 1000);

        main_text_send_status.setText("未发送");
        main_text_player_status.setText("未连接");

        mBtnRecordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RecordAudioDialogFragment fragment = RecordAudioDialogFragment.newInstance();
                fragment.show(getSupportFragmentManager(), RecordAudioDialogFragment.class.getSimpleName());
                fragment.setOnCancelListener(new RecordAudioDialogFragment.OnAudioCancelListener() {
                    @Override
                    public void onCancel() {
                        fragment.dismiss();
                    }
                });
            }
        });

        mBtnPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordingItem recordingItem = new RecordingItem();
                SharedPreferences sharePreferences = getSharedPreferences("sp_name_audio", MODE_PRIVATE);
                final String filePath = sharePreferences.getString("audio_path", "");
                long elpased = sharePreferences.getLong("elpased", 0);
                recordingItem.setFilePath(filePath);
                recordingItem.setLength((int) elpased);
                PlaybackDialogFragment fragmentPlay = PlaybackDialogFragment.newInstance(recordingItem);
                fragmentPlay.show(getSupportFragmentManager(), PlaybackDialogFragment.class.getSimpleName());
            }
        });

        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cliConnStat) {
                    final AlertDialog.Builder normalDialog = new AlertDialog.Builder(MainActivity.this);
                    normalDialog.setTitle("提示");
                    normalDialog.setMessage("播放器没有连接,当下次播放器连接时将播放录音.确定?");
                    normalDialog.setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences sharePreferences = getSharedPreferences("sp_name_audio", MODE_PRIVATE);
                                    final String filePath = sharePreferences.getString("audio_path", "");

                                    SendService sendService = new SendService();
                                    sendService.sendAudio(filePath, handler);
                                }
                            });
                    normalDialog.setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //...To-do
                                }
                            });
                    // 显示
                    normalDialog.show();
                }
                else
                {
                    SharedPreferences sharePreferences = getSharedPreferences("sp_name_audio", MODE_PRIVATE);
                    final String filePath = sharePreferences.getString("audio_path", "");

                    SendService sendService = new SendService();
                    sendService.sendAudio(filePath, handler);
                }
            }
        });
        SendService.getInstance().GetVersion(handler);
    }

    private void checkServer()
    {
        SendService.getInstance().getStatus(new Callback() {
            //请求失败执行的方法
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Message msg = new Message();
                msg.what = UPDATE_SERVER_STATUS;
                msg.obj = "连接失败: " + e.toString();
                handler.sendMessage(msg);
            }
            //请求成功执行的方法
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Message msg = new Message();
                msg.what = UPDATE_SERVER_STATUS;
                msg.obj = response.code() == 404 ?"连接成功":"连接失败 code:" + response.code();
                handler.sendMessage(msg);
            }
        });
    }

    private void checkClient()
    {
        SendService.getInstance().getClientStatus(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Message msg = new Message();
                msg.what = UPDATE_SERVER_STATUS;
                msg.obj = "连接失败: " + e.toString();
                System.out.println();
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Message msg = new Message();
                try {
                    Map<String, Object> resp = JsonUtils.json2map(Objects.requireNonNull(response.body()).string());
                    cliConnStat = "online".equals(resp.get("status").toString());
                    msg.what = UPDATE_CLI_STATUS;
                    msg.obj = cliConnStat?"在线":"离线";
                } catch (Exception e) {
                    msg.what = Alert;
                    msg.obj = e.toString();
                }
                handler.sendMessage(msg);
            }
        });

    }

    private void initView() {
        mBtnRecordAudio = (Button)findViewById(R.id.main_btn_record_sound);
        mBtnPlayAudio = (Button) findViewById(R.id.main_btn_play_sound);
        mBtnSend = (Button) findViewById(R.id.main_btn_send_sound);
    }

    public static final int UPDATE_SERVER_STATUS = 1;
    public static final int UPDATE_SEND_STATUS = 2;
    public static final int Alert = 3;
    public static final int UPDATE_CLI_STATUS = 4;
    public static final int GOT_VERSION = 5;


    private Handler handler = new Handler()
    {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case UPDATE_SERVER_STATUS:
                    main_text_server_status.setText(msg.obj.toString());
                    break;
                case UPDATE_SEND_STATUS:
                    main_text_send_status.setText(msg.obj.toString());
                    break;
                case Alert:
                    AlertDialog.Builder builder  = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(((Map<String, Object>) msg.obj).get("title").toString());
                    builder.setMessage(((Map<String, Object>) msg.obj).get("message").toString());
                    builder.setPositiveButton("是" ,  null );
                    builder.show();
                    break;
                case UPDATE_CLI_STATUS:
                    main_text_player_status.setText(msg.obj.toString());
                    break;
                case GOT_VERSION:
                    if (!msg.obj.equals(CURRECT_VERSION) && false)
                    {
                        new UpdateManger(MainActivity.this).checkUpdateInfo();
                    }
            }
        }
    };
}
