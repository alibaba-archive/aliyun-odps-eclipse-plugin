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

package com.aliyun.odps.eclipse.launch.shortcut.udf;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
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

public class UDFShotcutConfigWizardPage extends WizardPage implements SelectionListener {

  private ILaunchConfigurationWorkingCopy lanchConfig;

  private List projectList;

  private ScrolledComposite scrolledComposite;
  private Button btnAddProject;
  private Button btnRemoveProject;
  private Button btnEditProject;

  private Text txtTable;
  private Text txtPartitions;
  private Text txtColumns;

  private java.util.List<ODPSProjectItem> odpsProjectList;
  private String runClassName;

  protected UDFShotcutConfigWizardPage(ILaunchConfigurationWorkingCopy lanchConfig,
      String runClassName) {
    super("ODPS UDF|UDTF Run Configuration");
    setDescription("ODPS UDF|UDTF Run Configuration");
    odpsProjectList = Activator.getDefault().getOdpsProjectList();
    this.lanchConfig = lanchConfig;
    this.runClassName = runClassName;

  }

  public void createControl(Composite parent) {
    Font font = parent.getFont();
    Composite comp = new Composite(parent, SWT.NONE);
    comp.setLayout(new GridLayout(1, false));
    comp.setFont(font);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.horizontalSpan = 1;
    comp.setLayoutData(gd);

    createRunClassName(comp);
    createSelectOdpsProjectGroup(comp);
    createInputGroup(comp);
    setControl(comp);
    updateWidgets();
    init();
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
      String tableInfo =
          lanchConfig.getAttribute(LaunchConfigurationConstants.ATTR_INPUT_TABLE_INFO, "").trim();
      if (!tableInfo.isEmpty()) {
        String[] ss = tableInfo.split("\\.");
        txtTable.setText(ss[0]);
        for (int i = 1; i < ss.length; i++) {
          String temp = ss[i].substring(2, ss[i].length() - 1);
          if (ss[i].startsWith("p(")) {
            txtPartitions.setText(temp);
          } else if (ss[i].startsWith("c(")) {
            txtColumns.setText(temp);
          }
        }
      }
    } catch (CoreException e) {
    }

    updateWidgets();
  }

  private void createRunClassName(Composite parent) {
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

  private void createSelectOdpsProjectGroup(Composite parent) {
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

  private void createInputGroup(Composite parent) {
    Font font = parent.getFont();
    Group group = new Group(parent, SWT.NONE);
    GridLayout topLayout = new GridLayout();
    topLayout.numColumns = 3;
    group.setLayout(topLayout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.heightHint = 120;
    group.setLayoutData(gd);
    group.setFont(font);
    group.setText("Input Table");

    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.widthHint = 120;
    Label label = new Label(group, SWT.NONE);
    label.setText("Table:");
    txtTable = new Text(group, SWT.BORDER);
    txtTable.setLayoutData(gd);
    new Label(group, SWT.NONE);

    label = new Label(group, SWT.NONE);
    label.setText("Partitions:");
    txtPartitions = new Text(group, SWT.BORDER);
    txtPartitions.setLayoutData(gd);
    label = new Label(group, SWT.NONE);
    label.setText("ie: p1=1,p2=1 (default all partitions)");

    label = new Label(group, SWT.NONE);
    label.setText("Columns:");
    txtColumns = new Text(group, SWT.BORDER);
    txtColumns.setLayoutData(gd);
    label = new Label(group, SWT.NONE);
    label.setText("ie: c1,c2,c3 (default all columns)");
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

  public String getOdpsProject() {
    int index = projectList.getSelectionIndex();
    if (index == -1) {
      return null;
    }
    ODPSProjectItem projectItem = odpsProjectList.get(index);
    String project = ODPSProjectItemUtil.convert(projectItem);
    return project;
  }


  public String getInputTableInfo() {
    StringBuffer tableInfo = new StringBuffer(txtTable.getText().trim());
    String temp = txtPartitions.getText().trim();
    if (!temp.isEmpty()) {
      tableInfo.append(".p(");
      tableInfo.append(temp);
      tableInfo.append(")");
    }
    temp = txtColumns.getText().trim();
    if (!temp.isEmpty()) {
      tableInfo.append(".c(");
      tableInfo.append(temp);
      tableInfo.append(")");
    }
    return tableInfo.toString();

  }

  public boolean canFinish() {
    if (projectList.getSelectionIndex() == -1 || txtTable.getText().trim().isEmpty()) {
      return false;
    } else {
      return true;
    }
  }

  @Override
  public void widgetSelected(SelectionEvent event) {
    if (event.getSource() == projectList) {
      updateWidgets();
    } else if (event.getSource() == btnAddProject) {
      ProjectEditorDialog projectOpDialog =
          new ProjectEditorDialog(UDFShotcutConfigWizardPage.this.getShell(), null,
              ProjectOp.INSERT);
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
          new ProjectEditorDialog(UDFShotcutConfigWizardPage.this.getShell(),
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
  public void widgetDefaultSelected(SelectionEvent event) {

  }

}
