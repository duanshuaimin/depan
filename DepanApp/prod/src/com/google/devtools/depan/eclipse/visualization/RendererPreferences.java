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
package com.google.devtools.depan.eclipse.visualization;

import com.google.devtools.depan.eclipse.preferences.ColorPreferencesIds;
import com.google.devtools.depan.eclipse.preferences.LabelPreferencesIds;
import com.google.devtools.depan.eclipse.preferences.LabelPreferencesIds.LabelPosition;
import com.google.devtools.depan.eclipse.preferences.NodePreferencesIds;
import com.google.devtools.depan.eclipse.preferences.NodePreferencesIds.NodeColors;
import com.google.devtools.depan.eclipse.preferences.NodePreferencesIds.NodeShape;
import com.google.devtools.depan.eclipse.preferences.NodePreferencesIds.NodeSize;
import com.google.devtools.depan.eclipse.preferences.PreferencesIds;
import com.google.devtools.depan.eclipse.utils.Tools;
import com.google.devtools.depan.eclipse.visualization.ogl.GLPanel;
import com.google.devtools.depan.eclipse.visualization.ogl.RenderingPipe;
import com.google.devtools.depan.eclipse.visualization.plugins.impl.NodeColorPlugin;
import com.google.devtools.depan.eclipse.visualization.plugins.impl.NodeLabelPlugin;
import com.google.devtools.depan.eclipse.visualization.plugins.impl.NodeShapePlugin;
import com.google.devtools.depan.eclipse.visualization.plugins.impl.NodeSizePlugin;
import com.google.devtools.depan.eclipse.visualization.plugins.impl.NodeStrokePlugin;
import com.google.devtools.depan.model.GraphEdge;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

import java.awt.Color;

/**
 * Handle the coordination of Eclipse managed user preferences with
 * the OGL renderer.
 */
public class RendererPreferences implements IPreferenceChangeListener {

  private GLPanel glPanel;
  
  public RendererPreferences(GLPanel glPanel) {
    this.glPanel = glPanel;
  }

  /////////////////////////////////////
  // Support methods

  private RenderingPipe getRenderingPipe() {
    return glPanel.getRenderingPipe();
  }

  private NodeLabelPlugin getNodeLabel() {
    return getRenderingPipe().getNodeLabel();
  }
  
  private NodeColorPlugin<GraphEdge> getNodeColor() {
    return getRenderingPipe().getNodeColors();
  }

  private NodeShapePlugin<GraphEdge> getNodeShape() {
    return getRenderingPipe().getNodeShape();
  }

  private NodeStrokePlugin<GraphEdge> getNodeStroke() {
    return getRenderingPipe().getNodeStroke();
  }

  private NodeSizePlugin<GraphEdge> getNodeSize() {
    return getRenderingPipe().getNodeSize();
  }

  /////////////////////////////////////
  // Write preferences to renderer

  /**
   * Read and setup label preferences.
   */
  private void setLabelPreferences() {
    IEclipsePreferences node = PreferencesIds.getInstanceNode();

    // set label position
    try {
      String val = node.get(LabelPreferencesIds.LABEL_POSITION,
          LabelPreferencesIds.LABEL_POSITION_DEFAULT);
      getNodeLabel().setLabelPosition(LabelPosition.valueOf(val));
    } catch (IllegalArgumentException ex) {
      // bad label position in the preferences. ignore the change.
      System.err.println("Bad label position in preferences");
    }
  }

