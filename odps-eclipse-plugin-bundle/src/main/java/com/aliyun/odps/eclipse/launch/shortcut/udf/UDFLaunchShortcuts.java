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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import com.aliyun.odps.eclipse.constants.LaunchConfigurationConstants;
import com.aliyun.odps.eclipse.constants.OdpsEclipseConstants;

public class UDFLaunchShortcuts extends UDFLaunchShortcuts2 {

  @Override
  protected void launch(IType type, String mode) {
    ILaunchConfiguration config = findLaunchConfiguration(type, getConfigurationType());
    if (config != null) {
      DebugUITools.launch(config, mode);
    }
  }

  /* @inheritDoc */
  @Override
  protected ILaunchConfiguration findLaunchConfiguration(IType type,
      ILaunchConfigurationType configType) {

    // Find an existing or create a launch configuration (Standard way)
    ILaunchConfiguration iConf = super.findLaunchConfiguration(type, configType);

    ILaunchConfigurationWorkingCopy iConfWC;
    try {
      /*
       * Tune the default launch configuration: setup run-time classpath manually
       */
      if (iConf == null) {
        iConf = createConfiguration(type);
      }

      iConfWC = iConf.getWorkingCopy();

    } catch (CoreException e) {
      e.printStackTrace();
      // FIXME Error dialog
      return null;
    }

    // Configure ODPS launch setting
    IResource resource = type.getResource();
    if (!(resource instanceof IFile)) {
      return null;
    }
    UDFShotcutConfigWizard wizard =
        new UDFShotcutConfigWizard((IFile) resource, iConfWC, type.getFullyQualifiedName());
    WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
    dialog.create();
    dialog.setBlockOnOpen(true);
    if (dialog.open() != WizardDialog.OK) {
      return null;
    }

    try {
      iConfWC.doSave();
    } catch (CoreException e) {
      e.printStackTrace();
      // FIXME Error dialog
      return null;
    }
    return iConf;
  }

  protected ILaunchConfigurationType getConfigurationType() {
    return DebugPlugin.getDefault().getLaunchManager()
        .getLaunchConfigurationType(LaunchConfigurationConstants.ID_LAUNCH_CONFIGURATION_ODPS_UDF);
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
      // ILaunchConfigurationType configType =
      // DebugPlugin.getDefault().getLaunchManager()
      // .getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);

      ILaunchConfigurationType configType =
          DebugPlugin
              .getDefault()
              .getLaunchManager()
              .getLaunchConfigurationType(
                  LaunchConfigurationConstants.ID_LAUNCH_CONFIGURATION_ODPS_UDF);
      wc =
          configType.newInstance(null, DebugPlugin.getDefault().getLaunchManager()
              .generateLaunchConfigurationName(type.getTypeQualifiedName('.')));
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
}
