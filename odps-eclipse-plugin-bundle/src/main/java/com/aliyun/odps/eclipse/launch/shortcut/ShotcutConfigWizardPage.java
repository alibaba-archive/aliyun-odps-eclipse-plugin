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

package com.aliyun.odps.eclipse.launch.shortcut;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.internal.debug.ui.actions.ControlAccessibleListener;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import com.aliyun.odps.eclipse.Activator;
import com.aliyun.odps.eclipse.constants.LaunchConfigurationConstants;
import com.aliyun.odps.eclipse.template.ODPSProjectItem;
import com.aliyun.odps.eclipse.template.ProjectEditorDialog;
import com.aliyun.odps.eclipse.template.ProjectEditorDialog.ProjectOp;
import com.aliyun.odps.eclipse.utils.ConfigurationPersistenceUtil;
import com.aliyun.odps.eclipse.utils.ODPSProjectItemUtil;

public class ShotcutConfigWizardPage extends WizardPage implements SelectionListener {

  private ILaunchConfigurationWorkingCopy lanchConfig;

  private Text txtProgramArgs;
  private Text txtResources;
  private List projectList;
  private Button radioLocalMode;
  private ScrolledComposite scrolledComposite;
  private Button btnAddProject;
  private Button btnRemoveProject;
  private Button btnEditProject;
  private Button radioRemoteMode;

  private java.util.List<ODPSProjectItem> odpsProjectList;
  private String runClassName;

  protected ShotcutConfigWizardPage(final ILaunchConfigurationWorkingCopy lanchConfig,
      final String runClassName) {
    super("ODPS Mapreduce Run Configuration");
    setDescription("ODPS Mapreduce Run Configuration");
    odpsProjectList = Activator.getDefault().getOdpsProjectList();
    this.lanchConfig = lanchConfig;
    this.runClassName = runClassName;

  }

  @Override
  public void createControl(final Composite parent) {
    Font font = parent.getFont();
    Composite comp = new Composite(parent, SWT.NONE);
    comp.setLayout(new GridLayout(1, false));
    comp.setFont(font);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.horizontalSpan = 1;
    comp.setLayoutData(gd);

    createRunClassName(comp);
    createRunModeGroup(comp);
    createSelectOdpsProjectGroup(comp);
    createResourceGroup(comp);
    setControl(comp);
    createProgramArgsGroup(comp);
    updateWidgets();
    init();

  }

