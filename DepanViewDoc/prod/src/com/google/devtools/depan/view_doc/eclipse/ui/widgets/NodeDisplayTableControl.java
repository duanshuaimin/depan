/*
 * Copyright 2014 The Depan Project Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.depan.view_doc.eclipse.ui.widgets;

import com.google.devtools.depan.eclipse.ui.nodes.trees.NodeWrapper;
import com.google.devtools.depan.eclipse.ui.nodes.viewers.GraphNodeViewer;
import com.google.devtools.depan.eclipse.ui.nodes.viewers.NodeViewerProvider;
import com.google.devtools.depan.model.GraphNode;
import com.google.devtools.depan.platform.AlphabeticSorter;
import com.google.devtools.depan.platform.Colors;
import com.google.devtools.depan.platform.InverseSorter;
import com.google.devtools.depan.platform.LabelProviderToString;
import com.google.devtools.depan.platform.PlatformResources;
import com.google.devtools.depan.platform.eclipse.ui.tables.EditColTableDef;
import com.google.devtools.depan.platform.eclipse.ui.widgets.Widgets;
import com.google.devtools.depan.view_doc.eclipse.ViewDocLogger;
import com.google.devtools.depan.view_doc.model.EdgeDisplayProperty;
import com.google.devtools.depan.view_doc.model.NodeDisplayProperty;
import com.google.devtools.depan.view_doc.model.NodeDisplayRepository;
import com.google.devtools.depan.view_doc.model.NodeLocationRepository;
import com.google.devtools.depan.view_doc.model.Point2dUtils;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.model.IWorkbenchAdapter;

import java.awt.Color;
import java.awt.geom.Point2D;

/**
 * Show a table of the nodes, with their attributes.  The attributes
 * include node visibility and {@link NodeDisplayProperty}s.
 * 
 * @author <a href="leeca@pnambic.com">Lee Carver</a>
 */
public class NodeDisplayTableControl extends Composite {

  public static final String COL_NAME = "Node";
  protected static final String COL_XPOS = "X";
  protected static final String COL_YPOS = "Y";
  protected static final String COL_VISIBLE = "Visible";
  protected static final String COL_SELECTED = "Selected";
  protected static final String COL_SIZE = "Size";
  public static final String COL_COLOR = "Color";

  public static final int INDEX_NAME = 0;
  public static final int INDEX_XPOS = 1;
  public static final int INDEX_YPOS = 2;
  public static final int INDEX_VISIBLE = 3;
  public static final int INDEX_SIZE = 4;
  public static final int INDEX_COLOR = 5;

  private static final EditColTableDef[] TABLE_DEF = new EditColTableDef[] {
    new EditColTableDef(COL_NAME, false, COL_NAME, 600),
    new EditColTableDef(COL_XPOS, true, COL_XPOS, 100),
    new EditColTableDef(COL_YPOS, true, COL_YPOS, 100),
    new EditColTableDef(COL_VISIBLE, true, COL_VISIBLE, 100),
    new EditColTableDef(COL_SIZE, true, COL_SIZE, 100),
    new EditColTableDef(COL_COLOR, true, COL_COLOR, 180)
  };

  private class ControlGraphNodeViewer extends GraphNodeViewer {

    /**
     * @param parent
     */
    public ControlGraphNodeViewer(Composite parent) {
      super(parent);
    }

    @Override
    protected TreeViewer createTreeViewer(Composite parent) {
      TreeViewer result = super.createTreeViewer(parent);

      // Initialize the table.
      Tree tree = result.getTree();
      tree.setHeaderVisible(true);
      tree.setToolTipText("Node Display Properties");
      EditColTableDef.setupTree(TABLE_DEF, tree);

      // Configure cell editing.
      CellEditor[] cellEditors = new CellEditor[TABLE_DEF.length];
      cellEditors[INDEX_NAME] = null;
      cellEditors[INDEX_XPOS] = new TextCellEditor(tree);
      cellEditors[INDEX_YPOS] = new TextCellEditor(tree);
      cellEditors[INDEX_VISIBLE] = new CheckboxCellEditor(tree);
      cellEditors[INDEX_SIZE] = new ComboBoxCellEditor(tree,
          NodeDisplayTableControl.toString(
              NodeDisplayProperty.Size.values(), true));
      cellEditors[INDEX_COLOR] = new ColorCellEditor(tree);

      result.setCellEditors(cellEditors);
      result.setLabelProvider(new PartLabelProvider());
      result.setColumnProperties(EditColTableDef.getProperties(TABLE_DEF));
      result.setCellModifier(new PartCellModifier());

      return result;
    }

