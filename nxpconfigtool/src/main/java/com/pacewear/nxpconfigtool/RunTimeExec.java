package com.pacewear.nxpconfigtool;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by p_qingyuan on 2017/9/15.
 */

public class RunTimeExec {
    public static void runSu(String... str) {
        Runtime runtime = Runtime.getRuntime();
        Process proc = null;
        try {
            proc = runtime.exec("/system/xbin/su");
//            String[] command = new String[]{"su","input tap 200 200"};
//            proc = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            Log.e("yqq","e:"+e.getMessage());
            e.printStackTrace();
        }
        if(proc == null){
            Log.e("yqq","proc is null so return");
            return;
        }
        DataOutputStream os = new DataOutputStream(proc.getOutputStream());
        try {
            for (String tmp : str) {
                os.writeBytes(tmp + "\n");
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public  static  String do_exec(String cmd) {
        String s = "/n";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                s += line + "/n";
            }
        } catch (IOException e) {
            Log.d("yqq","e:"+e.getMessage());
            e.printStackTrace();
        }
        return s;
    }
}
