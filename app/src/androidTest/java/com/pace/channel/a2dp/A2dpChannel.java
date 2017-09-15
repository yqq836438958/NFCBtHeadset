
package com.pace.channel.a2dp;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pace.channel.AbsBTChannel;

import java.util.HashMap;
import java.util.Map;

public class A2dpChannel extends AbsBTChannel {

    private class A2dpConStatChanged implements RecvHandler {
        private Map<Integer, String> mDbgMap = new HashMap<Integer, String>();

        A2dpConStatChanged() {
            mDbgMap.put(BluetoothA2dp.STATE_CONNECTING, "A2DP dev connecting:");
            mDbgMap.put(BluetoothA2dp.STATE_CONNECTED, "A2DP dev connected:");
            mDbgMap.put(BluetoothA2dp.STATE_DISCONNECTED, "A2DP dev disconnected:");
            mDbgMap.put(BluetoothA2dp.STATE_DISCONNECTING, "A2DP dev disconnecting:");
        }

        @Override
        public void onReceive(Context context, Intent intent, BluetoothDevice dev, int status) {
            if (mDbgMap.containsKey(status)) {
                Log.d(TAG, mDbgMap.get(status) + dev.getName());
            }
        }

    }

    private class A2dpPlayChanged implements RecvHandler {
        private Map<Integer, String> mDbgMap = new HashMap<Integer, String>();

        A2dpPlayChanged() {
            mDbgMap.put(BluetoothA2dp.STATE_PLAYING, "A2DP dev isPlaying:");
            mDbgMap.put(BluetoothA2dp.STATE_NOT_PLAYING, "A2DP dev notPlaying:");
        }

        @Override
        public void onReceive(Context context, Intent intent, BluetoothDevice dev, int status) {
            if (mDbgMap.containsKey(status)) {
                Log.d(TAG, mDbgMap.get(status) + dev.getName());
            }
        }
    }

    public A2dpChannel(Context context) {
        super(context, BluetoothProfile.A2DP);
        addHandler(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED, new A2dpConStatChanged());
        addHandler(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED, new A2dpPlayChanged());
        registReceiver();
    }

}
