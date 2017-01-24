package com.cylan.ext.processor;


import com.cylan.ext.annotations.DPTarget;

/**
 * Created by yzd on 17-1-14.
 */

public class GeneratorFactory {

    public static Generator getGeneratorInstance(DPTarget target) {
        Generator result = null;
        switch (target) {
            case DEVICE:
                result = new JFGDeviceParentGenerator();
                break;
            case ACCOUNT:
                result = new JFGAccountGenerator();
                break;
            case DATAPOINT:
            case DOORBELL:
            case CAMERA:
            case EFAMILY:
            case MAGNETOMETER:
                result = new JFGDeviceInstanceGenerator();
                break;
        }
        result.setTarget(target);
        return result;
    }
}
