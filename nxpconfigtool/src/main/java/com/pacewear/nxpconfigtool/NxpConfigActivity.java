package com.pacewear.nxpconfigtool;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class NxpConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nxp_config);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RunTimeExec.runSu("cp /sdcard/env.ini  /system/test/env.ini");
//                        String out = RunTimeExec.do_exec("mv /sdcard/env.ini  /sdcard/env.iniaaa");
//                        String out = RunTimeExec.do_exec("ls");
//                        Log.d("yqq","out/:"+out);
                    }
                }).start();
            }
        });
    }
}
