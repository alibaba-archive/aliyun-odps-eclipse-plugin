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

package com.aliyun.odps.eclipse.utils;

import com.aliyun.odps.eclipse.template.ODPSProjectItem;

public class ODPSProjectItemUtil {
  public static ODPSProjectItem convert(String project) {
    String[] ss = project.split(",");
    if (ss.length != 4) {
      return null;
    }

    ODPSProjectItem projectItem = new ODPSProjectItem();
    projectItem.setProject(ss[0]);
    projectItem.setEndpoint(ss[1]);
    projectItem.setAccessId(ss[2]);
    projectItem.setAccessKey(ss[3]);
    return projectItem;
  }

  public static String convert(ODPSProjectItem projectItem) {
    if (projectItem == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(projectItem.getProject());
    sb.append(",");
    sb.append(projectItem.getEndpoint());
    sb.append(",");
    sb.append(projectItem.getAccessId());
    sb.append(",");
    sb.append(projectItem.getAccessKey());
    return sb.toString();
  }

}