  /**
   * Read and setup node rendering preferences (Colors, size, shape, ratio).
   */
  private void setNodePreferences() {
    IEclipsePreferences node = PreferencesIds.getInstanceNode();

    // read enable/disable preferences
    boolean colorEnabled = node.getBoolean(
        NodePreferencesIds.NODE_COLOR_ON, true);
    boolean shapeEnabled = node.getBoolean(
        NodePreferencesIds.NODE_SHAPE_ON, true);
    boolean resizeEnabled = node.getBoolean(
        NodePreferencesIds.NODE_SIZE_ON, false);
    boolean ratioEnabled = node.getBoolean(
        NodePreferencesIds.NODE_RATIO_ON, false);
    boolean strokeHighlight = node.getBoolean(
        NodePreferencesIds.NODE_STROKE_HIGHLIGHT_ON, true);
    boolean rootHighlight = node.getBoolean(
        NodePreferencesIds.NODE_ROOT_HIGHLIGHT_ON, false);

    NodeSizePlugin<GraphEdge> nodeSize = getNodeSize();
    NodeColorPlugin<GraphEdge> nodeColor = getNodeColor();
    NodeShapePlugin<GraphEdge> nodeShape = getNodeShape();
    NodeStrokePlugin<GraphEdge> nodeStroke = getNodeStroke();

    // set enable/disable preferences
    nodeColor.setColor(colorEnabled);
    nodeColor.setSeedColoring(rootHighlight);
    nodeShape.setShapes(shapeEnabled);
    nodeSize.setRatio(ratioEnabled);
    nodeSize.setResize(resizeEnabled);
    nodeStroke.activate(strokeHighlight);

    // set color mode color
    try {
      NodeColors color = NodeColors.valueOf(node.get(
          NodePreferencesIds.NODE_COLOR,
          NodeColors.getDefault().toString()));
      nodeColor.setColorMode(color);
    } catch (IllegalArgumentException ex) {
      // bad node rendering option. ignore.
      System.err.println("Bad node rendering option (color) in preferences.");
    }

    // set shape mode
    try {
      NodeShape shape = NodeShape.valueOf(node.get(
          NodePreferencesIds.NODE_SHAPE,
          NodeShape.getDefault().toString()));
      nodeShape.setShapeMode(shape);
    } catch (IllegalArgumentException ex) {
      // bad node rendering option. ignore.
      System.err.println("Bad node rendering option (shape) in preferences.");
    }

    // set size mode
    try {
      NodeSize size = NodeSize.valueOf(node.get(
          NodePreferencesIds.NODE_SIZE,
          NodeSize.getDefault().toString()));
      nodeSize.setSizeMode(size);
    } catch (IllegalArgumentException ex) {
      // bad node rendering option. ignore.
      System.err.println("Bad node rendering option (size) in preferences.");
    }
  }

  /**
   * read and setup color preferences.
   */
  private void setColorsPreferences() {
    IEclipsePreferences node = PreferencesIds.getInstanceNode();

    Color back = Tools.getRgb(node.get(
        ColorPreferencesIds.COLOR_BACKGROUND, "255,255,255"));
    Color front = Tools.getRgb(node.get(
        ColorPreferencesIds.COLOR_FOREGROUND, "0,0,0"));
    glPanel.setColors(back, front);
  }

  /**
   * Set all Eclipse managed preferences to the GLPanel.
   */
  public void setPreferences() {
    // setup label preferences
    setLabelPreferences();
    // setup size and shape for nodes with preferences.
    setNodePreferences();
    // setup color preferences.
    setColorsPreferences();
  }

  public void dispose() {
    // TODO(leeca): What does this do?
    // listen the changes in the configuration
    IEclipsePreferences prefs = PreferencesIds.getInstanceNode();
    prefs.removePreferenceChangeListener(this);
  }

  @Override
  public void preferenceChange(PreferenceChangeEvent event) {
    // changes in the configuration for the views, so redraw the graph.
    if (event.getKey().startsWith(LabelPreferencesIds.LABEL_PREFIX)) {
      setLabelPreferences();
    }
    if (event.getKey().startsWith(ColorPreferencesIds.COLORS_PREFIX)) {
      setColorsPreferences();
    }
    if (event.getKey().startsWith(NodePreferencesIds.NODE_PREFIX)) {
      setNodePreferences();
    }
  }

  /**
   * Create a new updater for the user's Eclipse preferences, and configure
   * it to keep the OGL renderer up to date.
   */
  public static RendererPreferences preparePreferences(GLPanel glPanel) {
    RendererPreferences result = new RendererPreferences(glPanel);
    result.setPreferences();

    // Register the new instance for preference changes
    IEclipsePreferences prefs = PreferencesIds.getInstanceNode();
    prefs.addPreferenceChangeListener(result);
    return result;
  }
}
