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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.MainMethodSearchEngine;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;

import com.aliyun.odps.eclipse.constants.LaunchConfigurationConstants;
import com.aliyun.odps.eclipse.launch.configuration.udf.UDFSearchEngine;

/**
 * Launch shortcut for local Java applications.
 * <p>
 * This class may be instantiated or subclassed.
 * </p>
 * 
 * @since 3.3
 */
public class UDFLaunchShortcuts2 extends UDFLaunchShortcuts1 {

  /**
   * Returns the Java elements corresponding to the given objects. Members are translated to
   * corresponding declaring types where possible.
   * 
   * @param objects selected objects
   * @return corresponding Java elements
   * @since 3.5
   */
  protected IJavaElement[] getJavaElements(Object[] objects) {
    List list = new ArrayList(objects.length);
    for (int i = 0; i < objects.length; i++) {
      Object object = objects[i];
      if (object instanceof IAdaptable) {
        IJavaElement element = (IJavaElement) ((IAdaptable) object).getAdapter(IJavaElement.class);
        if (element != null) {
          if (element instanceof IMember) {
            // Use the declaring type if available
            IJavaElement type = ((IMember) element).getDeclaringType();
            if (type != null) {
              element = type;
            }
          }
          list.add(element);
        }
      }
    }
    return (IJavaElement[]) list.toArray(new IJavaElement[list.size()]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#
   * createConfiguration(org.eclipse.jdt.core.IType)
   */
  protected ILaunchConfiguration createConfiguration(IType type) {
    ILaunchConfiguration config = null;
    ILaunchConfigurationWorkingCopy wc = null;
    try {
      ILaunchConfigurationType configType = getConfigurationType();
      wc =
          configType.newInstance(null,
              getLaunchManager().generateLaunchConfigurationName(type.getTypeQualifiedName('.')));
      wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
          type.getFullyQualifiedName());
      wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, type.getJavaProject()
          .getElementName());
      wc.setMappedResources(new IResource[] {type.getUnderlyingResource()});
      config = wc.doSave();
    } catch (CoreException exception) {
      MessageDialog.openError(JDIDebugUIPlugin.getActiveWorkbenchShell(),
          LauncherMessages.JavaLaunchShortcut_3, exception.getStatus().getMessage());
    }
    return config;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut# getConfigurationType()
   */
  protected ILaunchConfigurationType getConfigurationType() {
    return getLaunchManager().getLaunchConfigurationType(
        LaunchConfigurationConstants.ID_LAUNCH_CONFIGURATION_ODPS_UDF);
  }

  /**
   * Returns the singleton launch manager.
   * 
   * @return launch manager
   */
  private ILaunchManager getLaunchManager() {
    return DebugPlugin.getDefault().getLaunchManager();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#findTypes
   * (java.lang.Object[], org.eclipse.jface.operation.IRunnableContext)
   */
  protected IType[] findTypes(Object[] elements, IRunnableContext context)
      throws InterruptedException, CoreException {
    try {
      if (elements.length == 1) {
        IType type = isUDF(elements[0]);
        if (type != null) {
          return new IType[] {type};
        }
      }
      IJavaElement[] javaElements = getJavaElements(elements);
      UDFSearchEngine engine = new UDFSearchEngine();
      int constraints = IJavaSearchScope.SOURCES;
      constraints |= IJavaSearchScope.APPLICATION_LIBRARIES;
      IJavaSearchScope scope = SearchEngine.createJavaSearchScope(javaElements, constraints);
      return engine.searchUDFClass(context, scope, true);
    } catch (InvocationTargetException e) {
      throw (CoreException) e.getTargetException();
    }
  }

  /**
   * Returns the smallest enclosing <code>IType</code> if the specified object is a main method, or
   * <code>null</code>
   * 
   * @param o the object to inspect
   * @return the smallest enclosing <code>IType</code> of the specified object if it is a main
   *         method or <code>null</code> if it is not
   */
  private IType isUDF(Object o) {
    if (o instanceof IAdaptable) {
      IAdaptable adapt = (IAdaptable) o;
      IJavaElement element = (IJavaElement) adapt.getAdapter(IJavaElement.class);
      if (element instanceof CompilationUnit) {
        CompilationUnit unit = (CompilationUnit) element;
        IClassFile i;
        try {
          IType[] types = unit.getTypes();
          for (IType iType : types) {
            String superClassName = iType.getSuperclassName();
            if (superClassName.equals("UDF") || superClassName.equals("UDTF")
                || superClassName.equals("Aggregator")) {
              return iType;
            }

          }
        } catch (JavaModelException e) {
        }
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut# getTypeSelectionTitle()
   */
  protected String getTypeSelectionTitle() {
    return LauncherMessages.JavaApplicationLaunchShortcut_0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut# getEditorEmptyMessage()
   */
  protected String getEditorEmptyMessage() {
    return LauncherMessages.JavaApplicationLaunchShortcut_1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#
   * getSelectionEmptyMessage()
   */
  protected String getSelectionEmptyMessage() {
    return LauncherMessages.JavaApplicationLaunchShortcut_2;
  }
}
