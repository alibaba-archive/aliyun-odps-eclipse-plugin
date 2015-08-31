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

package com.aliyun.odps.eclipse.constants;

public class OdpsEclipseConstants {
  public static final String CONSOLE_PATH = getUniqueIdentifier() + "ODPS_CONSOLE_PATH";

  public static final String END_POINT = getUniqueIdentifier() + "ODPS_END_POINT";

  public static final String ACCESS_ID = getUniqueIdentifier() + "ODPS_ACCESS_ID";

  public static final String ACCESS_KEY = getUniqueIdentifier() + "ODPS_ACCESS_KEY";

  public static final String PRJECT_NAME = getUniqueIdentifier() + "ODPS_PRJECT_NAME";

  public static final String RESOURCES = getUniqueIdentifier() + "ODPS_PRJECT_NAME";

  public static final String LOCAL = getUniqueIdentifier() + "ODPS_LOCAL";

  public static final String RETAIN_TEMP = getUniqueIdentifier() + "ODPS_RETAIN_TEMP";

  public static final String TEMP_DIR = getUniqueIdentifier() + "ODPS_TEMP_DIR";

  public static final String LAUNCH_CONFIGURATION_TYPE =
      "com.aliyun.odps.eclipse.launchConfigurationODPS";

  public static String getUniqueIdentifier() {
    return "com.aliyun.odps.eclipse.";
  }

}
