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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;

/**
 * 处理warehouse目录兼容性问题的工具类 主要是odps-mapred-local-xxx.jar 0.13sdk 和0.14sdk warehouse目录结构不一致导致的
 * 
 */
public class VersionCompatibilityUtil {
  public final static String expectedSdkVersion = "0.15.0";

  /**
   * 
   * get Console SDK version
   */
  public static String getConsoleVersion(String consoleBaseDir) {
    if (!OdpsConsoleUtil.validateODPSConoleLocation(consoleBaseDir)) {
      return null;
    }

    File libDir = new Path(consoleBaseDir + Path.SEPARATOR + "lib").toFile();

    String[] files = libDir.list(new FilenameFilter() {

      @Override
      public boolean accept(File dir, String name) {
        if (name.startsWith("odps-sdk-core") && name.endsWith(".jar")) {
          return true;
        } else {
          return false;
        }
      }
    });

    String version = null;
    if (files != null && files.length > 0) {
      String jarFilePath = libDir.getAbsolutePath() + File.separator + files[0];
      version = getJarManifestAttr(jarFilePath, "Implementation-Version");
    }

    return version;

  }

  /**
   * 
   * get attribute of manifest.mf from jar file
   */
  public static String getJarManifestAttr(String jarFilePath, String key) {
    BufferedReader br = null;
    String version = null;
    try {
      JarFile jarFile = new JarFile(jarFilePath);
      JarEntry entry = jarFile.getJarEntry("META-INF/MANIFEST.MF");
      InputStream in = jarFile.getInputStream(entry);
      br = new BufferedReader(new InputStreamReader(in));
      String line = null;
      while ((line = br.readLine()) != null) {
        if (line.startsWith(key)) {
          version = line.substring(line.indexOf(":") + 1).trim();
          break;
        }
      }
    } catch (Exception e) {
    } finally {
      try {
        br.close();
      } catch (IOException e) {
      }
    }
    return version;
  }

  /**
   * check if the warehouse directory is compatible, if Ok return null,else give suggestion
   */
  public static String validVersion(String javaProjectDir, String odpsProject, String[] classPaths) {
    if (classPaths == null || classPaths.length == 0) {
      return null;
    }
    String jarFilePath = null;
    for (String path : classPaths) {
      if (path.indexOf("odps-sdk-core") != -1 && path.endsWith(".jar")) {
        jarFilePath = path;
        break;
      }
    }
    if (jarFilePath == null) {
      return null;
    }

    boolean flag = true;
    String warnMsg = null;
    String version = getJarManifestAttr(jarFilePath, "Implementation-Version");
    if (version != null && version.indexOf("0.13.") != -1) {
      flag = VersionCompatibilityUtil.isVersionClean13(javaProjectDir, odpsProject);
      if (!flag) {
        warnMsg =
            "Detected directory warehouse/" + odpsProject + "/__tables__ has sub-directories,"
                + "\n We suggest to move these sub-directories to folder warehouse/" + odpsProject;
      }
    } else {
      flag = VersionCompatibilityUtil.isVersionClean14(javaProjectDir, odpsProject);
      if (!flag) {
        warnMsg =
            "Detected directory /warehouse/" + odpsProject + "contains some table directories,"
                + "\n We suggest to move these  table directories to folder warehouse/"
                + odpsProject + "/__tables__";
      }
    }

    return warnMsg;
  }

  /**
   * If user use 0.13 SDK, check if the warehouse directory is compatible
   * 
   */
  public static boolean isVersionClean13(String javaProjectDir, String odpsProject) {
    if (javaProjectDir == null || javaProjectDir.isEmpty() || odpsProject == null
        || odpsProject.isEmpty()) {
      return true;
    }
    File warehouseDir = new File(javaProjectDir + File.separator + "warehouse");
    if (!warehouseDir.exists()) {
      return true;
    }
    File odpsProjectDir = new File(warehouseDir, odpsProject);
    if (!odpsProjectDir.exists()) {
      return true;
    }
    File tablesDir = new File(odpsProjectDir, "__tables__");
    if (!tablesDir.exists() || tablesDir.isFile()) {
      return true;
    }
    return tablesDir.list().length > 0 ? false : true;
  }

  /**
   * If user use 0.14 SDK, check if the warehouse directory is compatible
   * 
   */
  public static boolean isVersionClean14(String javaProjectDir, String odpsProject) {
    if (javaProjectDir == null || javaProjectDir.isEmpty() || odpsProject == null
        || odpsProject.isEmpty()) {
      return true;
    }
    File warehouseDir = new File(javaProjectDir + File.separator + "warehouse");
    if (!warehouseDir.exists()) {
      return true;
    }
    File odpsProjectDir = new File(warehouseDir, odpsProject);
    if (!odpsProjectDir.exists()) {
      return true;
    }

    String[] files = odpsProjectDir.list();
    for (String file : files) {
      File f = new File(odpsProjectDir, file);
      if (f.isDirectory() && !file.equals("__tables__") && !file.equals("__resources__")) {
        return false;
      }
    }

    return true;
  }

  public static String getExpectedSdkVersion() {
    return expectedSdkVersion;
  }

  public static int compareSdkVersionToEclipseVersion(String consoleBaseDir) {
    try {
      String sdkVersion = getConsoleVersion(consoleBaseDir);
      String[] sdkVersions = sdkVersion.split("\\.");
      String[] eclipseVersions = expectedSdkVersion.split("\\.");
      for (int i = 0; i < eclipseVersions.length; ++i) {
        int e = Integer.parseInt(eclipseVersions[i]);
        int s = Integer.parseInt(sdkVersions[i]);
        if (e == s) {
          continue;
        } else {
          return s - e > 0 ? 1 : -1;
        }
      }
    } catch (Exception e) {
      JDIDebugUIPlugin.log(e);
    }
    return 0;
  }

}
