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
package com.aliyun.odps.eclipse.launch.configuration.udf;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.internal.debug.ui.IJavaDebugHelpContextIds;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.DebugTypeSelectionDialog;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.aliyun.odps.eclipse.Activator;
import com.aliyun.odps.eclipse.constants.LaunchConfigurationConstants;
import com.aliyun.odps.eclipse.template.ODPSProjectItem;
import com.aliyun.odps.eclipse.template.ProjectEditorDialog;
import com.aliyun.odps.eclipse.template.ProjectEditorDialog.ProjectOp;
import com.aliyun.odps.eclipse.utils.ConfigurationPersistenceUtil;
import com.aliyun.odps.eclipse.utils.ODPSProjectItemUtil;

/**
 * A launch configuration tab that displays and edits project and main type name launch
 * configuration attributes.
 * <p>
 * This class may be instantiated.
 * </p>
 * 
 * @since 3.2
 * @noextend This class is not intended to be subclassed by clients.
 */

public class UDFClassTab extends SharedUDFClassTab implements SelectionListener {

  /**
   * Boolean launch configuration attribute indicating that external jars (on the runtime classpath)
   * should be searched when looking for a main type. Default value is <code>false</code>.
   * 
   * @since 2.1
   */
  public static final String ATTR_INCLUDE_EXTERNAL_JARS = IJavaDebugUIConstants.PLUGIN_ID
      + ".INCLUDE_EXTERNAL_JARS"; //$NON-NLS-1$
  /**
   * Boolean launch configuration attribute indicating whether types inheriting a main method should
   * be considered when searching for a main type. Default value is <code>false</code>.
   * 
   * @since 3.0
   */
  public static final String ATTR_CONSIDER_INHERITED_MAIN = IJavaDebugUIConstants.PLUGIN_ID
      + ".CONSIDER_INHERITED_MAIN"; //$NON-NLS-1$ 

  private final static String SELECT_ODPS_PROJECT_TXT = "Select ODPS project";

  private ScrolledComposite scrolledComposite;
  private Button addProjectBtn;
  private Button removeProjectBtn;
  private Button editProjectBtn;

  private Text txtTable;
  private Text txtPartitions;
  private Text txtColumns;

  private List projectList;

  private java.util.List<ODPSProjectItem> odpsProjectList;

  public UDFClassTab() {
    odpsProjectList = Activator.getDefault().getOdpsProjectList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.
   * swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
    ((GridLayout) comp.getLayout()).verticalSpacing = 0;
    createProjectEditor(comp);
    createVerticalSpacer(comp, 1);
    createMainTypeEditor(comp, "&UDF|UDTF class:");
    createSelectOdpsProjectGroup(comp);
    createInputGroup(comp);
    setControl(comp);
    PlatformUI.getWorkbench().getHelpSystem()
        .setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);
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

