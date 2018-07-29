package com.android.uiautomator.actions;

import com.android.uiautomator.UiAutomatorView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

public class ToggleNafAction
        extends Action {
    private UiAutomatorView mView;

    public ToggleNafAction(UiAutomatorView view) {
        super("&Toggle NAF Nodes", IAction.AS_CHECK_BOX);
        setChecked(view.shouldShowNafNodes());

        mView = view;
    }

    public ImageDescriptor getImageDescriptor() {
        return ImageHelper.loadImageDescriptorFromResource("images/warning.png");
    }

    public void run() {
        mView.toggleShowNaf();
        mView.redrawScreenshot();
        setChecked(mView.shouldShowNafNodes());
    }
}
