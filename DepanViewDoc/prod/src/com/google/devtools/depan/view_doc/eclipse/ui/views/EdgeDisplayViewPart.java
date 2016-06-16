/*
 * Copyright 2007 The Depan Project Authors
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

package com.google.devtools.depan.view_doc.eclipse.ui.views;

import com.google.devtools.depan.graph.api.Relation;
import com.google.devtools.depan.graph.api.RelationSet;
import com.google.devtools.depan.graph.registry.RelationRegistry;
import com.google.devtools.depan.relations.eclipse.ui.wizards.NewRelationSetWizard;
import com.google.devtools.depan.relations.models.RelationSetDescriptor;
import com.google.devtools.depan.relations.persistence.RelationSetDescriptorXmlPersist;
import com.google.devtools.depan.view_doc.eclipse.ViewDocResources;
import com.google.devtools.depan.view_doc.eclipse.ui.editor.ViewEditor;
import com.google.devtools.depan.view_doc.eclipse.ui.widgets.EdgeDisplayTableControl;
import com.google.devtools.depan.view_doc.model.EdgeDisplayProperty;
import com.google.devtools.depan.view_doc.model.EdgeDisplayRepository;
import com.google.devtools.depan.view_doc.model.ViewPrefsListener;

import com.google.common.collect.Maps;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import java.io.File;
import java.net.URI;
import java.util.Map;

/**
 * Tool for selecting relations that have to be shown.
 *
 * @author ycoppel@google.com (Yohann Coppel)
 */
public class EdgeDisplayViewPart extends AbstractViewDocViewPart {

  public static final String PART_NAME = "Edge Properties";

  /**
   * The <code>RelationSetEditorControl</code> that controls the UX.
   */
  private EdgeDisplayTableControl propEditor;

  private EdgeDisplayRepository propRepo;

  private static class ToolEdgeDisplayRepo
      implements EdgeDisplayRepository {

    private final ViewEditor editor;

    private EdgeDisplayListener prefsListener;

    private Map<Relation, EdgeDisplayProperty> tempRows;

    public ToolEdgeDisplayRepo(ViewEditor editor) {
      this.editor = editor;
    }

    @Override
    public EdgeDisplayProperty getDisplayProperty(Relation relation) {
      EdgeDisplayProperty result = editor.getRelationProperty(relation);
      if (null != result) {
        return result;
      }
      return getTempRow(relation);
    }

    private EdgeDisplayProperty getTempRow(Relation relation) {
      if (null == tempRows) {
        tempRows = Maps.newHashMap();
      }
      EdgeDisplayProperty result = tempRows.get(relation);
      if (null != result) {
        return result;
      }
      result = new EdgeDisplayProperty();
      tempRows.put(relation, result);
      return result;
    }

    @Override
    public void setDisplayProperty(Relation relation, EdgeDisplayProperty props) {
      editor.setRelationProperty(relation, props);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
      prefsListener = new EdgeDisplayListener(listener);
      editor.addViewPrefsListener(prefsListener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
      // TODO: if multiple ChangeListener,
      // add Map<ChangeListener, EdgeDisplayListener>
      editor.removeViewPrefsListener(prefsListener);
    }
  }

  private static class EdgeDisplayListener extends ViewPrefsListener.Simple {

    private EdgeDisplayRepository.ChangeListener listener;

    public EdgeDisplayListener(EdgeDisplayRepository.ChangeListener listener) {
      this.listener = listener;
    }

    @Override
    public void relationPropertyChanged(Relation relation,
        EdgeDisplayProperty newProperty) {
      listener.edgeDisplayChanged(relation, newProperty);
    }
  }

  @Override
  public Image getTitleImage() {
    return ViewDocResources.IMAGE_RELATIONPICKER;
  }

  @Override
  public String getTitle() {
    return PART_NAME;
  }

  @Override
  protected void createGui(Composite parent) {
    Composite result = new Composite(parent, SWT.NONE);
    result.setLayout(new GridLayout());

    propEditor = new EdgeDisplayTableControl(result);
    propEditor.setLayoutData(
        new GridData(SWT.FILL, SWT.FILL, true, false));

    Composite saves = setupSaveButtons(result);
    saves.setLayoutData(
        new GridData(SWT.FILL, SWT.FILL, true, false));
  }

  @Override
  protected void disposeGui() {
    releaseResources();
  }

  private Composite setupSaveButtons(Composite parent) {
    Composite result = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(2, false);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    result.setLayout(layout);

    Button saveRels = new Button(result, SWT.PUSH);
    saveRels.setText("Save selected as an Edge Display resource...");
    saveRels.setLayoutData(
        new GridData(SWT.FILL, SWT.FILL, true, false));

    saveRels.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        saveSelection();
      }
    });

    Button saveProps = new Button(result, SWT.PUSH);
    saveProps.setText("Load properties from Edge Display resource...");
    saveProps.setLayoutData(
        new GridData(SWT.FILL, SWT.FILL, true, false));

    saveProps.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        loadSelection();
      }
    });

    return result;
  }

  /**
   * Open a dialog to save the current selection under a new name.
   */
  private void saveSelection() {

    NewRelationSetWizard wizard =
        new NewRelationSetWizard(getEditor().getVisibleRelationSet());

    Shell shell = getSite().getWorkbenchWindow().getShell();
    WizardDialog dialog = new WizardDialog(shell, wizard);
    dialog.open();
  }

  private void loadSelection() {
    Shell shell = getSite().getWorkbenchWindow().getShell();
    FileDialog dialog = new FileDialog(shell, SWT.OPEN);
    dialog.setFilterExtensions(new String[] {RelationSetDescriptor.EXTENSION});
    String visFilename = dialog.open();
    if (null == visFilename) {
      return;
    }

    URI visURI = new File(visFilename).toURI();
    RelationSetDescriptorXmlPersist loader =
        RelationSetDescriptorXmlPersist.build(true);
    RelationSetDescriptor visDescr = loader.load(visURI);
    RelationSet visRels = visDescr.getRelationSet();

    ViewEditor ed = getEditor();
    for (Relation relation : RelationRegistry.getRegistryRelations()) {
      ed.setVisibleRelation(relation, visRels.contains(relation));
    }
  }

  /////////////////////////////////////
  // ViewDoc/Editor integration

  @Override
  protected void acquireResources() {

    ViewEditor editor = getEditor();

    propRepo = new ToolEdgeDisplayRepo(editor);
    propEditor.setEdgeDisplayRepository(propRepo);

    // TODO: Should come from editor
    propEditor.setInput(RelationRegistry.getRegistryRelations());
    propEditor.update();
  }

  @Override
  protected void releaseResources() {
    propEditor.removeEdgeDisplayRepository(propRepo);
    propRepo = null;
  }
}