  private void createRunClassName(final Composite parent) {
    Font font = parent.getFont();

    Group group = new Group(parent, SWT.NONE);
    GridLayout topLayout = new GridLayout();
    topLayout.numColumns = 1;
    group.setLayout(topLayout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.heightHint = 36;
    group.setLayoutData(gd);
    group.setFont(font);
    group.setText("Class");

    Label label = new Label(group, SWT.NONE);
    label.setText(runClassName);
  }

  private void createRunModeGroup(final Composite parent) {
    Font font = parent.getFont();

    Group group = new Group(parent, SWT.NONE);
    GridLayout topLayout = new GridLayout();
    topLayout.numColumns = 2;
    group.setLayout(topLayout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.heightHint = 36;
    group.setLayoutData(gd);
    group.setFont(font);
    group.setText("Run Mode");

    radioLocalMode = new Button(group, SWT.RADIO);
    radioLocalMode.setSelection(true);
    radioLocalMode.setText("Local");
    radioLocalMode.addSelectionListener(this);
    radioRemoteMode = new Button(group, SWT.RADIO);
    radioRemoteMode.setText("Remote");

    // radioRemoteMode.addSelectionListener(this);
    radioRemoteMode.setEnabled(false);

  }

  private void createSelectOdpsProjectGroup(final Composite parent) {
    Font font = parent.getFont();

    Group group = new Group(parent, SWT.NONE);
    GridLayout topLayout = new GridLayout();
    topLayout.numColumns = 2;
    group.setLayout(topLayout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    group.setLayoutData(gd);
    group.setFont(font);
    group.setText("Select ODPS Project");

    scrolledComposite = new ScrolledComposite(group, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    GridData gd_scrolledComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
    gd_scrolledComposite.heightHint = 90;
    gd_scrolledComposite.widthHint = 360;
    scrolledComposite.setLayoutData(gd_scrolledComposite);
    scrolledComposite.setExpandHorizontal(true);
    scrolledComposite.setExpandVertical(true);

    projectList = new List(scrolledComposite, SWT.BORDER);
    scrolledComposite.setContent(projectList);
    scrolledComposite.setMinSize(projectList.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    projectList.addSelectionListener(this);

    Composite composite = new Composite(group, SWT.NONE);
    FillLayout fl_composite = new FillLayout(SWT.VERTICAL);
    fl_composite.spacing = 5;
    composite.setLayout(fl_composite);
    // composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false,
    // 3, 1));

    btnAddProject = new Button(composite, SWT.NONE);
    btnAddProject.setText("Add");

    btnEditProject = new Button(composite, SWT.NONE);
    btnEditProject.setText("Edit");

    btnRemoveProject = new Button(composite, SWT.NONE);
    btnRemoveProject.setText("Remove");

    btnAddProject.addSelectionListener(this);
    btnEditProject.addSelectionListener(this);
    btnRemoveProject.addSelectionListener(this);

  }

  private void createResourceGroup(final Composite parent) {
    Font font = parent.getFont();
    Group group = new Group(parent, SWT.NONE);
    GridLayout topLayout = new GridLayout();
    group.setLayout(topLayout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.heightHint = 60;
    group.setLayoutData(gd);
    group.setFont(font);
    group.setText("Resources");

    txtResources = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
    gd = new GridData(GridData.FILL_BOTH);
    gd.heightHint = 40;
    gd.widthHint = 100;
    txtResources.setLayoutData(gd);
    txtResources.setFont(font);
    ControlAccessibleListener.addListener(txtResources, group.getText());
  }

  private void createProgramArgsGroup(final Composite parent) {
    Font font = parent.getFont();
    Group group = new Group(parent, SWT.NONE);
    GridLayout topLayout = new GridLayout();
    group.setLayout(topLayout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.heightHint = 60;
    group.setLayoutData(gd);
    group.setFont(font);
    group.setText("Program Arguments");

    txtProgramArgs = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
    gd = new GridData(GridData.FILL_BOTH);
    gd.heightHint = 40;
    gd.widthHint = 100;
    txtProgramArgs.setLayoutData(gd);
    txtProgramArgs.setFont(font);
    ControlAccessibleListener.addListener(txtResources, group.getText());
  }

  private void init() {

    ODPSProjectItem projectItem = null;
    try {
      String project = lanchConfig.getAttribute(LaunchConfigurationConstants.ATTR_PROJECT, "");
      projectItem = ODPSProjectItemUtil.convert(project);
    } catch (CoreException e) {
    }

    for (int i = 0; i < odpsProjectList.size(); i++) {
      ODPSProjectItem item = odpsProjectList.get(i);
      projectList.add(item.getProject(), i);
      if (projectItem != null && projectItem.getProject().trim().equals(item.getProject().trim())) {
        projectList.setSelection(i);
      }
    }

    try {
      txtResources.setText(lanchConfig
          .getAttribute(LaunchConfigurationConstants.ATTR_RESOURCES, ""));
    } catch (CoreException e) {
    }
    try {
      txtProgramArgs.setText(lanchConfig.getAttribute(
          IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""));
    } catch (CoreException e) {
    }

    try {
      boolean flag = lanchConfig.getAttribute(LaunchConfigurationConstants.ATTR_RUN_MODE, true);
      if (flag) {
        radioLocalMode.setSelection(true);
        radioRemoteMode.setSelection(false);
      } else {
        radioLocalMode.setSelection(false);
        radioRemoteMode.setSelection(true);
      }
    } catch (CoreException e) {
    }

    updateWidgets();
  }

  private void updateWidgets() {
    scrolledComposite.setContent(projectList);
    scrolledComposite.setMinSize(projectList.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    int index = projectList.getSelectionIndex();
    if (index == -1 && projectList.getItemCount() > 0) {
      index = 0;
      projectList.setSelection(0);
    }
    if (index == -1) {
      btnEditProject.setEnabled(false);
      btnRemoveProject.setEnabled(false);
      setPageComplete(false);
    } else {
      btnEditProject.setEnabled(true);
      btnRemoveProject.setEnabled(true);
      setPageComplete(true);
    }
  }

  public String getOdpsMrResources() {
    return txtResources.getText().trim();
  }

  public boolean isOdpsLocalRunMode() {
    return radioLocalMode.getSelection();
  }

  public String getOdpsProject() {
    int index = projectList.getSelectionIndex();
    if (index == -1) {
      return null;
    }
    ODPSProjectItem projectItem = odpsProjectList.get(index);
    String project = ODPSProjectItemUtil.convert(projectItem);
    return project;
  }

  public String getProgramArgs() {
    return txtProgramArgs.getText().trim();
  }

  public boolean canFinish() {
    if (projectList.getSelectionIndex() == -1) {
      return false;
    } else {
      return true;
    }
  }

  @Override
  public void widgetSelected(final SelectionEvent event) {
    if (event.getSource() == projectList) {
      updateWidgets();
    } else if (event.getSource() == radioLocalMode) {

    } else if (event.getSource() == btnAddProject) {
      ProjectEditorDialog projectOpDialog =
          new ProjectEditorDialog(ShotcutConfigWizardPage.this.getShell(), null, ProjectOp.INSERT);
      if (projectOpDialog.open() == TitleAreaDialog.OK) {
        ConfigurationPersistenceUtil.addProject(projectOpDialog.getProjectItem(), odpsProjectList,
            projectList);
        updateWidgets();
      }

    } else if (event.getSource() == btnEditProject) {
      int index = projectList.getSelectionIndex();
      if (index < 0) {
        return;
      }
      ProjectEditorDialog projectOpDialog =
          new ProjectEditorDialog(ShotcutConfigWizardPage.this.getShell(),
              odpsProjectList.get(index), ProjectOp.EDIT);
      if (projectOpDialog.open() == TitleAreaDialog.OK) {
        ConfigurationPersistenceUtil.editProject(projectOpDialog.getProjectItem(), odpsProjectList,
            projectList);
        updateWidgets();
      }

    } else if (event.getSource() == btnRemoveProject) {
      ConfigurationPersistenceUtil.removeProject(odpsProjectList, projectList);
      updateWidgets();

    }

  }

  @Override
  public void widgetDefaultSelected(final SelectionEvent event) {

  }

}
