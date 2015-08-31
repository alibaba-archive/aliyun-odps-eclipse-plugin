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

public class VmArgsConstant {
  public static final String VM_END_POINT = "-Dodps.end.point=";
  public static final String VM_ACCESS_ID = " -Dodps.access.id=";
  public static final String VM_ACCESS_KEY = " -Dodps.access.key=";
  public static final String VM_PROXY_HOST = " -Dodps.proxy.hos=";
  public static final String VM_PROXY_PORT = " -Dodps.proxy.port=";
  public static final String VM_PROJECT = " -Dodps.project.name=";
  public static final String VM_MODE = " -Dodps.runner.mode=";
  public static final String VM_RESOURCE = " -Dodps.cache.resources=";
  public final static String VM_LOCAL_TEMP_DIR = " -Dodps.mapred.local.temp.dir=";
  public final static String VM_LOCAL_TEMP_RETAIN = " -Dodps.mapred.local.temp.retain=";

  public final static String VM_DOWNLOAD_RECORD_LIMIT =
      " -Dodps.mapred.local.record.download.limit=";

}
