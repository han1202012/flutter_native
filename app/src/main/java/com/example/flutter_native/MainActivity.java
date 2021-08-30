package com.example.flutter_native;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.android.FlutterFragment;
import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.StringCodec;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Flutter MainActivity";


    /**
     * 嵌入到 Activity 界面的 FlutterFragment
     */
    private FlutterFragment mFlutterFragment;

    /**
     * 显示收发消息的组件
     */
    private TextView show_message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        show_message = findViewById(R.id.show_message);

        findViewById(R.id.flutter1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction =
                        getSupportFragmentManager().beginTransaction();

                // 使用该方法创建的 Fragment 没有传递数据
                //FlutterFragment.createDefault()
                // 打开默认界面
                //fragmentTransaction.replace(R.id.frame, FlutterFragment.createDefault());

                mFlutterFragment = FlutterFragment.withNewEngine().
                        initialRoute("嵌入 FlutterFragment").build();

                Log.i(TAG, "mFlutterFragment : " + mFlutterFragment);

                // 创建 FlutterFragment
                fragmentTransaction.replace(R.id.frame, mFlutterFragment);
                fragmentTransaction.commit();

                //initBasicMessageChannel();

                new Thread(){
                    @Override
                    public void run() {
                        try {
                            sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        initBasicMessageChannel();
                        initEventChannel();
                        initMethodChannel();

                        Log.i(TAG, "mFlutterFragment : " + mFlutterFragment);

                    }
                }.start();
            }
        });

        findViewById(R.id.flutter2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = FlutterActivity
                        .withNewEngine()
                        .initialRoute("启动 FlutterActivity")
                        .build(MainActivity.this);
                intent.putExtra("initParams", "启动 FlutterActivity2");
                startActivity(intent);
            }
        });



    }


    /**
     * BasicMessageChannel 消息传递通道
     */
    private BasicMessageChannel mBasicMessageChannel;

    /**
     * 初始化 BasicMessageChannel
     */
    private void initBasicMessageChannel() {
        // 初始化
        mBasicMessageChannel = new BasicMessageChannel(
                mFlutterFragment.getFlutterEngine().getDartExecutor(),
                "BasicMessageChannel",
                StringCodec.INSTANCE);

        // 设置消息接收监听
        mBasicMessageChannel.setMessageHandler(new BasicMessageChannel.MessageHandler<String>() {
            @Override
            public void onMessage(@Nullable String message, @NonNull BasicMessageChannel.Reply reply) {
                show_message.setText("Dart 通过 BasicMessageChannel 通道向 Native 发送 " + message + " 信息");
            }
        });

        // 点击按钮发送消息 , 并设置 Reply 接收 Dart 返回的消息
        findViewById(R.id.channel1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBasicMessageChannel.send(
                        "Native 通过 BasicMessageChannel 通道发送消息 Hello !",
                        new BasicMessageChannel.Reply() {
                            @Override
                            public void reply(@Nullable Object reply) {
                                show_message.setText("Native 通过 BasicMessageChannel 通道发送消息 Hello 后 , Dart 反馈的信息 ");
                            }
                        }
                );
            }
        });
    }

    /**
     * 与 Flutter 进行消息交互的通道
     */
    private EventChannel mEventChannel;

    private EventChannel.EventSink mEventSink;

    /**
     * 初始化 EventChannel
     */
    private void initEventChannel() {
        // 初始化 EventChannel 实例对象
        mEventChannel = new EventChannel(
                mFlutterFragment.getFlutterEngine().getDartExecutor(),
                "EventChannel");

        Log.i(TAG, "mEventChannel 初始化成功 , mEventChannel : " + mEventChannel);

        mEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            /**
             * 事件流建立成功会回调该方法
             * @param arguments
             * @param events
             */
            @Override
            public void onListen(Object arguments, EventChannel.EventSink events) {
                mEventSink = events;
                Log.i(TAG, "事件流建立成功");
            }

            @Override
            public void onCancel(Object arguments) {
                mEventSink = null;
            }
        });

        Log.i(TAG, "mEventChannel StreamHandler 设置完成");

        findViewById(R.id.channel2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Native 通过 EventChannel 通道发送消息 , mEventSink : " + mEventSink);
                // 点击按钮 , 向 Flutter 端发送数据
                if (mEventSink != null) {
                    mEventSink.success("Native 通过 EventChannel 通道发送消息 Hello !");
                }
            }
        });
    }


    /**
     * 方法调用消息通道
     */
    private MethodChannel mMethodChannel;

    /**
     * 初始化 MethodChannel
     */
    private void initMethodChannel() {
        mMethodChannel = new MethodChannel(mFlutterFragment.getFlutterEngine().getDartExecutor(), "MethodChannel");

        mMethodChannel.setMethodCallHandler(new MethodChannel.MethodCallHandler() {
            @Override
            public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
                show_message.setText("Dart 端通过 MethodChannel 调用 Android 端的 " + call.method + " 方法 , 参数是 " + call.arguments);
            }
        });

        findViewById(R.id.channel3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMethodChannel.invokeMethod("method", "arguments");
            }
        });

    }

}