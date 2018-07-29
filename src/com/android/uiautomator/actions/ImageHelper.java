package com.android.uiautomator.actions;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

public class ImageHelper {
    public static ImageDescriptor loadImageDescriptorFromResource(String path) {
        InputStream is = ImageHelper.class.getClassLoader().getResourceAsStream(path);
        if (is != null) {
            ImageData[] data = null;
            try {
                data = new ImageLoader().load(is);
            } catch (SWTException localSWTException) {
            } finally {
                try {
                    is.close();
                } catch (IOException localIOException2) {
                }
            }
            if (data != null && data.length > 0) {
                return ImageDescriptor.createFromImageData(data[0]);
            }
        }
        return null;
    }
}
