package com.android.uiautomator.tree;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class BasicTreeNodeContentProvider
        implements ITreeContentProvider {
    private static final Object[] EMPTY_ARRAY = new Object[0];

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    public Object[] getChildren(Object parentElement) {
        if ((parentElement instanceof BasicTreeNode)) {
            return ((BasicTreeNode) parentElement).getChildren();
        }
        return EMPTY_ARRAY;
    }

    public Object getParent(Object element) {
        if ((element instanceof BasicTreeNode)) {
            return ((BasicTreeNode) element).getParent();
        }
        return null;
    }

    public boolean hasChildren(Object element) {
        if ((element instanceof BasicTreeNode)) {
            return ((BasicTreeNode) element).hasChild();
        }
        return false;
    }
}
