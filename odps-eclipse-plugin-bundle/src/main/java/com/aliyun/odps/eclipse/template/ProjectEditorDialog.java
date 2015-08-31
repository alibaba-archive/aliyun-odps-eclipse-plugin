/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.aliyun.odps.eclipse.template;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class ProjectEditorDialog extends TitleAreaDialog {
  public static enum ProjectOp {
    INSERT, EDIT
  }

  private Text txtProject;
  private Text txtEndpoint;
  private Text txtAccessId;
  private Text txtAccessKey;

  private ODPSProjectItem projectItem;
  private ProjectOp opMode;

  public ProjectEditorDialog(Shell parentShell, ODPSProjectItem projectItem, ProjectOp opMode) {
    super(parentShell);
    setBlockOnOpen(true);
    this.opMode = opMode;
    if (opMode == ProjectOp.INSERT) {
      this.projectItem = new ODPSProjectItem();
    } else {
      this.projectItem = projectItem;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.
   * swt.widgets.Composite)
   */
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);

    Composite panel = new Composite(area, SWT.NONE);
    panel.setLayout(new GridLayout(2, false));
    panel.setLayoutData(new GridData(GridData.FILL_BOTH));

    Label lblNewLabel = new Label(panel, SWT.NONE);
    lblNewLabel.setText("Project:");

    txtProject = new Text(panel, SWT.BORDER);
    txtProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    Label lblEndpoint = new Label(panel, SWT.NONE);
    lblEndpoint.setText("Endpoint:");

    txtEndpoint = new Text(panel, SWT.BORDER);
    txtEndpoint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    Label lblAccessid = new Label(panel, SWT.NONE);
    lblAccessid.setText("AccessId:");

    txtAccessId = new Text(panel, SWT.BORDER);
    txtAccessId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    Label lblAccesskey = new Label(panel, SWT.NONE);
    lblAccesskey.setText("Accesskey:");

    txtAccessKey = new Text(panel, SWT.BORDER);
    txtAccessKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    setTitle("Odps Project");
    setMessage("Please set odps project information");

    init();

    return area;
  }

  private void init() {
    if (opMode == ProjectOp.EDIT) {
      txtProject.setText(projectItem.getProject());
      txtEndpoint.setText(projectItem.getEndpoint());
      txtAccessId.setText(projectItem.getAccessId());
      txtAccessKey.setText(projectItem.getAccessKey());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.
   * swt.widgets.Composite)
   */
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  protected void okPressed() {
    String project = txtProject.getText().trim();
    if (project.isEmpty()) {
      setErrorMessage("Project can't be null!");
      return;
    }
    String endpoint = txtEndpoint.getText().trim();
    if (endpoint.isEmpty()) {
      setErrorMessage("Endpoint can't be null");
      return;
    }
    String accessId = txtAccessId.getText().trim();
    if (accessId.isEmpty()) {
      setErrorMessage("AccessId can't be null");
      return;
    }
    String accessKey = txtAccessKey.getText().trim();
    if (accessKey.isEmpty()) {
      setErrorMessage("AccessKey can't be null");
      return;
    }

    projectItem.setProject(project);
    projectItem.setEndpoint(endpoint);
    projectItem.setAccessId(accessId);
    projectItem.setAccessKey(accessKey);

    setReturnCode(OK);
    close();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#getInitialSize()
   */
  protected Point getInitialSize() {
    return new Point(500, 375);

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets. Shell)
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    if (opMode == ProjectOp.INSERT) {
      newShell.setText("Create ODPS Project");
    } else {
      newShell.setText("Edit ODPS Project");
    }

  }

  public ODPSProjectItem getProjectItem() {
    return projectItem;
  }

}
