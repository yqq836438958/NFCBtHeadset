
package com.pace.channel;

import android.content.Context;

import com.pace.bean.RemoteDev;
import com.pace.channel.a2dp.A2dpChannel;
import com.pace.channel.headset.HeadsetChannel;

import java.util.ArrayList;
import java.util.List;

public class ChannelMgr {
    private List<IChannel> mChannels = null;
    private static ChannelMgr sInstance = null;

    public static ChannelMgr get(Context context) {
        if (sInstance == null) {
            synchronized (ChannelMgr.class) {
                if (sInstance == null) {
                    sInstance = new ChannelMgr(context);
                }
            }
        }
        return sInstance;
    }

    private ChannelMgr(Context context) {
        mChannels = new ArrayList<IChannel>();
        mChannels.add(new A2dpChannel(context));
        mChannels.add(new HeadsetChannel(context));
    }

    public void open(RemoteDev dev) {
        for (IChannel channel : mChannels) {
            channel.connect(dev, null);
        }
    }

    public void close(RemoteDev dev) {
        for (IChannel channel : mChannels) {
            channel.disconnect(dev);
        }
    }
}
