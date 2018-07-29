package com.android.uiautomator.actions;

import com.android.ddmlib.IDevice;
import com.android.uiautomator.DebugBridge;
import com.android.uiautomator.UiAutomatorHelper;
import com.android.uiautomator.UiAutomatorHelper.UiAutomatorException;
import com.android.uiautomator.UiAutomatorHelper.UiAutomatorResult;
import com.android.uiautomator.UiAutomatorViewer;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ScreenshotAction
        extends Action {
    UiAutomatorViewer mViewer;
    private boolean mCompressed;

    public ScreenshotAction(UiAutomatorViewer viewer, boolean compressed) {
        super("&Device Screenshot " + (compressed ? "with Compressed Hierarchy" : "") + "(uiautomator dump" + (compressed ? " --compressed)" : ")"));

        this.mViewer = viewer;
        this.mCompressed = compressed;
    }

    public ImageDescriptor getImageDescriptor() {
        if (this.mCompressed) {
            return ImageHelper.loadImageDescriptorFromResource("images/screenshotcompressed.png");
        }
        return ImageHelper.loadImageDescriptorFromResource("images/screenshot.png");
    }

    public void run() {
        if (!DebugBridge.isInitialized()) {
            MessageDialog.openError(this.mViewer.getShell(), "Error obtaining Device Screenshot", "Unable to connect to adb. Check if adb is installed correctly.");

            return;
        }
        final IDevice device = pickDevice();
        if (device == null) {
            return;
        }
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(this.mViewer.getShell());
        try {
            dialog.run(true, false, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    UiAutomatorHelper.UiAutomatorResult result = null;
                    try {
                        result = UiAutomatorHelper.takeSnapshot(device, monitor, ScreenshotAction.this.mCompressed);
                    } catch (UiAutomatorHelper.UiAutomatorException e) {
                        monitor.done();
                        ScreenshotAction.this.showError(e.getMessage(), e);
                        return;
                    }
                    ScreenshotAction.this.mViewer.setModel(result.model, result.uiHierarchy, result.screenshot);
                    monitor.done();
                }
            });
        } catch (Exception e) {
            showError("Unexpected error while obtaining UI hierarchy", e);
        }
    }

    private void showError(final String msg, final Throwable t) {
        this.mViewer.getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                Status s = new Status(4, "Screenshot", msg, t);
                ErrorDialog.openError(ScreenshotAction.this.mViewer
                        .getShell(), "Error", "Error obtaining UI hierarchy", s);
            }
        });
    }

    private IDevice pickDevice() {
        List<IDevice> devices = DebugBridge.getDevices();
        if (devices.size() == 0) {
            MessageDialog.openError(this.mViewer.getShell(), "Error obtaining Device Screenshot", "No Android devices were detected by adb.");

            return null;
        }
        if (devices.size() == 1) {
            return devices.get(0);
        }
        DevicePickerDialog dlg = new DevicePickerDialog(this.mViewer.getShell(), devices);
        if (dlg.open() != 0) {
            return null;
        }
        return dlg.getSelectedDevice();
    }

    private static class DevicePickerDialog
            extends Dialog {
        private final List<IDevice> mDevices;
        private final String[] mDeviceNames;
        private static int sSelectedDeviceIndex;

        public DevicePickerDialog(Shell parentShell, List<IDevice> devices) {
            super(parentShell);

            this.mDevices = devices;
            this.mDeviceNames = new String[this.mDevices.size()];
            for (int i = 0; i < devices.size(); i++) {
                this.mDeviceNames[i] = devices.get(i).getName();
            }
        }

        protected Control createDialogArea(Composite parentShell) {
            Composite parent = (Composite) super.createDialogArea(parentShell);
            Composite c = new Composite(parent, SWT.NONE);

            c.setLayout(new GridLayout(2, false));

            Label l = new Label(c, SWT.NONE);
            l.setText("Select device: ");

            final Combo combo = new Combo(c, 2056);
            combo.setItems(this.mDeviceNames);

            int defaultSelection = sSelectedDeviceIndex < this.mDevices.size() ? sSelectedDeviceIndex : 0;
            combo.select(defaultSelection);
            sSelectedDeviceIndex = defaultSelection;

            combo.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent arg0) {
                    sSelectedDeviceIndex = combo.getSelectionIndex();
                }
            });
            return parent;
        }

        public IDevice getSelectedDevice() {
            return (IDevice) this.mDevices.get(sSelectedDeviceIndex);
        }
    }
}
