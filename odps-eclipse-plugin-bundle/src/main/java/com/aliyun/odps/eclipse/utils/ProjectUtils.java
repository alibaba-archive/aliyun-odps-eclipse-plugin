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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import com.aliyun.odps.eclipse.Activator;

public class ProjectUtils {

  public static String WARE_HOUSE = "warehouse";
  public static String TEMP_DIR = "temp";
  public static String EXAMPLE_CLASS = "examples";

  public static void addODPSNature(IProject project, IProgressMonitor monitor,
      String currentConsolePath) throws CoreException, IOException {
    IContainer container = (IContainer) project;
    final IFolder warehouseFolder = container.getFolder(new Path(WARE_HOUSE));
    final IFolder mrLocalJobFolder = container.getFolder(new Path(TEMP_DIR));

    final IFolder exampleFolder = container.getFolder(new Path(EXAMPLE_CLASS));

    String consoleVersion = VersionCompatibilityUtil.getConsoleVersion(currentConsolePath);

    warehouseFolder.create(true, true, monitor);
    mrLocalJobFolder.create(true, true, monitor);
    exampleFolder.create(true, true, monitor);

    Bundle bundle = Activator.getDefault().getBundle();

    Enumeration<URL> urls = bundle.findEntries(warehouseFolder.getName(), "*", true);
    while (urls.hasMoreElements()) {
      URL url = urls.nextElement();
      InputStream in;
      try {
        in = url.openStream();
        if (in.read() == -1) {
          // Compatible with win, if win,in(InputStream) exist.
          continue;
        }
      } catch (FileNotFoundException e) {
        // this exception get thrown for file system directories
        // container.getFolder(new Path(url.getPath()))
        // .create(true, true, monitor);
        continue;
      }
      addFileToProject(container, url.getPath(), url.openStream(), monitor, consoleVersion);
      in.close();
    }

    urls = bundle.findEntries(exampleFolder.getName(), "*", true);
    while (urls.hasMoreElements()) {
      URL url = urls.nextElement();
      InputStream in;
      try {
        in = url.openStream();
        if (in.read() == -1) {
          // Compatible with win, if win,in(InputStream) exist.
          continue;
        }
      } catch (FileNotFoundException e) {
        // this exception get thrown for file system directories
        // container.getFolder(new Path(url.getPath()))
        // .create(true, true, monitor);
        continue;
      }
      addFileToProject(container, url.getPath(), url.openStream(), monitor, consoleVersion);
      in.close();
    }
  }

  /**
   * Adds a new file to the project.
   * 
   * @param container
   * @param path
   * @param contentStream
   * @param monitor
   * @throws CoreException
   * @throws IOException
   */
  private static void addFileToProject(IContainer container, String path,
      InputStream contentStream, IProgressMonitor monitor, String consoleVersion)
      throws CoreException, IOException {

    if (consoleVersion != null && consoleVersion.indexOf("0.13.") != -1) {
      path = path.replaceFirst("__tables__", "");
    }

    IProject prj = (IProject) container;
    final IFile file = container.getFile(new Path(path));

    createDir(prj, file.getParent(), monitor);

    if (file.exists()) {
      file.setContents(contentStream, true, true, monitor);
    } else {
      file.create(contentStream, true, monitor);
    }
  }

  private static void createDir(IProject prj, IContainer dir, IProgressMonitor monitor)
      throws CoreException {

    if (!dir.getParent().exists()) {
      createDir(prj, dir.getParent(), monitor);
    }
    IFolder f = (IFolder) dir;
    if (!f.exists()) {
      f.create(true, true, monitor);
    }
  }

}
