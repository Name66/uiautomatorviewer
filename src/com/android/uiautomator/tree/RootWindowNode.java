package com.android.uiautomator.tree;

public class RootWindowNode
        extends BasicTreeNode {
    private final String mWindowName;
    private Object[] mCachedAttributesArray;
    private int mRotation;

    public RootWindowNode(String windowName) {
        this(windowName, 0);
    }

    public RootWindowNode(String windowName, int rotation) {
        this.mWindowName = windowName;
        this.mRotation = rotation;
    }

    public String toString() {
        return this.mWindowName;
    }

    public Object[] getAttributesArray() {
        if (this.mCachedAttributesArray == null) {
            this.mCachedAttributesArray = new Object[]{new AttributePair("window-name", this.mWindowName)};
        }
        return this.mCachedAttributesArray;
    }

    public int getRotation() {
        return this.mRotation;
    }
}
