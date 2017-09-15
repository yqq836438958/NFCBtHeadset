
package com.pace.bean;

import java.io.Serializable;

public class RemoteDev implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String mDevMac = null;
    private String mDevName = null;

    public RemoteDev(String mac) {
        mDevMac = mac;
    }

    public RemoteDev() {

    }

    public String getDevMac() {
        return mDevMac;
    }

    public String getDevName() {
        return mDevName;
    }

    public void setDevMac(String mac) {
        mDevMac = mac;
    }

    public void setDevName(String name) {
        mDevName = name;
    }
}
