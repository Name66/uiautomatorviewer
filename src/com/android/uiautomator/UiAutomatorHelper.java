package com.android.uiautomator;

import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.SyncService;
import com.android.uiautomator.tree.BasicTreeNode;
import com.android.uiautomator.tree.RootWindowNode;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;

public class UiAutomatorHelper {
    public static final int UIAUTOMATOR_MIN_API_LEVEL = 16;
    private static final String UIAUTOMATOR = "/system/bin/uiautomator";
    private static final String UIAUTOMATOR_DUMP_COMMAND = "dump";
    private static final String UIDUMP_DEVICE_PATH = "/data/local/tmp/uidump.xml";
    private static final int XML_CAPTURE_TIMEOUT_SEC = 40;

    private static boolean supportsUiAutomator(IDevice device) {
        String apiLevelString = device.getProperty("ro.build.version.sdk");
        int apiLevel;
        try {
            apiLevel = Integer.parseInt(apiLevelString);
        } catch (NumberFormatException e) {
//            int apiLevel;
            apiLevel = 16;
        }
        return apiLevel >= 16;
    }

    private static void getUiHierarchyFile(IDevice device, File dst, IProgressMonitor monitor, boolean compressed) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.subTask("Deleting old UI XML snapshot ...");
        String command = "rm /data/local/tmp/uidump.xml";
        try {
            CountDownLatch commandCompleteLatch = new CountDownLatch(1);
            device.executeShellCommand(command, new CollectingOutputReceiver(commandCompleteLatch));

            commandCompleteLatch.await(5L, TimeUnit.SECONDS);
        } catch (Exception localException1) {
        }
        monitor.subTask("Taking UI XML snapshot...");
        if (compressed) {
            command = String.format("%s %s --compressed %s", new Object[]{"/system/bin/uiautomator", "dump", "/data/local/tmp/uidump.xml"});
        } else {
            command = String.format("%s %s %s", new Object[]{"/system/bin/uiautomator", "dump", "/data/local/tmp/uidump.xml"});
        }
        CountDownLatch commandCompleteLatch = new CountDownLatch(1);
        try {
            device.executeShellCommand(command, new CollectingOutputReceiver(commandCompleteLatch), 40000);

            commandCompleteLatch.await(40L, TimeUnit.SECONDS);

            monitor.subTask("Pull UI XML snapshot from device...");
            device.getSyncService().pullFile("/data/local/tmp/uidump.xml", dst
                    .getAbsolutePath(), SyncService.getNullProgressMonitor());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static UiAutomatorResult takeSnapshot(IDevice device, IProgressMonitor monitor)
            throws UiAutomatorHelper.UiAutomatorException {
        return takeSnapshot(device, monitor, false);
    }

    public static UiAutomatorResult takeSnapshot(IDevice device, IProgressMonitor monitor, boolean compressed)
            throws UiAutomatorHelper.UiAutomatorException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.subTask("Checking if device support UI Automator");
        if (!supportsUiAutomator(device)) {
            String msg = "UI Automator requires a device with API Level 16";

            throw new UiAutomatorException(msg, null);
        }
        monitor.subTask("Creating temporary files for uiautomator results.");
        File tmpDir = null;
        File xmlDumpFile = null;
        File screenshotFile = null;
        try {
            tmpDir = File.createTempFile("uiautomatorviewer_", "");
            tmpDir.delete();
            if (!tmpDir.mkdirs()) {
                throw new IOException("Failed to mkdir");
            }
            xmlDumpFile = File.createTempFile("dump_", ".uix", tmpDir);
            screenshotFile = File.createTempFile("screenshot_", ".png", tmpDir);
        } catch (Exception e) {
            String msg = "Error while creating temporary file to save snapshot: " + e.getMessage();
            throw new UiAutomatorException(msg, e);
        }
        tmpDir.deleteOnExit();
        xmlDumpFile.deleteOnExit();
        screenshotFile.deleteOnExit();

        monitor.subTask("Obtaining UI hierarchy");
        try {
            getUiHierarchyFile(device, xmlDumpFile, monitor, compressed);
        } catch (Exception e) {
            String msg = "Error while obtaining UI hierarchy XML file: " + e.getMessage();
            throw new UiAutomatorException(msg, e);
        }
        UiAutomatorModel model;
        try {
            model = new UiAutomatorModel(xmlDumpFile);
        } catch (Exception e) {
//            UiAutomatorModel model;
            String msg = "Error while parsing UI hierarchy XML file: " + e.getMessage();
            throw new UiAutomatorException(msg, e);
        }
//        UiAutomatorModel model;
        RawImage rawImage;
        monitor.subTask("Obtaining device screenshot");
        try {
            rawImage = device.getScreenshot();
        } catch (Exception e) {
//            RawImage rawImage;
            String msg = "Error taking device screenshot: " + e.getMessage();
            throw new UiAutomatorException(msg, e);
        }
//        RawImage rawImage;
        BasicTreeNode root = model.getXmlRootNode();
        if ((root instanceof RootWindowNode)) {
            for (int i = 0; i < ((RootWindowNode) root).getRotation(); i++) {
                rawImage = rawImage.getRotated();
            }
        }
        PaletteData palette = new PaletteData(rawImage.getRedMask(), rawImage.getGreenMask(), rawImage.getBlueMask());
        ImageData imageData = new ImageData(rawImage.width, rawImage.height, rawImage.bpp, palette, 1, rawImage.data);

        ImageLoader loader = new ImageLoader();
        loader.data = new ImageData[]{imageData};
        loader.save(screenshotFile.getAbsolutePath(), 5);
        Image screenshot = new Image(Display.getDefault(), imageData);

        return new UiAutomatorResult(xmlDumpFile, model, screenshot);
    }

    public static class UiAutomatorException
            extends Exception {
        public UiAutomatorException(String msg, Throwable t) {
            super(t);
        }
    }

    public static class UiAutomatorResult {
        public final File uiHierarchy;
        public final UiAutomatorModel model;
        public final Image screenshot;

        public UiAutomatorResult(File uiXml, UiAutomatorModel m, Image s) {
            this.uiHierarchy = uiXml;
            this.model = m;
            this.screenshot = s;
        }
    }
}
