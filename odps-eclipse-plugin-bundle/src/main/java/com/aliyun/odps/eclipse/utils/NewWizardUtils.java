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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ComboDialogField;
import org.osgi.framework.Bundle;

import com.aliyun.odps.eclipse.Activator;

public class NewWizardUtils {

  private final static String IO_PACKAGE = "com.aliyun.odps.io";
  private final static String RESOURCE = "resource";
  private final static String UDF_WAREHOUSE = "udf";

  public static String[] getOdpsWritableClassName() {
    return new String[] {IO_PACKAGE + ".BooleanWritable", IO_PACKAGE + ".BytesWritable",
        IO_PACKAGE + ".DatetimeWritable", IO_PACKAGE + ".DoubleWritable",
        IO_PACKAGE + ".IntWritable", IO_PACKAGE + ".LongWritable", IO_PACKAGE + ".NullWritable",
        IO_PACKAGE + ".Text", IO_PACKAGE + ".Tuple"};
  }

  public static String getSelectClassName(ComboDialogField keyCombo2) {
    String[] key = keyCombo2.getItems()[keyCombo2.getSelectionIndex()].split("\\.");
    return key[key.length - 1];
  }

  public static String constructTypeArgs(String s1, String s2) {
    return "<" + s1 + "," + s2 + ">";
  }

  public static void createClass(IJavaProject myjavapro, String className, String classPath,
      IProgressMonitor monitor, String content) throws JavaModelException {
    IPath mypath = myjavapro.getPath();
    mypath = mypath.append("src");
    mypath.append(classPath);
    IPackageFragmentRoot fragmentRoot = myjavapro.findPackageFragmentRoot(mypath);
    IPackageFragment fragment = fragmentRoot.createPackageFragment(classPath, true, monitor);
    fragment.createCompilationUnit(className + ".java", content, true, monitor);
  }

  public static String getContent(IProject myproject, String testName, String udfName,
      String udfClassPath, String classPath, String model) {
    try {
      Bundle bundle = Activator.getDefault().getBundle();
      IContainer container = (IContainer) myproject;
      final IFolder warehouseFolder = container.getFolder(new Path(RESOURCE));

      Enumeration<URL> urls = bundle.findEntries(warehouseFolder.getName(), model, true);

      URL url = urls.nextElement();
      BufferedReader br =
          new BufferedReader(new InputStreamReader(FileLocator.toFileURL(url).openStream()));

      StringBuilder contents = new StringBuilder();
      String buffer;
      while ((buffer = br.readLine()) != null) {
        contents.append(buffer);
        contents.append("\n");
      }
      br.close();
      String content = contents.toString();
      if (udfClassPath.equals("")) {
        content = content.replace("USERCLASSFULLPATH", udfName);
        content = content.replace("PACKAGEPATH", "");
      } else {
        content = content.replace("USERCLASSFULLPATH", udfClassPath + "." + udfName);
        content = content.replace("PACKAGEPATH", "package " + classPath + ";");
      }
      content = content.replace("USERUDTFNAME", udfName);
      return content.replace("TESTTNAME", testName);
    } catch (Exception localException) {
      throw new RuntimeException(localException);
    }
  }

  public static void createDataFile(IProject myproject, String shortName, String folder,
      IProgressMonitor monitor) throws CoreException {
    IFolder dataFolder = myproject.getFolder(UDF_WAREHOUSE);
    createDir(dataFolder, monitor);
    dataFolder = dataFolder.getFolder(folder);
    createDir(dataFolder, monitor);
    InputStream emptyStream = new ByteArrayInputStream("".getBytes());
    IFile inputFile = dataFolder.getFile(shortName + ".in");
    if (!inputFile.exists())
      inputFile.create(emptyStream, false, monitor);
    IFile outputFile = dataFolder.getFile(shortName + ".out");
    if (!outputFile.exists()) {
      outputFile.create(new ByteArrayInputStream("".getBytes()), false, monitor);
    }
  }

  public static void createDir(IFolder dataFolder, IProgressMonitor monitor) throws CoreException {
    if (!dataFolder.exists()) {
      dataFolder.create(false, true, monitor);
    }
  }

  public static void createTestBase(IJavaProject myjavapro, String classPath,
      IProgressMonitor monitor, String content, String testBaseName) throws JavaModelException {
    IPath mypath = myjavapro.getPath();
    mypath = mypath.append("src");
    mypath.append(classPath);

    IPackageFragmentRoot fragmentRoot = myjavapro.findPackageFragmentRoot(mypath);
    IPackageFragment fragment = fragmentRoot.createPackageFragment(classPath, true, monitor);
    IJavaElement[] iJavaElement = fragment.getChildren();
    boolean exit = false;
    for (int i = 0; i < iJavaElement.length; i++) {
      if (iJavaElement[i].getElementName().equals(testBaseName)) {
        exit = true;
        break;
      }
    }
    if (!exit) {
      fragment.createCompilationUnit(testBaseName, content, false, monitor);
    }
  }

}
