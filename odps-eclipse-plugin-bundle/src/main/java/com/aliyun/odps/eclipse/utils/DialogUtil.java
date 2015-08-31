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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class DialogUtil {

  public static void error(String title, String msg) {
    MessageDialog.openError(Display.getDefault().getActiveShell(), title, msg);
  }

  public static boolean confirm(String title, String msg) {
    return MessageDialog.openConfirm(Display.getDefault().getActiveShell(), title, msg);
  }

  public static void inform(String title, String msg) {
    MessageDialog.openInformation(Display.getDefault().getActiveShell(), title, msg);
  }

  public static void question(String title, String msg) {
    MessageDialog.openQuestion(Display.getDefault().getActiveShell(), title, msg);
  }

  public static void warn(String title, String msg) {
    MessageDialog.openWarning(Display.getDefault().getActiveShell(), title, msg);
  }

}
