package com.android.uiautomator;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class OpenDialog
        extends Dialog {
    private static final int FIXED_TEXT_FIELD_WIDTH = 300;
    private static final int DEFAULT_LAYOUT_SPACING = 10;
    private Text mScreenshotText;
    private Text mXmlText;
    private boolean mFileChanged = false;
    private Button mOkButton;
    private static File sScreenshotFile;
    private static File sXmlDumpFile;

    public OpenDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(67680);
    }

    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gl_container = new GridLayout(1, false);
        gl_container.verticalSpacing = 10;
        gl_container.horizontalSpacing = 10;
        gl_container.marginWidth = 10;
        gl_container.marginHeight = 10;
        container.setLayout(gl_container);

        Group openScreenshotGroup = new Group(container, 0);
        openScreenshotGroup.setLayout(new GridLayout(2, false));
        openScreenshotGroup.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        openScreenshotGroup.setText("Screenshot");

        this.mScreenshotText = new Text(openScreenshotGroup, 2056);
        if (sScreenshotFile != null) {
            this.mScreenshotText.setText(sScreenshotFile.getAbsolutePath());
        }
        GridData gd_screenShotText = new GridData(4, 16777216, true, false, 1, 1);
        gd_screenShotText.minimumWidth = 300;
        gd_screenShotText.widthHint = 300;
        this.mScreenshotText.setLayoutData(gd_screenShotText);

        Button openScreenshotButton = new Button(openScreenshotGroup, 0);
        openScreenshotButton.setText("...");
        openScreenshotButton.addListener(13, new Listener() {
            public void handleEvent(Event event) {
                OpenDialog.this.handleOpenScreenshotFile();
            }
        });
        Group openXmlGroup = new Group(container, 0);
        openXmlGroup.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        openXmlGroup.setText("UI XML Dump");
        openXmlGroup.setLayout(new GridLayout(2, false));

        this.mXmlText = new Text(openXmlGroup, 2056);
        this.mXmlText.setEditable(false);
        if (sXmlDumpFile != null) {
            this.mXmlText.setText(sXmlDumpFile.getAbsolutePath());
        }
        GridData gd_xmlText = new GridData(4, 16777216, true, false, 1, 1);
        gd_xmlText.minimumWidth = 300;
        gd_xmlText.widthHint = 300;
        this.mXmlText.setLayoutData(gd_xmlText);

        Button openXmlButton = new Button(openXmlGroup, 0);
        openXmlButton.setText("...");
        openXmlButton.addListener(13, new Listener() {
            public void handleEvent(Event event) {
                OpenDialog.this.handleOpenXmlDumpFile();
            }
        });
        return container;
    }

    protected void createButtonsForButtonBar(Composite parent) {
        this.mOkButton = createButton(parent, 0, IDialogConstants.OK_LABEL, true);
        createButton(parent, 1, IDialogConstants.CANCEL_LABEL, false);
        updateButtonState();
    }

    protected Point getInitialSize() {
        return new Point(368, 233);
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Open UI Dump Files");
    }

    private void handleOpenScreenshotFile() {
        FileDialog fd = new FileDialog(getShell(), 4096);
        fd.setText("Open Screenshot File");
        File initialFile = sScreenshotFile;
        if ((initialFile == null) && (sXmlDumpFile != null) && (sXmlDumpFile.isFile())) {
            initialFile = sXmlDumpFile.getParentFile();
        }
        if (initialFile != null) {
            if (initialFile.isFile()) {
                fd.setFileName(initialFile.getAbsolutePath());
            } else if (initialFile.isDirectory()) {
                fd.setFilterPath(initialFile.getAbsolutePath());
            }
        }
        String[] filter = {"*.png"};
        fd.setFilterExtensions(filter);
        String selected = fd.open();
        if (selected != null) {
            sScreenshotFile = new File(selected);
            this.mScreenshotText.setText(selected);
            this.mFileChanged = true;
        }
        updateButtonState();
    }

    private void handleOpenXmlDumpFile() {
        FileDialog fd = new FileDialog(getShell(), 4096);
        fd.setText("Open UI Dump XML File");
        File initialFile = sXmlDumpFile;
        if ((initialFile == null) && (sScreenshotFile != null) && (sScreenshotFile.isFile())) {
            initialFile = sScreenshotFile.getParentFile();
        }
        if (initialFile != null) {
            if (initialFile.isFile()) {
                fd.setFileName(initialFile.getAbsolutePath());
            } else if (initialFile.isDirectory()) {
                fd.setFilterPath(initialFile.getAbsolutePath());
            }
        }
        String initialPath = this.mXmlText.getText();
        if ((initialPath.isEmpty()) && (sScreenshotFile != null) && (sScreenshotFile.isFile())) {
            initialPath = sScreenshotFile.getParentFile().getAbsolutePath();
        }
        String[] filter = {"*.uix"};
        fd.setFilterExtensions(filter);
        String selected = fd.open();
        if (selected != null) {
            sXmlDumpFile = new File(selected);
            this.mXmlText.setText(selected);
            this.mFileChanged = true;
        }
        updateButtonState();
    }

    private void updateButtonState() {
        this.mOkButton.setEnabled((sXmlDumpFile != null) && (sXmlDumpFile.isFile()));
    }

    public boolean hasFileChanged() {
        return this.mFileChanged;
    }

    public File getScreenshotFile() {
        return sScreenshotFile;
    }

    public File getXmlDumpFile() {
        return sXmlDumpFile;
    }
}
