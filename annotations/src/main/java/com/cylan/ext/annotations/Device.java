package com.cylan.ext.annotations;

/**
 * Created by cylan-hunt on 16-11-16.
 */

public enum Device {

    CAMERA,//
    BELL,//
    MAG,//
    EFamily,
    CLOUD;//
//    ALL;//



    Device() {
    }

    public String getName(Device device) {
        return device.name();
    }
}
