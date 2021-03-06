/*
 * Copyright 2007 The Depan Project Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.devtools.depan.eclipse.visualization.layout;

import com.google.devtools.depan.graph.api.EdgeMatcher;
import com.google.devtools.depan.model.GraphEdge;
import com.google.devtools.depan.model.GraphModel;
import com.google.devtools.depan.model.GraphNode;
import com.google.devtools.depan.view.HierarchicalTreeModel;
import com.google.devtools.depan.view.SuccessorEdges;
import com.google.devtools.depan.view.TreeModel;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Karlheinz Toni (JUNG pacakge)
 * @author Tom Nelson - converted to jung2
 * @author ycoppel@google.com (Yohann Coppel) - converted to any graph, even
 *         cyclic, based on JUNG TreeLayout implementation.
 */
public class UniversalTreeLayout extends
    AbstractLayout<GraphNode, GraphEdge> {

  public static final int DEFAULT_DIST_X = 50;
  public static final int DEFAULT_DIST_Y = 50;

  protected Map<GraphNode, Integer> basePositions =
      Maps.newHashMap();

  protected transient Set<GraphNode> allreadyDone =
      Sets.newHashSet();

  protected int distX = DEFAULT_DIST_X;
  protected int distY = DEFAULT_DIST_Y;
  protected transient Point mCurrentPoint = new Point();

  protected GraphModel graphModel;

  protected EdgeMatcher<String> edgeMatcher;

  TreeModel hierarchy = TreeModel.EMPTY;

  protected UniversalTreeLayout(
      Graph<GraphNode, GraphEdge> graph,
      GraphModel graphModel, EdgeMatcher<String> edgeMatcher,
      Dimension size) {
    super(graph, size);
    this.graphModel = graphModel;
    this.edgeMatcher = edgeMatcher;
  }

  @Override
  public void initialize() {
    allreadyDone.clear();
    hierarchy = new HierarchicalTreeModel(
        graphModel.computeSpanningHierarchy(edgeMatcher));
    buildTree();
  }

  private Collection<GraphNode> getSuccessors(GraphNode node) {
    SuccessorEdges successors = hierarchy.getSuccessors(node);
    return successors.computeSuccessorNodes();
  }

  /**
   * To be overridden
   */
  protected void buildTree() {
    Collection<GraphNode> roots = hierarchy.computeRoots();
    this.mCurrentPoint = new Point(0, 20);
    if (roots.size() > 0) {
      for (GraphNode node : roots) {
        calculateDimensionX(node);
        mCurrentPoint.x += this.basePositions.get(node) / 2 + 50;
        buildTree(node, this.mCurrentPoint.x);
      }
    }
  }

  private void buildTree(GraphNode v, int x) {
    if (!allreadyDone.contains(v)) {
      allreadyDone.add(v);

      // go one level further down
      this.mCurrentPoint.y += this.distY;
      this.mCurrentPoint.x = x;

      this.setCurrentPositionFor(v);

      int sizeXofCurrent = basePositions.get(v);

      int lastX = x - sizeXofCurrent / 2;

      int sizeXofChild;
      int startXofChild;

      for (GraphNode element : getSuccessors(v)) {
        sizeXofChild = this.basePositions.get(element);
        startXofChild = lastX + sizeXofChild / 2;
        buildTree(element, startXofChild);
        lastX = lastX + sizeXofChild + distX;
      }
      this.mCurrentPoint.y -= this.distY;
    }
  }

  private int calculateDimensionX(GraphNode v) {
    int size = 0;
    for (GraphNode element : getSuccessors(v)) {
      size += calculateDimensionX(element) + distX;
    }
    size = Math.max(0, size - distX);
    basePositions.put(v, size);
    return size;
  }

  public int getDepth(GraphNode v) {
    int depth = 0;
    for (GraphNode c : getSuccessors(v)) {

      if (getSuccessors(c).size() == 0) {
        depth = 0;
      } else {
        depth = Math.max(depth, getDepth(c));
      }
    }
    return depth + 1;
  }

  public int getMaxDepth(Collection<GraphNode> nodes) {
    int maxDepth = 0;
    for (GraphNode n : nodes) {
      maxDepth = Math.max(getDepth(n), maxDepth);
    }
    return maxDepth;
  }

  private void setCurrentPositionFor(GraphNode vertex) {
    locations.get(vertex).setLocation(mCurrentPoint);
  }

  @Override
  public void reset() {
    initialize();
  }
}
