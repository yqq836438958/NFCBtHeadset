
package com.pace.channel;

public interface IChannelCallback {
    public static final int STAT_CONNECT_INIT = 999;
    public static final int STAT_CONNECT_ERR = -1;
    public static final int STAT_CONNECT_IGONRE = 1;// 已经连接，不需在连接
    public static final int STAT_CONNECT_BUSY = -2;// 当前设备正在连接
    public static final int STAT_CONNECT_OK = 0;

    public void onConnectResult(int result);
}
