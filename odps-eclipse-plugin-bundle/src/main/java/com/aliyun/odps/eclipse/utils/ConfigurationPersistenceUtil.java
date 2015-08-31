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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.List;

import com.aliyun.odps.eclipse.Activator;
import com.aliyun.odps.eclipse.template.ODPSProjectItem;

public class ConfigurationPersistenceUtil {

  public static String odpsConfigPath = Platform.getInstanceLocation().getURL().getFile()
      + ".odps_conf";
  public static String ODPS_PROJECT_SEPARATOR = "##ODPS PROJECT##";
  public static String END_SEPARATOR = "##END##";

  public static void loadOdpsConfig() {
    File odpsConfigFile = new File(odpsConfigPath);
    if (!odpsConfigFile.exists()) {
      return;
    }
    BufferedReader br = null;
    try {
      br = new BufferedReader(new InputStreamReader(new FileInputStream(odpsConfigFile)));
      String line = br.readLine();
      while (line != null && !line.trim().equals(ODPS_PROJECT_SEPARATOR)) {
        line = br.readLine();
      }

      if (line != null) {
        while ((line = br.readLine()) != null) {
          String[] ss = line.trim().split(",");
          if (line.trim().equals(END_SEPARATOR) || ss.length != 4) {
            break;
          }
          ODPSProjectItem projectItem = new ODPSProjectItem();
          projectItem.setProject(ss[0].trim());
          projectItem.setEndpoint(ss[1].trim());
          projectItem.setAccessId(ss[2].trim());
          projectItem.setAccessKey(ss[3].trim());
          Activator.getDefault().getOdpsProjectList().add(projectItem);
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void saveOdpsConfig() {

    StringBuffer sb = new StringBuffer();
    sb.append(ODPS_PROJECT_SEPARATOR);
    for (ODPSProjectItem projectItem : Activator.getDefault().getOdpsProjectList()) {
      sb.append("\n");
      sb.append(projectItem.getProject());
      sb.append(",");
      sb.append(projectItem.getEndpoint());
      sb.append(",");
      sb.append(projectItem.getAccessId());
      sb.append(",");
      sb.append(projectItem.getAccessKey());
    }
    sb.append("\n");
    sb.append(END_SEPARATOR);

    FileOutputStream fout = null;
    try {
      fout = new FileOutputStream(odpsConfigPath);
      fout.write(sb.toString().getBytes());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (fout != null) {
        try {
          fout.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

  }

  public static void addProject(ODPSProjectItem projectItem,
      java.util.List<ODPSProjectItem> odpsProjectList, List projectList) {
    if (projectItem.getProject().isEmpty()) {
      return;
    }
    // will not add Duplicate
    if (getIndex(projectItem) != -1) {
      return;
    }
    if (projectList != null) {
      projectList.add(projectItem.getProject(), 0);
      projectList.setSelection(0);
    }
    odpsProjectList.add(0, projectItem);
    saveOdpsConfig();
  }

  public static void editProject(ODPSProjectItem projectItem,
      java.util.List<ODPSProjectItem> odpsProjectList, List projectList) {
    int index = projectList.getSelectionIndex();
    if (index == -1) {
      return;
    }
    if (projectList != null) {
      projectList.setItem(index, projectItem.getProject());
    }
    odpsProjectList.set(index, projectItem);
    saveOdpsConfig();
  }

  public static void removeProject(java.util.List<ODPSProjectItem> odpsProjectList, List projectList) {
    int index = projectList.getSelectionIndex();
    if (index == -1) {
      return;
    }
    if (projectList != null) {
      odpsProjectList.remove(index);
    }
    projectList.remove(index);
    saveOdpsConfig();
  }

  public static java.util.List<ODPSProjectItem> getOdpsProjectList() {
    return Activator.getDefault().getOdpsProjectList();
  }

  public static int getIndex(ODPSProjectItem projectItem) {
    java.util.List<ODPSProjectItem> odpsProjectList = getOdpsProjectList();
    if (odpsProjectList == null) {
      return -1;
    }
    int index = -1;
    for (int i = 0; i < odpsProjectList.size(); i++) {
      ODPSProjectItem item = odpsProjectList.get(i);
      if (projectItem.getProject().equals(item.getProject())) {
        return i;
      }
    }

    return index;
  }

}
