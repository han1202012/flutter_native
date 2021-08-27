package com.example.flutter_native;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.android.FlutterFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.flutter1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction =
                        getSupportFragmentManager().beginTransaction();
                // 创建 FlutterFragment
                fragmentTransaction.replace(R.id.frame, FlutterFragment.createDefault());
                fragmentTransaction.commit();
            }
        });

        findViewById(R.id.flutter2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = FlutterActivity
                        .withNewEngine()
                        .initialRoute("Android 启动 FlutterActivity")
                        .build(MainActivity.this);
                //intent.putExtra("initParams", "Android 中 Activity 启动 Flutter");
                startActivity(intent);
            }
        });

    }
}