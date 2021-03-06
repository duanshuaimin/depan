/*
 * Copyright 2009 The Depan Project Authors
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

package com.google.devtools.depan.eclipse.editors;

import com.google.devtools.depan.graph.api.Relation;
import com.google.devtools.depan.graph.api.RelationSet;
import com.google.devtools.depan.model.GraphEdge;
import com.google.devtools.depan.model.GraphNode;
import com.google.devtools.depan.view.CollapseData;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map;

/**
 * A listener for events that can occurs in ViewPreferences
 * 
 * @author <a href='mailto:leeca@google.com'>Lee Carver</a>
 */
public interface ViewPrefsListener {

  /**
   * Provides simple "ignore all events" bodies for every event method.
   */
  public class Simple implements ViewPrefsListener {

    @Override
    public void relationSetVisibleChanged(RelationSet visibleSet) {
    }

    @Override
    public void relationVisibleChanged(Relation relation, boolean visible) {
    }

    @Override
    public void edgePropertyChanged(
        GraphEdge node, EdgeDisplayProperty newProperty) {
    }

    @Override
    public void relationPropertyChanged(
        Relation relation, EdgeDisplayProperty newProperty) {
    }

    @Override
    public void nodePropertyChanged(
        GraphNode node, NodeDisplayProperty newProperty) {
    }

    @Override
    public void collapseChanged(Collection<CollapseData> created,
        Collection<CollapseData> removed, Object author) {
    }

    @Override
    public void nodeLocationsChanged(
        Map<GraphNode, Point2D> newLocations, Object author) {
    }

    @Override
    public void selectionChanged(Collection<GraphNode> previous,
        Collection<GraphNode> current, Object author) {
    }

    @Override
    public void descriptionChanged(String description) {
    }

    @Override
    public void nodeLocationsSet(Map<GraphNode, Point2D> newLocations) {
    }
  }

  /////////////////////////////////////
  // Relation visibility callbacks

  void relationSetVisibleChanged(RelationSet visibleSet);

  void relationVisibleChanged(Relation relation, boolean visible);

  /////////////////////////////////////
  // Edge related callbacks

  void edgePropertyChanged(
      GraphEdge node, EdgeDisplayProperty newProperty);

  void relationPropertyChanged(
      Relation relation, EdgeDisplayProperty newProperty);

  /////////////////////////////////////
  // Node related callbacks

  void nodePropertyChanged(
      GraphNode node, NodeDisplayProperty newProperty);

  /**
   * Callback when nodes locations are set.  Any nodes omitted from the map
   * are assumed to be at the origin (0.0, 0.0).
   * 
   * @param collection of nodes at new locations.
   * @param author the initiator of the change.
   */
  void nodeLocationsSet(Map<GraphNode, Point2D> newLocations);

  /**
   * Callback when node locations change.  Any nodes omitted from the map
   * remain at the current location.
   * 
   * @param collection of nodes at new locations.
   * @param author the initiator of the change.
   */
  void nodeLocationsChanged(
      Map<GraphNode, Point2D> newLocations, Object author);

  /**
   * Callback when nodes collapsing changes.
   * 
   * @param created Collection of new collapsed nodes
   * @param removed Collection of now un-collapsed nodes
   * @param author the initiator of the change
   */
  void collapseChanged(
      Collection<CollapseData> created,
      Collection<CollapseData> removed,
      Object author);

  /**
   * Callback for changes to the selected nodes.
   * 
   * @param created Collection of new collapsed nodes.
   * @param removed Collection of now un-collapsed nodes.
   * @param author the initiator of the change.
   */
  void selectionChanged(
      Collection<GraphNode> previous,
      Collection<GraphNode> current,
      Object author);

  void descriptionChanged(String description);
}
