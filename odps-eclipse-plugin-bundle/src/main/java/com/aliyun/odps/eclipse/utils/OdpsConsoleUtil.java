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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.runtime.Path;

import com.aliyun.odps.eclipse.template.ODPSProjectItem;

public class OdpsConsoleUtil {
  public static boolean validateODPSConoleLocation(String consoleBaseDir) {
    if (consoleBaseDir == null || consoleBaseDir.isEmpty()) {
      return false;
    }
    FilenameFilter gotHadoopJar = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return (name.startsWith("odps-sdk-core") || name.startsWith("odps-mapred-local"))
            && name.endsWith(".jar");
      }
    };
    return new Path(consoleBaseDir).toFile().exists()
        && new Path(consoleBaseDir + Path.SEPARATOR + "lib").toFile().exists()
        && (new Path(consoleBaseDir + Path.SEPARATOR + "lib").toFile().list(gotHadoopJar).length > 0);

  }

  public static java.util.List<ODPSProjectItem> listOdpsProjectFromConsole(String consoleBaseDir) {
    if (consoleBaseDir == null || consoleBaseDir.isEmpty()) {
      return null;
    }
    return listOdpsProject(consoleBaseDir + Path.SEPARATOR + "conf");

  }

  public static java.util.List<ODPSProjectItem> listOdpsProject(String dir) {
    File confDir = new File(dir);
    if (!confDir.exists()) {
      return null;
    }

    String[] confFiles = confDir.list();
    if (confFiles == null || confFiles.length == 0) {
      return null;
    }

    java.util.List<ODPSProjectItem> result = new java.util.LinkedList<ODPSProjectItem>();
    for (String file : confFiles) {
      ODPSProjectItem item = loadOdpsProject(dir + Path.SEPARATOR + file);
      if (item != null) {
        result.add(item);
      }
    }
    return result;
  }

  public static ODPSProjectItem loadOdpsProject(String confPath) {
    if (confPath == null || confPath.isEmpty()) {
      return null;
    }
    File file = new File(confPath);
    if (!file.exists()) {
      return null;
    }

    ODPSProjectItem projectItem = null;
    Properties props = new Properties();
    InputStream in = null;
    try {
      in = new FileInputStream(file);
      props.load(in);

      String projectName = props.getProperty("project_name");
      if (isEmpty(projectName)) {
        // Old Version
        projectName = props.getProperty("default_project");
        if (isEmpty(projectName)) {
          projectName = props.getProperty("default.project");
        }
      }

      String endpoint = props.getProperty("end_point");
      if (isEmpty(endpoint)) {
        // Old Version
        endpoint = props.getProperty("endpoint");
      }

      String accessId = props.getProperty("access_id");
      if (isEmpty(accessId)) {
        // Old Version
        accessId = props.getProperty("access.id");
      }

      String accessKey = props.getProperty("access_key");
      if (isEmpty(accessKey)) {
        // Old Version
        accessKey = props.getProperty("access.key");
      }

      if (!isEmpty(projectName) && !isEmpty(endpoint) && !isEmpty(accessId) && !isEmpty(accessId)) {
        projectItem = new ODPSProjectItem(projectName, endpoint, accessId, accessKey);

      }
    } catch (FileNotFoundException e) {
    } catch (IOException e) {
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
        }
      }
    }

    return projectItem;

  }

  private static boolean isEmpty(String str) {
    return str == null || str.isEmpty();
  }

}
