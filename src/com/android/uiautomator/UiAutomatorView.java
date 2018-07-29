package com.android.uiautomator;

import com.android.uiautomator.actions.ExpandAllAction;
import com.android.uiautomator.actions.ImageHelper;
import com.android.uiautomator.actions.ToggleNafAction;
import com.android.uiautomator.tree.AttributePair;
import com.android.uiautomator.tree.BasicTreeNode;
import com.android.uiautomator.tree.BasicTreeNodeContentProvider;
import com.android.uiautomator.tree.UiNode;

import java.io.File;
import java.util.List;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;

public class UiAutomatorView
        extends Composite {
    private static final int IMG_BORDER = 2;
    private Composite mScreenshotComposite;
    private StackLayout mStackLayout;
    private Composite mSetScreenshotComposite;
    private Canvas mScreenshotCanvas;
    private TreeViewer mTreeViewer;
    private TableViewer mTableViewer;
    private float mScale = 1.0F;
    private int mDx;
    private int mDy;
    private UiAutomatorModel mModel;
    private File mModelFile;
    private Image mScreenshot;
    private List<BasicTreeNode> mSearchResult;
    private int mSearchResultIndex;
    private ToolItem itemDeleteAndInfo;
    private Text searchTextarea;
    private Cursor mOrginialCursor;
    private ToolItem itemPrev;
    private ToolItem itemNext;
    private ToolItem coordinateLabel;
    private String mLastSearchedTerm;
    private Cursor mCrossCursor;

    public UiAutomatorView(Composite parent, int style) {
        super(parent, 0);
        setLayout(new FillLayout());

        SashForm baseSash = new SashForm(this, 256);
        this.mOrginialCursor = getShell().getCursor();
        this.mCrossCursor = new Cursor(getDisplay(), 2);
        this.mScreenshotComposite = new Composite(baseSash, 2048);
        this.mStackLayout = new StackLayout();
        this.mScreenshotComposite.setLayout(this.mStackLayout);

        this.mScreenshotCanvas = new Canvas(this.mScreenshotComposite, 2048);
        this.mStackLayout.topControl = this.mScreenshotCanvas;
        this.mScreenshotComposite.layout();

        this.mScreenshotCanvas.addListener(6, new Listener() {
            public void handleEvent(Event arg0) {
                UiAutomatorView.this.getShell().setCursor(UiAutomatorView.this.mCrossCursor);
            }
        });
        this.mScreenshotCanvas.addListener(7, new Listener() {
            public void handleEvent(Event arg0) {
                UiAutomatorView.this.getShell().setCursor(UiAutomatorView.this.mOrginialCursor);
            }
        });
        this.mScreenshotCanvas.addMouseListener(new MouseAdapter() {
            public void mouseUp(MouseEvent e) {
                if (UiAutomatorView.this.mModel != null) {
                    UiAutomatorView.this.mModel.toggleExploreMode();
                    UiAutomatorView.this.redrawScreenshot();
                }
            }
        });
        this.mScreenshotCanvas.setBackground(
                getShell().getDisplay().getSystemColor(22));
        this.mScreenshotCanvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                if (UiAutomatorView.this.mScreenshot != null) {
                    UiAutomatorView.this.updateScreenshotTransformation();

                    Transform t = new Transform(e.gc.getDevice());
                    t.translate(UiAutomatorView.this.mDx, UiAutomatorView.this.mDy);
                    t.scale(UiAutomatorView.this.mScale, UiAutomatorView.this.mScale);
                    e.gc.setTransform(t);
                    e.gc.drawImage(UiAutomatorView.this.mScreenshot, 0, 0);

                    e.gc.setTransform(null);
                    if (UiAutomatorView.this.mModel.shouldShowNafNodes()) {
                        e.gc.setForeground(e.gc.getDevice().getSystemColor(7));
                        e.gc.setBackground(e.gc.getDevice().getSystemColor(7));
                        for (Rectangle r : UiAutomatorView.this.mModel.getNafNodes()) {
                            e.gc.setAlpha(50);
                            e.gc.fillRectangle(UiAutomatorView.this.mDx + UiAutomatorView.this.getScaledSize(r.x), UiAutomatorView.this.mDy + UiAutomatorView.this.getScaledSize(r.y), UiAutomatorView.this
                                    .getScaledSize(r.width), UiAutomatorView.this.getScaledSize(r.height));
                            e.gc.setAlpha(255);
                            e.gc.setLineStyle(1);
                            e.gc.setLineWidth(2);
                            e.gc.drawRectangle(UiAutomatorView.this.mDx + UiAutomatorView.this.getScaledSize(r.x), UiAutomatorView.this.mDy + UiAutomatorView.this.getScaledSize(r.y), UiAutomatorView.this
                                    .getScaledSize(r.width), UiAutomatorView.this.getScaledSize(r.height));
                        }
                    }
                    if (UiAutomatorView.this.mSearchResult != null) {
                        for (BasicTreeNode result : UiAutomatorView.this.mSearchResult) {
                            if ((result instanceof UiNode)) {
                                UiNode uiNode = (UiNode) result;
                                Rectangle rect = new Rectangle(uiNode.x, uiNode.y, uiNode.width, uiNode.height);

                                e.gc.setForeground(e.gc
                                        .getDevice().getSystemColor(7));
                                e.gc.setLineStyle(2);
                                e.gc.setLineWidth(1);
                                e.gc.drawRectangle(UiAutomatorView.this.mDx + UiAutomatorView.this.getScaledSize(rect.x),
                                        UiAutomatorView.this.mDy + UiAutomatorView.this.getScaledSize(rect.y), UiAutomatorView.this
                                                .getScaledSize(rect.width), UiAutomatorView.this.getScaledSize(rect.height));
                            }
                        }
                    }
                    Rectangle rect = UiAutomatorView.this.mModel.getCurrentDrawingRect();
                    if (rect != null) {
                        e.gc.setForeground(e.gc.getDevice().getSystemColor(3));
                        if (UiAutomatorView.this.mModel.isExploreMode()) {
                            e.gc.setLineStyle(2);
                            e.gc.setLineWidth(1);
                        } else {
                            e.gc.setLineStyle(1);
                            e.gc.setLineWidth(2);
                        }
                        e.gc.drawRectangle(UiAutomatorView.this.mDx + UiAutomatorView.this.getScaledSize(rect.x), UiAutomatorView.this.mDy + UiAutomatorView.this.getScaledSize(rect.y), UiAutomatorView.this
                                .getScaledSize(rect.width), UiAutomatorView.this.getScaledSize(rect.height));
                    }
                }
            }
        });
        this.mScreenshotCanvas.addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e) {
                if (UiAutomatorView.this.mModel != null) {
                    int x = UiAutomatorView.this.getInverseScaledSize(e.x - UiAutomatorView.this.mDx);
                    int y = UiAutomatorView.this.getInverseScaledSize(e.y - UiAutomatorView.this.mDy);

                    UiAutomatorView.this.coordinateLabel.setText(String.format("(%d,%d)", new Object[]{Integer.valueOf(x), Integer.valueOf(y)}));
                    if (UiAutomatorView.this.mModel.isExploreMode()) {
                        BasicTreeNode node = UiAutomatorView.this.mModel.updateSelectionForCoordinates(x, y);
                        if (node != null) {
                            UiAutomatorView.this.updateTreeSelection(node);
                        }
                    }
                }
            }
        });
        this.mSetScreenshotComposite = new Composite(this.mScreenshotComposite, 0);
        this.mSetScreenshotComposite.setLayout(new GridLayout());

        final Button setScreenshotButton = new Button(this.mSetScreenshotComposite, 8);
        setScreenshotButton.setText("Specify Screenshot...");
        setScreenshotButton.addSelectionListener(new SelectionAdapter() {
            ImageData[] data;
            public void widgetSelected(SelectionEvent arg0) {
                FileDialog fd = new FileDialog(setScreenshotButton.getShell());
                fd.setFilterExtensions(new String[]{"*.png"});
                if (UiAutomatorView.this.mModelFile != null) {
                    fd.setFilterPath(UiAutomatorView.this.mModelFile.getParent());
                }
                String screenshotPath = fd.open();
                if (screenshotPath == null) {
                    return;
                }
                try {
                    data = new ImageLoader().load(screenshotPath);
                } catch (Exception e) {
//                    ImageData[] data;
                    return;
                }
//                ImageData[] data;
                if (data.length < 1) {
                    return;
                }
                UiAutomatorView.this.mScreenshot = new Image(Display.getDefault(), data[0]);
                UiAutomatorView.this.redrawScreenshot();
            }
        });
        SashForm rightSash = new SashForm(baseSash, 512);

        Composite upperRightBase = new Composite(rightSash, 2048);
        upperRightBase.setLayout(new GridLayout(1, false));

        ToolBarManager toolBarManager = new ToolBarManager(8388608);
        toolBarManager.add(new ExpandAllAction(this));
        toolBarManager.add(new ToggleNafAction(this));
        ToolBar searchtoolbar = toolBarManager.createControl(upperRightBase);

        ToolItem itemSeparator = new ToolItem(searchtoolbar, 131074);
        this.searchTextarea = new Text(searchtoolbar, 2180);
        this.searchTextarea.pack();
        itemSeparator.setWidth(this.searchTextarea.getBounds().width);
        itemSeparator.setControl(this.searchTextarea);
        this.itemPrev = new ToolItem(searchtoolbar, 64);
        this.itemPrev.setImage(ImageHelper.loadImageDescriptorFromResource("images/prev.png")
                .createImage());
        this.itemNext = new ToolItem(searchtoolbar, 64);
        this.itemNext.setImage(ImageHelper.loadImageDescriptorFromResource("images/next.png")
                .createImage());
        this.itemDeleteAndInfo = new ToolItem(searchtoolbar, 64);
        this.itemDeleteAndInfo.setImage(ImageHelper.loadImageDescriptorFromResource("images/delete.png")
                .createImage());
        this.itemDeleteAndInfo.setToolTipText("Clear search results");
        this.coordinateLabel = new ToolItem(searchtoolbar, 64);
        this.coordinateLabel.setText("");
        this.coordinateLabel.setEnabled(false);

        this.searchTextarea.addKeyListener(new KeyListener() {
            public void keyReleased(KeyEvent event) {
                if (event.keyCode == 13) {
                    String term = UiAutomatorView.this.searchTextarea.getText();
                    if (!term.isEmpty()) {
                        if (term.equals(UiAutomatorView.this.mLastSearchedTerm)) {
                            UiAutomatorView.this.nextSearchResult();
                            return;
                        }
                        UiAutomatorView.this.clearSearchResult();
                        UiAutomatorView.this.mSearchResult = UiAutomatorView.this.mModel.searchNode(term);
                        if (!UiAutomatorView.this.mSearchResult.isEmpty()) {
                            UiAutomatorView.this.mSearchResultIndex = 0;
                            UiAutomatorView.this.updateSearchResultSelection();
                            UiAutomatorView.this.mLastSearchedTerm = term;
                        }
                    }
                }
            }

            public void keyPressed(KeyEvent event) {
            }
        });
        SelectionListener l = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent se) {
                if (se.getSource() == UiAutomatorView.this.itemPrev) {
                    UiAutomatorView.this.prevSearchResult();
                } else if (se.getSource() == UiAutomatorView.this.itemNext) {
                    UiAutomatorView.this.nextSearchResult();
                } else if (se.getSource() == UiAutomatorView.this.itemDeleteAndInfo) {
                    UiAutomatorView.this.searchTextarea.setText("");
                    UiAutomatorView.this.clearSearchResult();
                }
            }
        };
        this.itemPrev.addSelectionListener(l);
        this.itemNext.addSelectionListener(l);
        this.itemDeleteAndInfo.addSelectionListener(l);

        searchtoolbar.pack();
        searchtoolbar.setLayoutData(new GridData(768));

        this.mTreeViewer = new TreeViewer(upperRightBase, 0);
        this.mTreeViewer.setContentProvider(new BasicTreeNodeContentProvider());

        this.mTreeViewer.setLabelProvider(new LabelProvider());
        this.mTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                BasicTreeNode selectedNode = null;
                if ((event.getSelection() instanceof IStructuredSelection)) {
                    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                    Object o = selection.getFirstElement();
                    if ((o instanceof BasicTreeNode)) {
                        selectedNode = (BasicTreeNode) o;
                    }
                }
                UiAutomatorView.this.mModel.setSelectedNode(selectedNode);
                UiAutomatorView.this.redrawScreenshot();
                if (selectedNode != null) {
                    UiAutomatorView.this.loadAttributeTable();
                }
            }
        });
        Tree tree = this.mTreeViewer.getTree();
        tree.setLayoutData(new GridData(4, 4, true, true, 1, 1));

        tree.setFocus();

        Composite lowerRightBase = new Composite(rightSash, 2048);
        lowerRightBase.setLayout(new FillLayout());
        Group grpNodeDetail = new Group(lowerRightBase, 0);
        grpNodeDetail.setLayout(new FillLayout(256));
        grpNodeDetail.setText("Node Detail");

        Composite tableContainer = new Composite(grpNodeDetail, 0);

        TableColumnLayout columnLayout = new TableColumnLayout();
        tableContainer.setLayout(columnLayout);

        this.mTableViewer = new TableViewer(tableContainer, 65536);
        Table table = this.mTableViewer.getTable();
        table.setLinesVisible(true);

        this.mTableViewer.setContentProvider(new ArrayContentProvider());

        TableViewerColumn tableViewerColumnKey = new TableViewerColumn(this.mTableViewer, 0);
        TableColumn tblclmnKey = tableViewerColumnKey.getColumn();
        tableViewerColumnKey.setLabelProvider(new ColumnLabelProvider() {
            public String getText(Object element) {
                if ((element instanceof AttributePair)) {
                    return ((AttributePair) element).key;
                }
                return super.getText(element);
            }
        });
        columnLayout.setColumnData(tblclmnKey, new ColumnWeightData(1, 20, true));

        TableViewerColumn tableViewerColumnValue = new TableViewerColumn(this.mTableViewer, 0);
        tableViewerColumnValue.setEditingSupport(new AttributeTableEditingSupport(this.mTableViewer));
        TableColumn tblclmnValue = tableViewerColumnValue.getColumn();
        columnLayout.setColumnData(tblclmnValue, new ColumnWeightData(2, 20, true));

        tableViewerColumnValue.setLabelProvider(new ColumnLabelProvider() {
            public String getText(Object element) {
                if ((element instanceof AttributePair)) {
                    return ((AttributePair) element).value;
                }
                return super.getText(element);
            }
        });
        baseSash.setWeights(new int[]{5, 3});
    }

    protected void prevSearchResult() {
        if (this.mSearchResult == null) {
            return;
        }
        if (this.mSearchResult.isEmpty()) {
            this.mSearchResult = null;
            return;
        }
        this.mSearchResultIndex -= 1;
        if (this.mSearchResultIndex < 0) {
            this.mSearchResultIndex += this.mSearchResult.size();
        }
        updateSearchResultSelection();
    }

    protected void clearSearchResult() {
        this.itemDeleteAndInfo.setText("");
        this.mSearchResult = null;
        this.mSearchResultIndex = 0;
        this.mLastSearchedTerm = "";
        this.mScreenshotCanvas.redraw();
    }

    protected void nextSearchResult() {
        if (this.mSearchResult == null) {
            return;
        }
        if (this.mSearchResult.isEmpty()) {
            this.mSearchResult = null;
            return;
        }
        this.mSearchResultIndex = ((this.mSearchResultIndex + 1) % this.mSearchResult.size());
        updateSearchResultSelection();
    }

    private void updateSearchResultSelection() {
        updateTreeSelection((BasicTreeNode) this.mSearchResult.get(this.mSearchResultIndex));
        this.itemDeleteAndInfo.setText("" + (this.mSearchResultIndex + 1) + "/" + this.mSearchResult
                .size());
    }

    private int getScaledSize(int size) {
        if (this.mScale == 1.0F) {
            return size;
        }
        return new Double(Math.floor(size * this.mScale)).intValue();
    }

    private int getInverseScaledSize(int size) {
        if (this.mScale == 1.0F) {
            return size;
        }
        return new Double(Math.floor(size / this.mScale)).intValue();
    }

    private void updateScreenshotTransformation() {
        Rectangle canvas = this.mScreenshotCanvas.getBounds();
        Rectangle image = this.mScreenshot.getBounds();
        float scaleX = (canvas.width - 4 - 1) / image.width;
        float scaleY = (canvas.height - 4 - 1) / image.height;

        this.mScale = Math.min(scaleX, scaleY);

        this.mDx = ((canvas.width - getScaledSize(image.width) - 4) / 2 + 2);
        this.mDy = ((canvas.height - getScaledSize(image.height) - 4) / 2 + 2);
    }

    private class AttributeTableEditingSupport
            extends EditingSupport {
        private TableViewer mViewer;

        public AttributeTableEditingSupport(TableViewer viewer) {
            super(viewer);
            this.mViewer = viewer;
        }

        protected boolean canEdit(Object arg0) {
            return true;
        }

        protected CellEditor getCellEditor(Object arg0) {
            return new TextCellEditor(this.mViewer.getTable());
        }

        protected Object getValue(Object o) {
            return ((AttributePair) o).value;
        }

        protected void setValue(Object arg0, Object arg1) {
        }
    }

    public void redrawScreenshot() {
        if (this.mScreenshot == null) {
            this.mStackLayout.topControl = this.mSetScreenshotComposite;
        } else {
            this.mStackLayout.topControl = this.mScreenshotCanvas;
        }
        this.mScreenshotComposite.layout();

        this.mScreenshotCanvas.redraw();
    }

    public void setInputHierarchy(Object input) {
        this.mTreeViewer.setInput(input);
    }

    public void loadAttributeTable() {
        this.mTableViewer.setInput(this.mModel.getSelectedNode().getAttributesArray());
    }

    public void expandAll() {
        this.mTreeViewer.expandAll();
    }

    public void updateTreeSelection(BasicTreeNode node) {
        this.mTreeViewer.setSelection(new StructuredSelection(node), true);
    }

    public void setModel(UiAutomatorModel model, File modelBackingFile, Image screenshot) {
        this.mModel = model;
        this.mModelFile = modelBackingFile;
        if (this.mScreenshot != null) {
            this.mScreenshot.dispose();
        }
        this.mScreenshot = screenshot;
        clearSearchResult();
        redrawScreenshot();

        BasicTreeNode wrapper = new BasicTreeNode();

        wrapper.addChild(this.mModel.getXmlRootNode());
        setInputHierarchy(wrapper);
        this.mTreeViewer.getTree().setFocus();
    }

    public boolean shouldShowNafNodes() {
        return this.mModel != null ? this.mModel.shouldShowNafNodes() : false;
    }

    public void toggleShowNaf() {
        if (this.mModel != null) {
            this.mModel.toggleShowNaf();
        }
    }

    public Image getScreenShot() {
        return this.mScreenshot;
    }

    public File getModelFile() {
        return this.mModelFile;
    }
}
