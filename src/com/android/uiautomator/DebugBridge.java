package com.android.uiautomator;

import com.android.SdkConstants;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class DebugBridge {
    private static AndroidDebugBridge sDebugBridge;

    private static String getAdbLocation() {
        String toolsDir = System.getProperty("com.android.uiautomator.bindir");
        if (toolsDir == null) {
            return null;
        }
        File sdk = new File(toolsDir).getParentFile();

        File platformTools = new File(sdk, "platform-tools");
        File adb = new File(platformTools, SdkConstants.FN_ADB);
        if (adb.exists()) {
            return adb.getAbsolutePath();
        }
        adb = new File(toolsDir, SdkConstants.FN_ADB);
        if (adb.exists()) {
            return adb.getAbsolutePath();
        }
        String androidOut = System.getenv("ANDROID_HOST_OUT");
        if (androidOut != null) {
            String adbLocation = androidOut + File.separator + "bin" + File.separator + SdkConstants.FN_ADB;
            if (new File(adbLocation).exists()) {
                return adbLocation;
            }
        }
        return null;
    }

    public static void init() {
        String adbLocation = getAdbLocation();
        if (adbLocation != null) {
            AndroidDebugBridge.init(false);
            sDebugBridge = AndroidDebugBridge.createBridge(adbLocation, false);
        }
    }

    public static void terminate() {
        if (sDebugBridge != null) {
            sDebugBridge = null;
            AndroidDebugBridge.terminate();
        }
    }

    public static boolean isInitialized() {
        return sDebugBridge != null;
    }

    public static List<IDevice> getDevices() {
        return Arrays.asList(sDebugBridge.getDevices());
    }
}
