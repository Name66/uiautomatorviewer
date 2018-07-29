package com.android.uiautomator.actions;

import com.android.uiautomator.UiAutomatorViewer;
import com.google.common.io.Files;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;

public class SaveScreenShotAction
        extends Action {
    private static final String PNG_TYPE = ".png";
    private static final String UIX_TYPE = ".uix";
    private UiAutomatorViewer mViewer;

    public SaveScreenShotAction(UiAutomatorViewer viewer) {
        super("&Save");
        this.mViewer = viewer;
    }

    public ImageDescriptor getImageDescriptor() {
        return ImageHelper.loadImageDescriptorFromResource("images/save.png");
    }

    public void run() {
        final Image screenshot = this.mViewer.getScreenShot();
        final File model = this.mViewer.getModelFile();
        if ((model == null) || (screenshot == null)) {
            return;
        }
        DirectoryDialog dd = new DirectoryDialog(Display.getDefault().getActiveShell());
        dd.setText("Save Screenshot and UiX Files");
        final String path = dd.open();
        if (path == null) {
            return;
        }
        new Thread() {
            String filepath;

            public void run() {
                this.filepath = new File(path, model.getName()).toString();
                this.filepath = this.filepath.substring(0, this.filepath.lastIndexOf("."));
                ImageLoader imageLoader = new ImageLoader();

                imageLoader.data = new ImageData[]{screenshot.getImageData()};
                try {
                    imageLoader.save(this.filepath + ".png", 5);
                    Files.copy(model, new File(this.filepath + ".uix"));
                } catch (Exception e) {
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            Status status = new Status(IStatus.ERROR, "Error writing file", e.getLocalizedMessage());
                            ErrorDialog.openError(Display.getDefault().getActiveShell(),
                                    String.format("Error writing %s.uix", filepath), e
                                            .getLocalizedMessage(), status);
                        }
                    });
                }
            }
        }.start();
    }
}
