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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.ControlAccessibleListener;
import org.eclipse.jdt.internal.debug.ui.launcher.AbstractJavaMainTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.MainMethodSearchEngine;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * Provides general widgets and methods for a Java type launch configuration 'Main' tab. This class
 * provides shared functionality for those main tabs which have a 'main type' field on them; such as
 * a main method for a local Java application, or an Applet for Java Applets
 * 
 * @since 3.2
 */
public abstract class SharedUDFClassTab extends AbstractJavaMainTab {

  protected Text fMainText;
  private Button fSearchButton;

  /**
   * Creates the widgets for specifying a main type.
   * 
   * @param parent the parent composite
   */
  protected void createMainTypeEditor(Composite parent, String text) {
    Group group = SWTFactory.createGroup(parent, text, 2, 1, GridData.FILL_HORIZONTAL);
    fMainText = SWTFactory.createSingleText(group, 1);
    fMainText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        updateLaunchConfigurationDialog();
      }
    });
    ControlAccessibleListener.addListener(fMainText, group.getText());
    fSearchButton = createPushButton(group, LauncherMessages.AbstractJavaMainTab_2, null);
    fSearchButton.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {}

      public void widgetSelected(SelectionEvent e) {
        handleSearchButtonSelected();
      }
    });
  }


  /**
   * The select button pressed handler
   */
  protected abstract void handleSearchButtonSelected();

  /**
   * Set the main type & name attributes on the working copy based on the IJavaElement
   */
  protected void initializeMainTypeAndName(IJavaElement javaElement,
      ILaunchConfigurationWorkingCopy config) {
    String name = null;
    if (javaElement instanceof IMember) {
      IMember member = (IMember) javaElement;
      if (member.isBinary()) {
        javaElement = member.getClassFile();
      } else {
        javaElement = member.getCompilationUnit();
      }
    }
    if (javaElement instanceof ICompilationUnit || javaElement instanceof IClassFile) {
      try {
        IJavaSearchScope scope =
            SearchEngine.createJavaSearchScope(new IJavaElement[] {javaElement}, false);
        MainMethodSearchEngine engine = new MainMethodSearchEngine();
        IType[] types = engine.searchMainMethods(getLaunchConfigurationDialog(), scope, false);
        if (types != null && (types.length > 0)) {
          // Simply grab the first main type found in the searched element
          name = types[0].getFullyQualifiedName();
        }
      } catch (InterruptedException ie) {
        JDIDebugUIPlugin.log(ie);
      } catch (InvocationTargetException ite) {
        JDIDebugUIPlugin.log(ite);
      }
    }
    if (name == null) {
      name = EMPTY_STRING;
    }
    config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, name);
    if (name.length() > 0) {
      int index = name.lastIndexOf('.');
      if (index > 0) {
        name = name.substring(index + 1);
      }
      name = getLaunchConfigurationDialog().generateName(name);
      config.rename(name);
    }
  }

  /**
   * Loads the main type from the launch configuration's preference store
   * 
   * @param config the config to load the main type from
   */
  protected void updateMainTypeFromConfig(ILaunchConfiguration config) {
    String mainTypeName = EMPTY_STRING;
    try {
      mainTypeName =
          config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, EMPTY_STRING);
    } catch (CoreException ce) {
      JDIDebugUIPlugin.log(ce);
    }
    fMainText.setText(mainTypeName);
  }
}
