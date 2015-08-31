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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.wizard.Wizard;

import com.aliyun.odps.eclipse.constants.LaunchConfigurationConstants;
import com.aliyun.odps.eclipse.constants.OdpsEclipseConstants;

public class UDFShotcutConfigWizard extends Wizard {

  private ILaunchConfigurationWorkingCopy lanchConfig;
  private UDFShotcutConfigWizardPage firstWizardPage;

  public UDFShotcutConfigWizard(IFile resource, ILaunchConfigurationWorkingCopy iConfWC,
      String runClassName) {
    this.lanchConfig = iConfWC;
    setForcePreviousAndNextButtons(false);
    setNeedsProgressMonitor(false);
    setWindowTitle("ODPS UDF|UDTF|UDAF Run Configuration");
    this.firstWizardPage = new UDFShotcutConfigWizardPage(lanchConfig, runClassName);
  }

  @Override
  public void addPages() {
    addPage(firstWizardPage);
  }

  @Override
  public boolean performFinish() {

    lanchConfig.setAttribute(LaunchConfigurationConstants.ATTR_PROJECT,
        firstWizardPage.getOdpsProject());
    lanchConfig.setAttribute(LaunchConfigurationConstants.ATTR_INPUT_TABLE_INFO,
        firstWizardPage.getInputTableInfo());

    try {
      lanchConfig.doSave();
    } catch (CoreException e) {
    }

    return true;
  }

}
