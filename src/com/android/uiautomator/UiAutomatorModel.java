package com.android.uiautomator;

import com.android.uiautomator.tree.AttributePair;
import com.android.uiautomator.tree.BasicTreeNode;
import com.android.uiautomator.tree.BasicTreeNode.IFindNodeListener;
import com.android.uiautomator.tree.UiHierarchyXmlLoader;
import com.android.uiautomator.tree.UiNode;

import java.io.File;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.graphics.Rectangle;

public class UiAutomatorModel {
    private BasicTreeNode mRootNode;
    private BasicTreeNode mSelectedNode;
    private Rectangle mCurrentDrawingRect;
    private List<Rectangle> mNafNodes;
    private boolean mExploreMode = true;
    private boolean mShowNafNodes = false;
    private List<BasicTreeNode> mNodelist;
    private Set<String> mSearchKeySet = new HashSet();

    public UiAutomatorModel(File xmlDumpFile) {
        this.mSearchKeySet.add("text");
        this.mSearchKeySet.add("content-desc");

        UiHierarchyXmlLoader loader = new UiHierarchyXmlLoader();
        BasicTreeNode rootNode = loader.parseXml(xmlDumpFile.getAbsolutePath());
        if (rootNode == null) {
            System.err.println("null rootnode after parsing.");
            throw new IllegalArgumentException("Invalid ui automator hierarchy file.");
        }
        this.mNafNodes = loader.getNafNodes();
        if (this.mRootNode != null) {
            this.mRootNode.clearAllChildren();
        }
        this.mRootNode = rootNode;
        this.mExploreMode = true;
        this.mNodelist = loader.getAllNodes();
    }

    public BasicTreeNode getXmlRootNode() {
        return this.mRootNode;
    }

    public BasicTreeNode getSelectedNode() {
        return this.mSelectedNode;
    }

    public void setSelectedNode(BasicTreeNode node) {
        this.mSelectedNode = node;
        if ((this.mSelectedNode instanceof UiNode)) {
            UiNode uiNode = (UiNode) this.mSelectedNode;
            this.mCurrentDrawingRect = new Rectangle(uiNode.x, uiNode.y, uiNode.width, uiNode.height);
        } else {
            this.mCurrentDrawingRect = null;
        }
    }

    public Rectangle getCurrentDrawingRect() {
        return this.mCurrentDrawingRect;
    }

    public BasicTreeNode updateSelectionForCoordinates(int x, int y) {
        BasicTreeNode node = null;
        if (this.mRootNode != null) {
            MinAreaFindNodeListener listener = new MinAreaFindNodeListener();
            boolean found = this.mRootNode.findLeafMostNodesAtPoint(x, y, listener);
            if ((found) && (listener.mNode != null) && (!listener.mNode.equals(this.mSelectedNode))) {
                node = listener.mNode;
            }
        }
        return node;
    }

    public boolean isExploreMode() {
        return this.mExploreMode;
    }

    public void toggleExploreMode() {
        this.mExploreMode = (!this.mExploreMode);
    }

    public void setExploreMode(boolean exploreMode) {
        this.mExploreMode = exploreMode;
    }

    private static class MinAreaFindNodeListener
            implements BasicTreeNode.IFindNodeListener {
        BasicTreeNode mNode = null;

        public void onFoundNode(BasicTreeNode node) {
            if (this.mNode == null) {
                this.mNode = node;
            } else if (node.height * node.width < this.mNode.height * this.mNode.width) {
                this.mNode = node;
            }
        }
    }

    public List<Rectangle> getNafNodes() {
        return this.mNafNodes;
    }

    public void toggleShowNaf() {
        this.mShowNafNodes = (!this.mShowNafNodes);
    }

    public boolean shouldShowNafNodes() {
        return this.mShowNafNodes;
    }

    public List<BasicTreeNode> searchNode(String tofind) {
        List<BasicTreeNode> result = new LinkedList();
        for (BasicTreeNode node : this.mNodelist) {
            Object[] attrs = node.getAttributesArray();
            for (Object attr : attrs) {
                if (this.mSearchKeySet.contains(((AttributePair) attr).key)) {
                    if (((AttributePair) attr).value.toLowerCase().contains(tofind.toLowerCase())) {
                        result.add(node);
                        break;
                    }
                }
            }
        }
        return result;
    }
}
