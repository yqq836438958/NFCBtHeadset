
package com.pace.channel.headset;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pace.channel.AbsBTChannel;

import java.util.HashMap;
import java.util.Map;

public class HeadsetChannel extends AbsBTChannel {
    private class AudioStatChanged implements RecvHandler {
        private Map<Integer, String> mDbgMap = new HashMap<Integer, String>();

        public AudioStatChanged() {
            mDbgMap.put(BluetoothHeadset.STATE_AUDIO_CONNECTED, "headset audio connected:");
            mDbgMap.put(BluetoothHeadset.STATE_AUDIO_CONNECTING, "headset audio connecting:");
            mDbgMap.put(BluetoothHeadset.STATE_AUDIO_DISCONNECTED, "headset audio disconnected:");
        }

        @Override
        public void onReceive(Context context, Intent intent, BluetoothDevice dev, int status) {
            if (mDbgMap.containsKey(status)) {
                Log.d(TAG, mDbgMap.get(status) + dev.getName());
            }

        }

    }

    private class HeadsetConStatChanged implements RecvHandler {
        private Map<Integer, String> mDbgMap = new HashMap<Integer, String>();

        public HeadsetConStatChanged() {
            mDbgMap.put(BluetoothHeadset.STATE_CONNECTED, "headset connected:");
            mDbgMap.put(BluetoothHeadset.STATE_CONNECTING, "headset connecting:");
            mDbgMap.put(BluetoothHeadset.STATE_DISCONNECTED, "headset audio disconnected:");
            mDbgMap.put(BluetoothHeadset.STATE_DISCONNECTING, "headset audio disconnecting:");
        }

        @Override
        public void onReceive(Context context, Intent intent, BluetoothDevice dev, int status) {
            if (mDbgMap.containsKey(status)) {
                Log.d(TAG, mDbgMap.get(status) + dev.getName());
            }
        }

    }

    public HeadsetChannel(Context context) {
        super(context, BluetoothProfile.HEADSET);
        addHandler(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED, new AudioStatChanged());
        addHandler(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED, new HeadsetConStatChanged());
        registReceiver();
    }

}
