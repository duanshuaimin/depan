/*
 * Copyright 2008 The Depan Project Authors
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

package com.google.devtools.depan.eclipse.wizards;

import com.google.devtools.depan.eclipse.editors.GraphDocument;
import com.google.devtools.depan.eclipse.utils.WorkspaceTools;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Provide a standard component for specifying the output
 * of an analysis page.
 *
 * @author <a href="leeca@google.com">Lee Carver</a>
 */
public class AnalysisOutputPart {

  // External context for this part
  private WizardPage containingPage;
  private final String defaultFilename;
  private IContainer outputContainer;

  // UI elements
  private Text containerText;
  private Text fileText;
  private Group output;
  private String errorMsg;

  public AnalysisOutputPart(
      WizardPage containingPage, IContainer outputContainer,
      String defaultFilename) {
    this.containingPage = containingPage;
    this.outputContainer = outputContainer;
    this.defaultFilename = defaultFilename;
  }

  /**
   * Provide the composite GUI element.
   * 
   * @param container window context for the UI
   */
  public Composite createControl(Composite container) {
    output = new Group(container, SWT.NONE);
    output.setText("Analysis Output File");

    GridLayout grid = new GridLayout();
    grid.numColumns = 3;
    grid.verticalSpacing = 9;
    output.setLayout(grid);

    GridData fillHorz = new GridData(SWT.FILL, SWT.BEGINNING, true, false);

    // Row 1) Container selection
    Label label = new Label(output, SWT.NULL);
    label.setText("&Container:");

    containerText = new Text(output, SWT.BORDER | SWT.SINGLE);
    containerText.setLayoutData(fillHorz);
    if (null != outputContainer) {
      containerText.setText(outputContainer.getFullPath().toString());
    }

    Button button = new Button(output, SWT.PUSH);
    button.setText("Browse...");

    // Row 2) File selection
    label = new Label(output, SWT.NULL);
    label.setText("&File name:");
    fileText = new Text(output, SWT.BORDER | SWT.SINGLE);
    fileText.setLayoutData(fillHorz);
    fileText.setText(defaultFilename);

    // Cross-check inputs
    errorMsg = validateInputs();

    // Install listeners after initial value assignments
    containerText.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        dialogChanged();
      }
    });
    button.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        handleBrowse();
      }
    });
    fileText.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        dialogChanged();
      }
    });

    return output;
  }

  public String getErrorMsg() {
    return errorMsg;
  }

  public boolean isComplete() {
    return (null == getErrorMsg());
  }

  public String getContainerName() {
    return containerText.getText();
  }

  public String getFileName() {
    return fileText.getText();
  }

  /**
   * @param containerName
   * @param fileName
   * @return
   * @throws CoreException
   */
  public IFile getOutputFile() throws CoreException {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IResource resource = root.findMember(new Path(getContainerName()));
    if (!resource.exists() || !(resource instanceof IContainer)) {
      throwCoreException(
          "Container \"" + getContainerName() + "\" does not exist.");
    }

    IContainer container = (IContainer) resource;
    final IFile file = container.getFile(new Path(getFileName()));
    return file;
  }

  /**
   * Ensure that all inputs are valid.
   */
  private void dialogChanged() {
    errorMsg = validateInputs();

    containingPage.setPageComplete(containingPage.isPageComplete());
  }

  /**
   * Determine if the inputs are consistent.
   * 
   * @return error string if problems exist, or null if inputs are valid
   */
  private String validateInputs() {
    IResource container = ResourcesPlugin.getWorkspace().getRoot().findMember(
        new Path(getContainerName()));
    String fileName = getFileName();

    if (getContainerName().length() == 0) {
      return "File container must be specified";
    }
    if (container == null
        || (container.getType()
            & (IResource.PROJECT | IResource.FOLDER)) == 0) {
      return "File container must exist";
    }
    if (!container.isAccessible()) {
      return "Project must be writable";
    }
    if (fileName.length() == 0) {
      return "File name must be specified";
    }
    if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
      return "File name must be valid";
    }
    int dotLoc = fileName.lastIndexOf('.');
    if (dotLoc != -1) {
      String ext = fileName.substring(dotLoc + 1);
      if (!ext.equalsIgnoreCase(GraphDocument.EXTENSION)) {
        return "File extension must be \"." + GraphDocument.EXTENSION + "\"";
      }
    }
    return null;
  }

  /**
   * Uses the standard container selection dialog to choose the new value for
   * the container field.
   */
  private void handleBrowse() {
    containerText.setText(WorkspaceTools.selectProject(
        containingPage.getShell(), getContainerName()));
  }

  private void throwCoreException(String message) throws CoreException {
    IStatus status = new Status(IStatus.ERROR, "com.google.devtools.depan",
        IStatus.OK, message, null);
    throw new CoreException(status);
  }
}
