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

import java.io.File;
import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import com.aliyun.odps.eclipse.constants.LaunchConfigurationConstants;
import com.aliyun.odps.eclipse.template.ODPSProjectItem;
import com.aliyun.odps.eclipse.utils.ODPSProjectItemUtil;

public class UDFLaunchConfigurationDelegate extends JavaLaunchDelegate {
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse
   * .debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch,
      IProgressMonitor monitor) throws CoreException {

    if (monitor == null) {
      monitor = new NullProgressMonitor();
    }

    monitor.beginTask(MessageFormat.format("{0}...", new String[] {configuration.getName()}), 3); //$NON-NLS-1$
    // check for cancellation
    if (monitor.isCanceled()) {
      return;
    }
    try {
      monitor
          .subTask(LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_Verifying_launch_attributes____1);

      String mainTypeName = verifyMainTypeName(configuration);
      IVMRunner runner = getVMRunner(configuration, mode);

      File workingDir = verifyWorkingDirectory(configuration);
      String workingDirName = null;
      if (workingDir != null) {
        workingDirName = workingDir.getAbsolutePath();
      }

      // Environment variables
      String[] envp = getEnvironment(configuration);

      // Program & VM arguments
      String pgmArgs =
          getProgramArguments(configuration) + getOdpsProgramArguments(configuration, mainTypeName);

      String vmArgs = getVMArguments(configuration);

      mainTypeName = "com.aliyun.odps.udf.local.Main";

      ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);

      // VM-specific attributes
      Map vmAttributesMap = getVMSpecificAttributesMap(configuration);

      // Classpath
      String[] classpath = getClasspath(configuration);

      // Create VM config
      VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, classpath);
      runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
      runConfig.setEnvironment(envp);
      runConfig.setVMArguments(execArgs.getVMArgumentsArray());
      runConfig.setWorkingDirectory(workingDirName);
      runConfig.setVMSpecificAttributesMap(vmAttributesMap);

      // Bootpath
      runConfig.setBootClassPath(getBootpath(configuration));

      // check for cancellation
      if (monitor.isCanceled()) {
        return;
      }

      // stop in main
      prepareStopInMain(configuration);

      // done the verification phase
      monitor.worked(1);

      monitor
          .subTask(LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_Creating_source_locator____2);
      // set the default source locator if required
      setDefaultSourceLocator(launch, configuration);
      monitor.worked(1);

      // Launch the configuration - 1 unit of work
      runner.run(runConfig, launch, monitor);

      // check for cancellation
      if (monitor.isCanceled()) {
        return;
      }
    } finally {
      monitor.done();
    }
  }

  public String getOdpsProgramArguments(ILaunchConfiguration configuration, String udfClassName)
      throws CoreException {

    StringBuffer arguments = new StringBuffer();

    arguments.append(" -c ");
    arguments.append(udfClassName);

    String tableInfo =
        configuration.getAttribute(LaunchConfigurationConstants.ATTR_INPUT_TABLE_INFO, "");
    arguments.append(" -i ");
    arguments.append(tableInfo);

    String project = configuration.getAttribute(LaunchConfigurationConstants.ATTR_PROJECT, "");
    if (project.isEmpty()) {
      return null;
    }
    ODPSProjectItem projectItem = ODPSProjectItemUtil.convert(project);

    if (projectItem == null) {
      return null;
    }

    arguments.append(" -p ");
    arguments.append(projectItem.getProject());
    arguments.append(" --endpoint ");
    arguments.append(projectItem.getEndpoint());
    arguments.append(" --access-id ");
    arguments.append(projectItem.getAccessId());
    arguments.append(" --access-key ");
    arguments.append(projectItem.getAccessKey());
    arguments.append(" ");

    return arguments.toString();
  }
}
