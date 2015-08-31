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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

public class ODPSLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup implements
    IJavaLaunchConfigurationConstants {
  private ConfigurationTab odpsConfigurationTab;

  @Override
  public void initializeFrom(ILaunchConfiguration configuration) {
    super.initializeFrom(configuration);
  }

  @Override
  public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
    odpsConfigurationTab = new ConfigurationTab();
    ILaunchConfigurationTab[] tabs =
        new ILaunchConfigurationTab[] {new JavaMainTab(), odpsConfigurationTab,
            new JavaArgumentsTab(), new JavaJRETab(), new JavaClasspathTab(),
            // new SourceLookupTab(),
            new EnvironmentTab(), new CommonTab()};
    setTabs(tabs);
  }

  /**
   * @see ILaunchConfigurationTabGroup#setDefaults(ILaunchConfigurationWorkingCopy)
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    super.setDefaults(config);
    // config.setAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE,
    // IDebugUIConstants.PERSPECTIVE_NONE);
    // config.setAttribute(ATTR_PROGRAM_ARGUMENTS,
    // LunchConfigurationProvider.getDefaultProgramArguments());
    // config.setAttribute(ATTR_VM_ARGUMENTS,
    // LunchConfigurationProvider.getDefaultVMArguments());
  }
}
