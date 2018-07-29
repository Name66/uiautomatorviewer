package com.android.uiautomator.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasicTreeNode {
    private static final BasicTreeNode[] CHILDREN_TEMPLATE = new BasicTreeNode[0];
    protected BasicTreeNode mParent;
    protected final List<BasicTreeNode> mChildren;
    public int x;
    public int y;
    public int width;
    public int height;
    protected boolean mHasBounds;

    public BasicTreeNode() {
        this.mChildren = new ArrayList();

        this.mHasBounds = false;
    }

    public void addChild(BasicTreeNode child) {
        if (child == null) {
            throw new NullPointerException("Cannot add null child");
        }
        if (this.mChildren.contains(child)) {
            throw new IllegalArgumentException("node already a child");
        }
        this.mChildren.add(child);
        child.mParent = this;
    }

    public List<BasicTreeNode> getChildrenList() {
        return Collections.unmodifiableList(this.mChildren);
    }

    public BasicTreeNode[] getChildren() {
        return (BasicTreeNode[]) this.mChildren.toArray(CHILDREN_TEMPLATE);
    }

    public BasicTreeNode getParent() {
        return this.mParent;
    }

    public boolean hasChild() {
        return this.mChildren.size() != 0;
    }

    public int getChildCount() {
        return this.mChildren.size();
    }

    public void clearAllChildren() {
        for (BasicTreeNode child : this.mChildren) {
            child.clearAllChildren();
        }
        this.mChildren.clear();
    }

    public boolean findLeafMostNodesAtPoint(int px, int py, IFindNodeListener listener) {
        boolean foundInChild = false;
        for (BasicTreeNode node : this.mChildren) {
            foundInChild |= node.findLeafMostNodesAtPoint(px, py, listener);
        }
        if (foundInChild) {
            return true;
        }
        if (this.mHasBounds) {
            if ((this.x <= px) && (px <= this.x + this.width) && (this.y <= py) && (py <= this.y + this.height)) {
                listener.onFoundNode(this);
                return true;
            }
            return false;
        }
        return false;
    }

    public Object[] getAttributesArray() {
        return null;
    }

    public static abstract interface IFindNodeListener {
        public abstract void onFoundNode(BasicTreeNode paramBasicTreeNode);
    }
}
