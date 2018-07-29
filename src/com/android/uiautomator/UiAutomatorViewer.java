package com.android.uiautomator;

import com.android.uiautomator.actions.OpenFilesAction;
import com.android.uiautomator.actions.SaveScreenShotAction;
import com.android.uiautomator.actions.ScreenshotAction;

import java.io.File;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

public class UiAutomatorViewer
        extends ApplicationWindow {
    private UiAutomatorView mUiAutomatorView;

    public UiAutomatorViewer() {
        super(null);
    }

    protected Control createContents(Composite parent) {
        Composite c = new Composite(parent, 2048);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        c.setLayout(gridLayout);

        GridData gd = new GridData(768);
        c.setLayoutData(gd);

        ToolBarManager toolBarManager = new ToolBarManager(8388608);
        toolBarManager.add(new OpenFilesAction(this));
        toolBarManager.add(new ScreenshotAction(this, false));
        toolBarManager.add(new ScreenshotAction(this, true));
        toolBarManager.add(new SaveScreenShotAction(this));
        ToolBar tb = toolBarManager.createControl(c);
        tb.setLayoutData(new GridData(768));

        this.mUiAutomatorView = new UiAutomatorView(c, 2048);
        this.mUiAutomatorView.setLayoutData(new GridData(1808));

        return parent;
    }

    public static void main(String[] args) {

        try {
            UiAutomatorViewer window = new UiAutomatorViewer();
            window.setBlockOnOpen(true);
            window.open();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DebugBridge.terminate();
        }
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("UI Automator Viewer");
    }

    protected Point getInitialSize() {
        return new Point(800, 600);
    }

    public void setModel(final UiAutomatorModel model, final File modelFile, final Image screenshot) {
        if (Display.getDefault().getThread() != Thread.currentThread()) {
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    UiAutomatorViewer.this.mUiAutomatorView.setModel(model, modelFile, screenshot);
                }
            });
        } else {
            this.mUiAutomatorView.setModel(model, modelFile, screenshot);
        }
    }

    public Image getScreenShot() {
        return this.mUiAutomatorView.getScreenShot();
    }

    public File getModelFile() {
        return this.mUiAutomatorView.getModelFile();
    }
}