    public Tree getTree() {
      return (Tree) getTreeViewer().getControl();
    }

    public ITableLabelProvider getLabelProvider() {
      return (ITableLabelProvider) getTreeViewer().getLabelProvider();
    }

    public void setSorter(ViewerSorter sorter) {
      getTreeViewer().setSorter(sorter);
    }

    public void updateNodeColumns(GraphNode node, String[] cols) {
      getTreeViewer().update(node, cols);
    }
  }

  /////////////////////////////////////
  // NodeDisplayProperty integration

  private NodeDisplayRepository displayRepo;

  private ControlDisplayChangeListener displayListener;

  private static final String[] UPDATE_DISPLAY_COLUMNS = new String [] {
    COL_VISIBLE, COL_SIZE, COL_COLOR
  };

  private class ControlDisplayChangeListener
  implements NodeDisplayRepository.ChangeListener {

    @Override
    public void nodeDisplayChanged(GraphNode node, NodeDisplayProperty props) {
      updateNodeColumns(node, UPDATE_DISPLAY_COLUMNS);
    }
  }

  /////////////////////////////////////
  // Location integration

  private NodeLocationRepository posRepo;

  private ControlLocationChangeListener posListener;

  private static final String[] UPDATE_LOCATION_COLUMNS = new String [] {
    COL_XPOS, COL_YPOS
  };

  private class ControlLocationChangeListener
      implements NodeLocationRepository.ChangeListener {

    @Override
    public void nodeLocationChanged(GraphNode node, Point2D location) {
      updateNodeColumns(node, UPDATE_LOCATION_COLUMNS);
    }
  }

  /////////////////////////////////////
  // UX Elements

  private final ControlGraphNodeViewer propViewer;

  /////////////////////////////////////
  // Public methods

  public NodeDisplayTableControl(Composite parent) {
    super(parent, SWT.NONE);
    setLayout(Widgets.buildContainerLayout(1));

    propViewer = new ControlGraphNodeViewer(this);
    propViewer.setLayoutData(Widgets.buildGrabFillData());
    configSorters(propViewer.getTree());
  }

  private static String[] toString(Object[] objs, boolean lowercase) {
    String[] s = new String[objs.length];
    int i = 0;
    for (Object o : objs) {
      s[i++] = lowercase ? o.toString().toLowerCase() : o.toString();
    }
    return s;
  }

  public void setInput(NodeViewerProvider provider) {
    propViewer.setNvProvider(provider);
    propViewer.refresh();
}

  public void setNodeRepository(
      NodeLocationRepository posRepo,
      NodeDisplayRepository displayRepo) {
    this.posRepo = posRepo;
    posListener = new ControlLocationChangeListener();
    posRepo.addChangeListener(posListener);

    this.displayRepo = displayRepo;
    displayListener = new ControlDisplayChangeListener();
    displayRepo.addChangeListener(displayListener);
  }

  public void removeNodeRepository(
      NodeLocationRepository posRepo,
      NodeDisplayRepository displayRepo) {
    if (null != displayListener) {
      this.displayRepo.removeChangeListener(displayListener);
      displayListener = null;
    }
    if (null != posListener) {
      this.posRepo.removeChangeListener(posListener);
      posListener = null;
    }
  }

  private void updateNodeColumns(GraphNode node, String[] cols) {
    propViewer.updateNodeColumns(node, cols);
  }

  /////////////////////////////////////
  // Display repository methods

  /**
   * Acquire properties directly, avoid setting up a default.
   */
  private void saveDisplayProperty(
      GraphNode node, NodeDisplayProperty props) {
    displayRepo.setDisplayProperty(node, props);
  }

  /**
   * Acquire properties directly, avoid setting up a default.
   */
  private NodeDisplayProperty loadDisplayProperty(GraphNode node) {
    return displayRepo.getDisplayProperty(node);
  }

  private boolean isVisible(GraphNode node) {
    NodeDisplayProperty prop = getDisplayProperty(node);
    return prop.isVisible();
  }

  /**
   * Utility method for both the label provider and cell modifier.
   * Note that the default constructor for {@link EdgeDisplayProperty}
   * uses the default values for all member elements.
   */
  private NodeDisplayProperty getDisplayProperty(GraphNode node) {
    return loadDisplayProperty(node);
  }

