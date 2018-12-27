package com.zhouqianbin.demo.ui;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhouqianbin.demo.R;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.speech_synth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SpeechSynthesizerActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.speech_recong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SpeechRecognizerActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.speech_wake_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,WakeUpActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.speech_grammar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,GrammarRecognizerActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.speech_aiui).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AiuiActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.speech_tools).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,XunfeiEngineTestActivity.class);
                startActivity(intent);
            }
        });



        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .requestEach(Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_PHONE_STATE)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            Log.d(TAG,"权限获取成功");
                            // `permission.name` is granted !
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // Denied permission without ask never again
                            Log.d(TAG,"权限获取失败");
                        } else {
                            // Denied permission with ask never again
                            // Need to go to the settings
                            Log.d(TAG,"权限获取失败");
                        }
                    }
                });


    }
}