    txtTable.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent evt) {
        updateLaunchConfigurationDialog();
      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
   */
  public Image getImage() {
    return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  public String getName() {
    return "UDF|UDTF";
  }

  /**
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
   * 
   * @since 3.3
   */
  public String getId() {
    return "org.eclipse.jdt.debug.ui.javaMainTab"; //$NON-NLS-1$
  }

  /**
   * Show a dialog that lists all main types
   */
  protected void handleSearchButtonSelected() {
    IJavaProject project = getJavaProject();
    IJavaElement[] elements = null;
    if ((project == null) || !project.exists()) {
      IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
      if (model != null) {
        try {
          elements = model.getJavaProjects();
        } catch (JavaModelException e) {
          JDIDebugUIPlugin.log(e);
        }
      }
    } else {
      elements = new IJavaElement[] {project};
    }
    if (elements == null) {
      elements = new IJavaElement[] {};
    }
    int constraints = IJavaSearchScope.SOURCES;
    constraints |= IJavaSearchScope.APPLICATION_LIBRARIES;
    // constraints |= IJavaSearchScope.SYSTEM_LIBRARIES;

    IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(elements, constraints);
    UDFSearchEngine engine = new UDFSearchEngine();
    IType[] types = null;
    try {
      types = engine.searchUDFClass(getLaunchConfigurationDialog(), searchScope, false);
    } catch (InvocationTargetException e) {
      setErrorMessage(e.getMessage());
      return;
    } catch (InterruptedException e) {
      setErrorMessage(e.getMessage());
      return;
    }
    DebugTypeSelectionDialog mmsd =
        new DebugTypeSelectionDialog(getShell(), types,
            LauncherMessages.JavaMainTab_Choose_Main_Type_11);
    if (mmsd.open() == Window.CANCEL) {
      return;
    }
    Object[] results = mmsd.getResult();
    IType type = (IType) results[0];
    if (type != null) {
      fMainText.setText(type.getFullyQualifiedName());
      fProjText.setText(type.getJavaProject().getElementName());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jdt.internal.debug.ui.launcher.AbstractJavaMainTab#initializeFrom
   * (org.eclipse.debug.core.ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration config) {
    super.initializeFrom(config);
    updateMainTypeFromConfig(config);
    updateStopInMainFromConfig(config);
    updateInheritedMainsFromConfig(config);
    updateExternalJars(config);

    try {
      String tableInfo =
          config.getAttribute(LaunchConfigurationConstants.ATTR_INPUT_TABLE_INFO, "").trim();
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
    } catch (Exception e) {

    }

    projectList.removeAll();

    ODPSProjectItem projectItem = null;
    try {
      String project = config.getAttribute(LaunchConfigurationConstants.ATTR_PROJECT, "");
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
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse
   * .debug.core.ILaunchConfiguration)
   */
  public boolean isValid(ILaunchConfiguration config) {
    setErrorMessage(null);
    setMessage(null);
    String name = fProjText.getText().trim();
    if (name.length() > 0) {
      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      IStatus status = workspace.validateName(name, IResource.PROJECT);
      if (status.isOK()) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        if (!project.exists()) {
          setErrorMessage(MessageFormat
              .format(LauncherMessages.JavaMainTab_20, new String[] {name}));
          return false;
        }
        if (!project.isOpen()) {
          setErrorMessage(MessageFormat
              .format(LauncherMessages.JavaMainTab_21, new String[] {name}));
          return false;
        }
      } else {
        setErrorMessage(MessageFormat.format(LauncherMessages.JavaMainTab_19,
            new String[] {status.getMessage()}));
        return false;
      }
    }
    name = fMainText.getText().trim();
    if (name.length() == 0) {
      setErrorMessage("UDF|UDTF class not specified");
      return false;
    }

    if (odpsProjectList.size() == 0 || projectList.getSelectionIndex() == -1) {
      setErrorMessage("ODPS Project not selected");
      return false;
    }

    if (txtTable.getText().trim().isEmpty()) {
      setErrorMessage("Input Table can't empty");
      return false;
    }

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug
   * .core.ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText()
        .trim());
    config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, fMainText.getText()
        .trim());
    mapResources(config);

    // config.setAttribute(ATTR_INCLUDE_EXTERNAL_JARS, true);
    config.setAttribute(ATTR_INCLUDE_EXTERNAL_JARS, (String) null);


    int index = projectList.getSelectionIndex();
    if (index != -1) {
      ODPSProjectItem projectItem = odpsProjectList.get(index);
      String project = ODPSProjectItemUtil.convert(projectItem);
      config.setAttribute(LaunchConfigurationConstants.ATTR_PROJECT, project);
    }

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

    config.setAttribute(LaunchConfigurationConstants.ATTR_INPUT_TABLE_INFO, tableInfo.toString());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug
   * .core.ILaunchConfigurationWorkingCopy)
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    IJavaElement javaElement = getContext();
    if (javaElement != null) {
      initializeJavaProject(javaElement, config);
    } else {
      config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
    }
    initializeMainTypeAndName(javaElement, config);
  }

  /**
   * updates the external jars attribute from the specified launch config
   * 
   * @param config the config to load from
   */
  private void updateExternalJars(ILaunchConfiguration config) {
    boolean search = false;
    try {
      search = config.getAttribute(ATTR_INCLUDE_EXTERNAL_JARS, false);
    } catch (CoreException e) {
      JDIDebugUIPlugin.log(e);
    }
  }

  /**
   * update the inherited mains attribute from the specified launch config
   * 
   * @param config the config to load from
   */
  private void updateInheritedMainsFromConfig(ILaunchConfiguration config) {
    boolean inherit = false;
    try {
      inherit = config.getAttribute(ATTR_CONSIDER_INHERITED_MAIN, false);
    } catch (CoreException e) {
      JDIDebugUIPlugin.log(e);
    }
    // fConsiderInheritedMainButton.setSelection(inherit);
  }

  /**
   * updates the stop in main attribute from the specified launch config
   * 
   * @param config the config to load the stop in main attribute from
   */
  private void updateStopInMainFromConfig(ILaunchConfiguration config) {
    boolean stop = false;
    try {
      stop = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
    } catch (CoreException e) {
      JDIDebugUIPlugin.log(e);
    }
    // fStopInMainCheckButton.setSelection(stop);
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
    } else if (event.getSource() == addProjectBtn) {
      ProjectEditorDialog projectOpDialog =
          new ProjectEditorDialog(getShell(), null, ProjectOp.INSERT);
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
          new ProjectEditorDialog(getShell(), odpsProjectList.get(index), ProjectOp.EDIT);
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

}
