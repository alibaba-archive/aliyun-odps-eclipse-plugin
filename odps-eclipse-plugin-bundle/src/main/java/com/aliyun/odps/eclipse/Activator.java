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

package com.aliyun.odps.eclipse;

import java.util.LinkedList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.aliyun.odps.eclipse.template.ODPSProjectItem;
import com.aliyun.odps.eclipse.utils.ConfigurationPersistenceUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "com.aliyun.odps.eclipse"; //$NON-NLS-1$


  // The shared instance
  private static Activator plugin;

  private java.util.List<ODPSProjectItem> odpsProjectList = new LinkedList<ODPSProjectItem>();

  /**
   * The constructor
   */
  public Activator() {}

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext )
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);

    plugin = this;

    // Add example project
    ConfigurationPersistenceUtil.loadOdpsConfig();
    if (odpsProjectList.size() == 0) {
      ODPSProjectItem item = new ODPSProjectItem();
      item.setProject("example_project");
      item.setEndpoint("http://service-corp.odps.aliyun-inc.com/api");
      item.setAccessId("yourAccessId");
      item.setAccessKey("yourAccessKey");
      ConfigurationPersistenceUtil.addProject(item, odpsProjectList, null);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
   */
  public void stop(BundleContext context) throws Exception {

    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

  /**
   * Global log method.
   * 
   * @param message
   */
  public static void error(String message) {
    getDefault().getLog().log(
        new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, String.valueOf(message), null));
  }

  /**
   * Global log method.
   * 
   * @param message
   * @param t
   */
  public static void error(String message, Throwable t) {
    getDefault().getLog().log(
        new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, String.valueOf(message), t));
  }

  /**
   * Global log method.
   * 
   * @param t
   */
  public static void error(Throwable e) {
    error(e.getMessage(), e);
  }

  public java.util.List<ODPSProjectItem> getOdpsProjectList() {
    return odpsProjectList;
  }
}
