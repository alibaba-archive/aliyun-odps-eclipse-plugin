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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.wizard.Wizard;

import com.aliyun.odps.eclipse.constants.LaunchConfigurationConstants;

public class ShotcutConfigWizard extends Wizard {

  private ILaunchConfigurationWorkingCopy lanchConfig;
  private ShotcutConfigWizardPage firstWizardPage;

  public ShotcutConfigWizard(IFile resource, ILaunchConfigurationWorkingCopy iConfWC,
      String runClassName) {
    this.lanchConfig = iConfWC;
    setForcePreviousAndNextButtons(false);
    setNeedsProgressMonitor(false);
    setWindowTitle("ODPS MapReduce Run Configuration");
    this.firstWizardPage = new ShotcutConfigWizardPage(lanchConfig, runClassName);
  }

  @Override
  public void addPages() {
    addPage(firstWizardPage);
  }

  @Override
  public boolean performFinish() {

    lanchConfig.setAttribute(LaunchConfigurationConstants.ATTR_PROJECT,
        firstWizardPage.getOdpsProject());
    lanchConfig.setAttribute(LaunchConfigurationConstants.ATTR_RESOURCES,
        firstWizardPage.getOdpsMrResources());
    lanchConfig.setAttribute(LaunchConfigurationConstants.ATTR_RUN_MODE,
        firstWizardPage.isOdpsLocalRunMode());
    lanchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
        firstWizardPage.getProgramArgs());

    try {
      lanchConfig.doSave();
    } catch (CoreException e) {
    }

    return true;
  }

}
