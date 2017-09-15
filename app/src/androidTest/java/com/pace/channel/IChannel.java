
package com.pace.channel;

import com.pace.bean.RemoteDev;

public interface IChannel {
    public void connect(RemoteDev dev, IChannelCallback callback);

    public void disconnect(RemoteDev dev);

    public boolean isAvailble();
}
