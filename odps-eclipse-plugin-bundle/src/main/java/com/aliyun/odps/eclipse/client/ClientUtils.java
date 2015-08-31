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

package com.aliyun.odps.eclipse.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.aliyun.odps.eclipse.Activator;
import com.aliyun.odps.eclipse.constants.CONST;

public class ClientUtils {

  public static final String ODPS_CLIENT_HOME = "odps_client_home";

  public static String getClientPath() {
    String path = null;
    try {
      path = getODPSSetting(ODPS_CLIENT_HOME);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return path;
  }

  public static void setClientPath(String path) {
    try {
      setODPSSetting(ODPS_CLIENT_HOME, path);
    } catch (IOException e) {
      System.err.println("Exception occured when save odps config file");
    }

  }

  private static String getODPSSetting(String key) throws IOException {

    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    IPath workSpacePath = workspaceRoot.getLocation();
    File conf = new File(workSpacePath.toString() + Path.SEPARATOR + "odps_client_conf");
    String ret = null;
    if (!conf.exists()) {
      // copy default
      try {
        InputStream stream =
            FileLocator.openStream(Activator.getDefault().getBundle(), new Path(CONST.RES_PATH
                + Path.SEPARATOR + "odps_client_conf"), false);
        // TODO FileUtils.copyInputStreamToFile(stream,
        // new File(workSpacePath.toString() + Path.SEPARATOR
        // + "odps_client_conf"));
      } catch (IOException e1) {
        throw new IOException("Exception occured when copy odps config file");
      }
    }

    InputStream in;
    try {
      in = new BufferedInputStream(new FileInputStream(conf));
      ret = loadSetting(in, key);
    } catch (FileNotFoundException e) {
      throw new IOException("Odps config file not found");
    } catch (IOException e) {
      throw new IOException("Exception occured when load odps config file");
    }
    return ret;
  }

  private static void setODPSSetting(String key, String value) throws IOException {
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    IPath workSpacePath = workspaceRoot.getLocation();
    File conf = new File(workSpacePath.toString() + Path.SEPARATOR + "odps_client_conf");
    if (!conf.exists()) {
      // copy default
      try {
        InputStream stream =
            FileLocator.openStream(Activator.getDefault().getBundle(), new Path(CONST.RES_PATH
                + Path.SEPARATOR + "odps_client_conf"), false);
        // TODO FileUtils.copyInputStreamToFile(stream,
        // new File(workSpacePath.toString() + Path.SEPARATOR
        // + "odps_client_conf"));
      } catch (IOException e1) {
        throw new IOException("Exception occured when copy odps config file");
      }
    }

    OutputStream out;
    InputStream in;
    Properties props;
    props = new Properties();
    try {
      in = new BufferedInputStream(new FileInputStream(conf));
      props.load(in);
      props.setProperty(key, value);
      in.close();
      out = new BufferedOutputStream(new FileOutputStream(conf));
      props.store(out, "odps client home");
      out.close();
    } catch (FileNotFoundException e) {
      throw new IOException("Odps config file not found");
    } catch (IOException e) {
      throw new IOException("Exception occured when load odps config file");
    }
  }

  private static String loadSetting(InputStream in, String key) throws IOException {
    Properties props;
    props = new Properties();
    props.load(in);
    return props.getProperty(key);
  }

}
