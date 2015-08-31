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

import com.aliyun.odps.eclipse.Activator;
import com.aliyun.odps.eclipse.constants.PreferenceConstants;

public class LunchConfigurationProvider {

  public static String getDefaultProgramArguments() {
    return "";
  }

  public static String getDefaultVMArguments() {

    String endPoint =
        Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_END_POINT);
    String id =
        Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_ACCESS_ID);
    String key =
        Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_ACCESS_KEY);
    String projectName =
        Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_PRJECT_NAME);
    String mode =
        Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_RUN_MODE);
    return "-Dodps.mapred.end.point=" + endPoint + " -Dodps.mapred.access.id=" + id
        + " -Dodps.mapred.access.key=" + key + " -Dodps.mapred.proxy.host="
        + " -Dodps.mapred.proxy.port=0" + " -Dodps.mapred.project.name=" + projectName
        + " -Dodps.mapred.runner.mode=" + mode + " -Dodps.mapred.cache.resources=";
  }
}