  private String getColorName(GraphNode node) {
    NodeDisplayProperty prop = getDisplayProperty(node);
    if (null == prop) {
      return null;
    }
    Color color = prop.getColor();
    if (null == color) {
      return null;
    }
    String result = StringConverter.asString(Colors.rgbFromColor(color));
    return "(" + result + ")";
  }

  private String getSizeName(GraphNode node) {
    NodeDisplayProperty prop = getDisplayProperty(node);
    return prop.getSize().toString().toLowerCase();
  }

  /////////////////////////////////////
  // Location repository methods

  private void updateLocationX(GraphNode node, Object update) {
    try {
      double newX = Double.parseDouble((String) update);
      Point2D pos = posRepo.getLocation(node);
      Point2D location = Point2dUtils.newPoint2D(newX, pos.getY());
      posRepo.setLocation(node, location);
    } catch (NumberFormatException errNum) {
      ViewDocLogger.logException("Bad number format for X position", errNum);
    } catch (RuntimeException err) {
      ViewDocLogger.logException("Bad update value for X position", err);
    }
  }

  private void updateLocationY(GraphNode node, Object update) {
    try {
      double newY = Double.parseDouble((String) update);
      Point2D pos = posRepo.getLocation(node);
      Point2D location = Point2dUtils.newPoint2D(pos.getX(), newY);
      posRepo.setLocation(node, location);
    } catch (NumberFormatException errNum) {
      ViewDocLogger.logException("Bad number format for Y position", errNum);
    } catch (RuntimeException err) {
      ViewDocLogger.logException("Bad update value for Y position", err);
    }
  }

  /** Not every node has a position */
  public String getXPos(GraphNode node) {
    Point2D position = posRepo.getLocation(node);
    if (null != position) {
      return fmtDouble(position.getX());
    }
    return "";
  }

  /** Not every node has a position */
  public String getYPos(GraphNode node) {
    Point2D position = posRepo.getLocation(node);
    if (null != position) {
      return fmtDouble(position.getY());
    }
    return "";
  }

  private String fmtDouble(double pos) {
    // TODO: 3 significant digits
    return Double.toString(pos);
  }

  /////////////////////////////////////
  // Column sorting

