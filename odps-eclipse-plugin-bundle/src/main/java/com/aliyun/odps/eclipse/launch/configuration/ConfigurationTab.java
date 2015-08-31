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

package com.aliyun.odps.eclipse.launch.configuration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.internal.debug.ui.actions.ControlAccessibleListener;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import com.aliyun.odps.eclipse.Activator;
import com.aliyun.odps.eclipse.constants.LaunchConfigurationConstants;
import com.aliyun.odps.eclipse.template.ODPSProjectItem;
import com.aliyun.odps.eclipse.template.ProjectEditorDialog;
import com.aliyun.odps.eclipse.template.ProjectEditorDialog.ProjectOp;
import com.aliyun.odps.eclipse.utils.ConfigurationPersistenceUtil;
import com.aliyun.odps.eclipse.utils.ODPSProjectItemUtil;

public class ConfigurationTab extends AbstractLaunchConfigurationTab implements SelectionListener,
    TraverseListener {

  private final static String SELECT_ODPS_PROJECT_TXT = "Select ODPS project";

  protected Text txtResources;
  private ScrolledComposite scrolledComposite;
  private Button removeProjectBtn;
  private Button editProjectBtn;
  private List projectList;
  private Button localModeRadio;
  private Button remoteModeRadio;
  private Button addProjectBtn;

  private java.util.List<ODPSProjectItem> odpsProjectList;

  protected ConfigurationTab() {
    odpsProjectList = Activator.getDefault().getOdpsProjectList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.
   * swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    Font font = parent.getFont();
    Composite comp = new Composite(parent, SWT.NONE);
    comp.setLayout(new GridLayout(1, false));
    comp.setFont(font);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.horizontalSpan = 1;
    comp.setLayoutData(gd);

    createRunModeGroup(comp);
    createSelectOdpsProjectGroup(comp);
    createResourceGroup(comp);
    setControl(comp);

  }

  private void createRunModeGroup(Composite parent) {
    Font font = parent.getFont();

    Group group = new Group(parent, SWT.NONE);
    GridLayout topLayout = new GridLayout();
    topLayout.numColumns = 2;
    group.setLayout(topLayout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.heightHint = 50;
    group.setLayoutData(gd);
    group.setFont(font);
    group.setText("Run Mode");

    localModeRadio = new Button(group, SWT.RADIO);
    localModeRadio.setSelection(true);
    localModeRadio.setText("Local");
    localModeRadio.addSelectionListener(this);
    remoteModeRadio = new Button(group, SWT.RADIO);
    remoteModeRadio.setText("Remote");

    // remoteModeRadio.addSelectionListener(this);
    remoteModeRadio.setEnabled(false);

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
    group.setText(SELECT_ODPS_PROJECT_TXT);

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

    addProjectBtn = new Button(composite, SWT.NONE);
    addProjectBtn.addSelectionListener(this);
    addProjectBtn.setText("Add");

    editProjectBtn = new Button(composite, SWT.NONE);
    editProjectBtn.addSelectionListener(this);
    editProjectBtn.setText("Edit");

    removeProjectBtn = new Button(composite, SWT.NONE);
    removeProjectBtn.addSelectionListener(this);
    removeProjectBtn.setText("Remove");

  }

  private void createResourceGroup(Composite parent) {
    Font font = parent.getFont();
    Group group = new Group(parent, SWT.NONE);
    GridLayout topLayout = new GridLayout();
    group.setLayout(topLayout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.heightHint = 90;
    group.setLayoutData(gd);
    group.setFont(font);
    group.setText("Resources");

    txtResources = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
    txtResources.addTraverseListener(this);
    gd = new GridData(GridData.FILL_BOTH);
    gd.heightHint = 40;
    gd.widthHint = 100;
    txtResources.setLayoutData(gd);
    txtResources.setFont(font);
    txtResources.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent evt) {
        updateLaunchConfigurationDialog();
      }
    });
    ControlAccessibleListener.addListener(txtResources, group.getText());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug
   * .core.ILaunchConfigurationWorkingCopy)
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {}

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse
   * .debug.core.ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration configuration) {
    String resources;
    try {
      resources = configuration.getAttribute(LaunchConfigurationConstants.ATTR_RESOURCES, "");
      txtResources.setText(resources);

      boolean flag = configuration.getAttribute(LaunchConfigurationConstants.ATTR_RUN_MODE, true);
      if (flag) {
        localModeRadio.setSelection(true);
        remoteModeRadio.setSelection(false);
      } else {
        localModeRadio.setSelection(false);
        remoteModeRadio.setSelection(true);
      }
    } catch (CoreException e) {
      e.printStackTrace();
    }

    projectList.removeAll();

    ODPSProjectItem projectItem = null;
    try {
      String project = configuration.getAttribute(LaunchConfigurationConstants.ATTR_PROJECT, "");
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

    updateWidgets();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug
   * .core.ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    String resources = txtResources.getText().trim();
    configuration.setAttribute(LaunchConfigurationConstants.ATTR_RESOURCES, resources);

    boolean flag = localModeRadio.getSelection();
    if (flag) {
      configuration.setAttribute(LaunchConfigurationConstants.ATTR_RUN_MODE, true);
    } else {
      configuration.setAttribute(LaunchConfigurationConstants.ATTR_RUN_MODE, false);
    }

    int index = projectList.getSelectionIndex();
    if (index != -1) {
      ODPSProjectItem projectItem = odpsProjectList.get(index);
      String project = ODPSProjectItemUtil.convert(projectItem);
      configuration.setAttribute(LaunchConfigurationConstants.ATTR_PROJECT, project);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  public String getName() {
    return "ODPS Config";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.
   * core.ILaunchConfiguration)
   */
  public boolean isValid(ILaunchConfiguration launchConfig) {
    if (odpsProjectList.size() == 0 || projectList.getSelectionIndex() == -1) {
      return false;
    }
    return true;
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
      editProjectBtn.setEnabled(false);
      removeProjectBtn.setEnabled(false);
    } else {
      editProjectBtn.setEnabled(true);
      removeProjectBtn.setEnabled(true);
    }

    updateLaunchConfigurationDialog();
  }

  @Override
  public void widgetSelected(SelectionEvent event) {
    if (event.getSource() == projectList) {
      updateWidgets();
    } else if (event.getSource() == localModeRadio) {
      updateLaunchConfigurationDialog();
    } else if (event.getSource() == remoteModeRadio) {
      updateLaunchConfigurationDialog();
    } else if (event.getSource() == addProjectBtn) {
      ProjectEditorDialog projectOpDialog =
          new ProjectEditorDialog(ConfigurationTab.this.getShell(), null, ProjectOp.INSERT);
      if (projectOpDialog.open() == TitleAreaDialog.OK) {
        ConfigurationPersistenceUtil.addProject(projectOpDialog.getProjectItem(), odpsProjectList,
            projectList);
        updateWidgets();
      }

    } else if (event.getSource() == editProjectBtn) {
      int index = projectList.getSelectionIndex();
      if (index < 0) {
        return;
      }
      ProjectEditorDialog projectOpDialog =
          new ProjectEditorDialog(ConfigurationTab.this.getShell(), odpsProjectList.get(index),
              ProjectOp.EDIT);
      if (projectOpDialog.open() == TitleAreaDialog.OK) {
        ConfigurationPersistenceUtil.editProject(projectOpDialog.getProjectItem(), odpsProjectList,
            projectList);
        updateWidgets();
      }

    } else if (event.getSource() == removeProjectBtn) {
      ConfigurationPersistenceUtil.removeProject(odpsProjectList, projectList);
      updateWidgets();

    }

  }

  @Override
  public void widgetDefaultSelected(SelectionEvent event) {

  }

  @Override
  public void keyTraversed(TraverseEvent event) {
    if (event.getSource() == txtResources) {
      switch (event.detail) {
        case SWT.TRAVERSE_ESCAPE:
        case SWT.TRAVERSE_PAGE_NEXT:
        case SWT.TRAVERSE_PAGE_PREVIOUS:
          event.doit = true;
          break;
        case SWT.TRAVERSE_RETURN:
        case SWT.TRAVERSE_TAB_NEXT:
        case SWT.TRAVERSE_TAB_PREVIOUS:
          if ((txtResources.getStyle() & SWT.SINGLE) != 0) {
            event.doit = true;
          } else {
            if (!txtResources.isEnabled() || (event.stateMask & SWT.MODIFIER_MASK) != 0) {
              event.doit = true;
            }
          }
          break;
      }
    }
  }

}
