
package com.pace.channel;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.pace.bean.RemoteDev;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public abstract class AbsBTChannel implements IChannel {
    protected static final String TAG = "Channel";
    protected BluetoothAdapter mBluetoothAdapter = null;
    protected BluetoothProfile mBluetoothProfile = null;
    protected Context mContext = null;
    private Map<String, RecvHandler> mHandlerMap = null;
    private IntentFilter mAdapterIntentFilters = null;
    private ConcurrentHashMap<String, IChannelCallback> mPendingCallbackMap = new ConcurrentHashMap<String, IChannelCallback>();
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            RecvHandler handler = mHandlerMap.get(action);
            int profStat = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
            preCheckConnectStatus(device, profStat);
            if (handler != null) {
                handler.onReceive(context, intent, device, profStat);
            }
        }
    };

    private void preCheckConnectStatus(BluetoothDevice device, int status) {
        IChannelCallback callback = mPendingCallbackMap.remove(device.getAddress());
        if (BluetoothProfile.STATE_CONNECTED == status) {
            // connected!!!
            postConnectResult(callback, IChannelCallback.STAT_CONNECT_OK);
        } else if (BluetoothProfile.STATE_DISCONNECTED == status) {
            // disconnected
            postConnectResult(callback, IChannelCallback.STAT_CONNECT_ERR);
        }
    }

    public AbsBTChannel(Context context, int profile) {
        mContext = context;
        BluetoothManager bluetoothManager = (BluetoothManager) context
                .getSystemService(Context.BLUETOOTH_SERVICE);
        mHandlerMap = new HashMap<String, AbsBTChannel.RecvHandler>();
        mAdapterIntentFilters = new IntentFilter();
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
        mBluetoothProfile = getBtProfile(profile);
    }

    protected void registReceiver() {
        mContext.registerReceiver(mBroadcastReceiver, mAdapterIntentFilters);
    }

    protected void unregistReceiver() {
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    protected void addHandler(String action, RecvHandler handler) {
        mHandlerMap.put(action, handler);
        mAdapterIntentFilters.addAction(action);
    }

    public static interface RecvHandler {
        void onReceive(Context context, Intent intent, BluetoothDevice dev, int status);
    }

    protected boolean isEnable() {
        return mBluetoothAdapter != null ? mBluetoothAdapter.isEnabled() : false;
    }

    protected boolean enable() {
        if (isEnable()) {
            return true;
        }
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.enable();
        }
        return false;
    }

    protected void disable() {
        if (!isEnable()) {
            return;
        }
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.disable();
        }
    }

    private void postConnectResult(IChannelCallback callback, int result) {
        if (callback != null) {
            callback.onConnectResult(result);
        }
    }

    @Override
    public synchronized void connect(RemoteDev dev, IChannelCallback callback) {
        if (mBluetoothAdapter == null || mBluetoothProfile == null || dev == null) {
            postConnectResult(callback, IChannelCallback.STAT_CONNECT_ERR);
            return;
        }
        String mac = dev.getDevMac();
        if (TextUtils.isEmpty(mac)) {
            postConnectResult(callback, IChannelCallback.STAT_CONNECT_ERR);
            return;
        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);
        int conStat = mBluetoothProfile.getConnectionState(device);
        int result = IChannelCallback.STAT_CONNECT_INIT;
        switch (conStat) {
            case BluetoothProfile.STATE_CONNECTED:
                result = IChannelCallback.STAT_CONNECT_IGONRE;
                break;
            case BluetoothProfile.STATE_CONNECTING:
                result = IChannelCallback.STAT_CONNECT_BUSY;
                break;
            default:
                break;
        }
        if (result != IChannelCallback.STAT_CONNECT_INIT) {
            postConnectResult(callback, result);
            return;
        }
        if (!connectDev(device)) {
            postConnectResult(callback, IChannelCallback.STAT_CONNECT_ERR);
            return;
        }
        mPendingCallbackMap.put(mac, callback);
    }

    @Override
    public synchronized void disconnect(RemoteDev dev) {
        if (dev == null) {
            return;
        }
        disconnectDev(dev.getDevMac());
        mPendingCallbackMap.remove(dev.getDevMac());
    }

    @Override
    public boolean isAvailble() {
        boolean btOn = mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
        boolean profileOn = (mBluetoothProfile != null);
        return btOn && profileOn;
    }

    private BluetoothProfile getBtProfile(int profileType) {
        if (mBluetoothAdapter == null) {
            return null;
        }
        SyncProfServiceCb syncProfServiceCb = new SyncProfServiceCb();
        mBluetoothAdapter.getProfileProxy(mContext, syncProfServiceCb, profileType);
        return syncProfServiceCb.waitResponse();
    }

    private void disconnectDev(String remoteDev) {
        Log.i(TAG, "disconnect");
        if (mBluetoothProfile == null || TextUtils.isEmpty(remoteDev)) {
            return;
        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(remoteDev.toUpperCase());
        if (device == null) {
            return;
        }
        try {
            Method disconnect = mBluetoothProfile.getClass().getDeclaredMethod("disconnect",
                    BluetoothDevice.class);
            disconnect.setAccessible(true);
            disconnect.invoke(mBluetoothProfile, device);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private boolean connectDev(BluetoothDevice device) {
        if (mBluetoothProfile == null || device == null) {
            return false;
        }
        if (device.getBondState() != BluetoothDevice.BOND_BONDED
                && device.getBondState() != BluetoothDevice.BOND_BONDING) {
            if (!device.createBond()) {
                return false;
            }
        }
        boolean invokeRet = false;
        try {
            Method connect = mBluetoothProfile.getClass().getDeclaredMethod("connect",
                    BluetoothDevice.class);
            connect.setAccessible(true);
            invokeRet = (Boolean) connect.invoke(mBluetoothProfile, device);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return invokeRet;
    }

    class SyncProfServiceCb implements BluetoothProfile.ServiceListener {
        private Semaphore mSemaphore = null;
        private BluetoothProfile mProfile = null;

        SyncProfServiceCb() {
            mSemaphore = new Semaphore(0);
        }

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            mProfile = proxy;
            mSemaphore.release();
        }

        @Override
        public void onServiceDisconnected(int profile) {
            mProfile = null;
            mSemaphore.release();
        }

        public BluetoothProfile waitResponse() {
            try {
                mSemaphore.acquire();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return mProfile;
        }
    }
}