  private void configSorters(Tree tree) {
    int index = 0;
    for (TreeColumn column : tree.getColumns()) {
      final int colIndex = index++;

      column.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent event) {
          updateSortColumn((TreeColumn) event.widget, colIndex);
        }
      });
    }
  }

  private void updateSortColumn(TreeColumn column, int colIndex) {
    setSortColumn(column, colIndex, getSortDirection(column));
  }

  private int getSortDirection(TreeColumn column) {
    Tree tree = propViewer.getTree();
    if (column != tree.getSortColumn()) {
      return SWT.DOWN;
    }
    // If it is unsorted (SWT.NONE), assume down sort
    return (SWT.DOWN == tree.getSortDirection())
        ? SWT.UP : SWT.DOWN;
  }

  private void setSortColumn(
      TreeColumn column, int colIndex, int direction) {

    ViewerSorter sorter = buildColumnSorter(colIndex);
    if (SWT.UP == direction) {
      sorter = new InverseSorter(sorter);
    }

    Tree tree = propViewer.getTree();
    tree.setSortColumn(column);
    tree.setSortDirection(direction);

    propViewer.setSorter(sorter);
  }

  private ViewerSorter buildColumnSorter(int colIndex) {
    if (INDEX_VISIBLE == colIndex) {
      return new BooleanViewSorter();
    }
    if (INDEX_XPOS == colIndex) {
      return new PositionSorter(true);
    }
    if (INDEX_YPOS == colIndex) {
      return new PositionSorter(false);
    }

    // By default, use an alphabetic sort over the column labels.
    ITableLabelProvider labelProvider =
        (ITableLabelProvider) propViewer.getLabelProvider();
    ViewerSorter result = new AlphabeticSorter(
        new LabelProviderToString(labelProvider, colIndex));
    return result;
  }

  @SuppressWarnings("unchecked")
  private GraphNode getGraphNode(Object element) {
    if (element instanceof NodeWrapper<?>) {
      NodeWrapper<GraphNode> wrap = (NodeWrapper<GraphNode>) element;
      return wrap.getNode();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private IWorkbenchAdapter getWorkbenchAdapter(Object element) {
    if (element instanceof IAdaptable ) {
      Object result = ((IAdaptable) element).getAdapter(IWorkbenchAdapter.class);
      return (IWorkbenchAdapter) result;
    }
    return null;
  }

  private class PositionSorter extends ViewerSorter {

    private final boolean useX;

    private PositionSorter(boolean useX) {
      this.useX = useX;
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
      double pos1 = getPosition(e1);
      double pos2 = getPosition(e2);
      return Double.compare(pos1, pos2);
    }

    private double getPosition(Object item) {
      GraphNode node = getGraphNode(item);
      if (null != node) {
        Point2D position = posRepo.getLocation(node);
        return useX ? position.getX() : position.getY();
      }

      return 0.0;
    }
  }

  private class BooleanViewSorter extends ViewerSorter {

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
      boolean vis1 = isVisible(e1);
      boolean vis2 = isVisible(e2);
      return Boolean.compare(vis1, vis2);
    }

    private boolean isVisible(Object item) {
      GraphNode node = getGraphNode(item);
      if (null != node) {
        return NodeDisplayTableControl.this.isVisible(node);
      }
      return false;
    }
  }

  /////////////////////////////////////
  // Label provider for table cell text

  private class PartLabelProvider extends LabelProvider
      implements ITableLabelProvider {

    @Override
    public String getColumnText(Object element, int columnIndex) {
      GraphNode node = getGraphNode(element);
      if (null != node) {
        switch (columnIndex) {
        case INDEX_NAME:
          return node.toString();
        case INDEX_XPOS:
          return getXPos(node);
        case INDEX_YPOS:
          return getYPos(node);
        case INDEX_COLOR:
          return getColorName(node);
        case INDEX_SIZE:
          return getSizeName(node);
        }
      }
      IWorkbenchAdapter item = getWorkbenchAdapter(element);
      if (null != item) {
        switch (columnIndex) {
        case INDEX_NAME:
          return item.getLabel(element);
        }
      }
      return null;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      GraphNode node = getGraphNode(element);
      if (null != node) {
        switch (columnIndex) {
        case INDEX_VISIBLE:
          return PlatformResources.getOnOff(isVisible(node));
        }
      }

      IWorkbenchAdapter item = getWorkbenchAdapter(element);
      if (null != item) {
        switch (columnIndex) {
        case INDEX_NAME:
          return null; // TODO: versus - item.getImageDescriptor(element)...
        }
      }
      // Fall through and unknown type
      return null;
    }
  }

  /////////////////////////////////////
  // Value provider/modifier for edit cells

  private class PartCellModifier implements ICellModifier{

    @Override
    public boolean canModify(Object element, String property) {
      return EditColTableDef.get(TABLE_DEF, property).isEditable();
    }

    @Override
    public Object getValue(Object element, String property) {
      GraphNode node = getGraphNode(element);
      if (null == node) {
        return null;
      }

      if (COL_XPOS.equals(property)) {
        return getXPos(node);
      }
      if (COL_YPOS.equals(property)) {
        return getXPos(node);
      }
      NodeDisplayProperty nodeProp = getDisplayProperty(node);
      if (COL_COLOR.equals(property)) {
        Color relColor = nodeProp.getColor();
        if (null == relColor) {
          return new RGB(0, 0, 0);
        }
        RGB result = Colors.rgbFromColor(relColor);
        return result;
      }
      if (COL_VISIBLE.equals(property)) {
        return Boolean.valueOf(nodeProp.isVisible());
      }
      if (COL_SIZE.equals(property)) {
        return nodeProp.getSize().ordinal();
      }
      return null;
    }

    @Override
    public void modify(Object element, String property, Object value) {
      if (!(element instanceof TreeItem)) {
        return;
      }
      Object modifiedObject = ((TreeItem) element).getData();

      GraphNode node = getGraphNode(modifiedObject);
      if (null == node) {
        return;
      }
      if (COL_XPOS.equals(property)) {
        updateLocationX(node, value);
        return;
      }
      if (COL_YPOS.equals(property)) {
        updateLocationY(node, value);
        return;
      }

      NodeDisplayProperty nodeProp = loadDisplayProperty(node);
      if (null == nodeProp) {
        return; // For example, when there is no editor.
      }

      if (COL_VISIBLE.equals(property) && (value instanceof Boolean)) {
        nodeProp.setVisible(((Boolean) value).booleanValue());
      } else if (COL_SIZE.equals(property) && (value instanceof Integer)) {
        nodeProp.setSize(NodeDisplayProperty.Size.values()[(Integer) value]);
      } else if (COL_COLOR.equals(property) && (value instanceof RGB)) {
        Color newColor = Colors.colorFromRgb((RGB) value);
        nodeProp.setColor(newColor);
      }

      saveDisplayProperty(node, nodeProp);
      // Viewer updates via ChangeListener
    }
  }
}