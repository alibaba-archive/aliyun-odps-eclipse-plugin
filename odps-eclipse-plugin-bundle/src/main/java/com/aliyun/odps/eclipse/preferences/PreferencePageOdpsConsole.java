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

package com.aliyun.odps.eclipse.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.aliyun.odps.eclipse.Activator;
import com.aliyun.odps.eclipse.constants.PreferenceConstants;
import com.aliyun.odps.eclipse.template.ODPSProjectItem;
import com.aliyun.odps.eclipse.utils.ConfigurationPersistenceUtil;
import com.aliyun.odps.eclipse.utils.OdpsConsoleUtil;
import com.aliyun.odps.eclipse.utils.VersionCompatibilityUtil;

public class PreferencePageOdpsConsole extends PreferencePage implements IWorkbenchPreferencePage,
    SelectionListener, FocusListener {

  private final static String TITLE_TXT = "ODPS Settings";
  private final static String CONSOLE_LOCATION_TXT = "Config ODPS console installation path";
  private final static String RETAIN_TEMP_TXT = "Retain local job temp directory";

  private Text txtConsolePath;
  private Button btnConsoleLocation;
  private Button localModeRadio;
  private Button remoteModeRadio;
  private Button btnRetainTempDir;
  private Text txtLimitRecordCount;
  private Label curConsoleVersionLabel;
  private Label versionWarningLabel;

  public PreferencePageOdpsConsole() {
    setPreferenceStore(Activator.getDefault().getPreferenceStore());
    setTitle(TITLE_TXT);
  }

  @Override
  public void init(IWorkbench workbench) {

  }

  @Override
  protected void performDefaults() {
    super.performDefaults();
  }

  @Override
  public boolean performOk() {
    return super.performOk();
  }

  @Override
  protected Control createContents(Composite parent) {
    Font font = parent.getFont();
    Composite comp = new Composite(parent, SWT.NONE);
    comp.setLayout(new GridLayout(1, false));
    comp.setFont(font);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.horizontalSpan = 1;
    comp.setLayoutData(gd);

    createConsolePathGroup(comp);
    createRunModeGroup(comp);
    createLimitDownloadRecordCount(comp);

    btnRetainTempDir = new Button(comp, SWT.CHECK | SWT.BEGINNING | SWT.LEFT);
    btnRetainTempDir.setText(RETAIN_TEMP_TXT);
    btnRetainTempDir.addSelectionListener(this);

    init();

    return comp;
  }

  private void init() {
    String consolePath = getPreferenceStore().getString(PreferenceConstants.P_CONSOLE_PATH);
    if (consolePath == null || consolePath.isEmpty()) {
      return;
    }
    txtConsolePath.setText(consolePath);

    String runMode = getPreferenceStore().getString(PreferenceConstants.P_RUN_MODE);
    if (runMode == null || !runMode.equals("lot")) {
      localModeRadio.setSelection(true);
      remoteModeRadio.setSelection(false);
    } else {
      localModeRadio.setSelection(false);
      remoteModeRadio.setSelection(true);
    }

    boolean isRettainTempDir = getPreferenceStore().getBoolean(PreferenceConstants.P_RETAIN_TEMP);
    if (isRettainTempDir) {
      btnRetainTempDir.setSelection(true);
    } else {
      btnRetainTempDir.setSelection(false);
    }

    String limitDownloadRecordCountStr =
        getPreferenceStore().getString(PreferenceConstants.P_DOWNLOAD_RECORD_LIMIT);
    if (limitDownloadRecordCountStr == null || limitDownloadRecordCountStr.trim().isEmpty()) {
      limitDownloadRecordCountStr = "100";
    }
    int limitDownloadRecordCount = Integer.parseInt(limitDownloadRecordCountStr);
    if (limitDownloadRecordCount < 0 || limitDownloadRecordCount > 10000) {
      limitDownloadRecordCount = 100;
    }
    txtLimitRecordCount.setText(limitDownloadRecordCount + "");

    updateConsoleVersionWarning(consolePath);

  }

  private void createConsolePathGroup(Composite parent) {
    Font font = parent.getFont();

    Group group = new Group(parent, SWT.NONE);
    GridLayout topLayout = new GridLayout();
    topLayout.numColumns = 2;
    group.setLayout(topLayout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.heightHint = 90;
    group.setLayoutData(gd);
    group.setFont(font);
    group.setText(CONSOLE_LOCATION_TXT);

    txtConsolePath = new Text(group, SWT.BORDER);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    txtConsolePath.setLayoutData(gd);
    txtConsolePath.addFocusListener(this);

    btnConsoleLocation = new Button(group, SWT.NONE);
    btnConsoleLocation.setText("Browse...");
    btnConsoleLocation
        .setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
    btnConsoleLocation.addSelectionListener(this);

    curConsoleVersionLabel = new Label(group, SWT.NONE);
    new Label(group, SWT.NONE);

    versionWarningLabel = new Label(group, SWT.NONE);
    new Label(group, SWT.NONE);

  }

  private void createRunModeGroup(Composite parent) {
    Font font = parent.getFont();

    Group group = new Group(parent, SWT.NONE);
    GridLayout topLayout = new GridLayout();
    topLayout.numColumns = 2;
    group.setLayout(topLayout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.heightHint = 50;
    group.setLayoutData(gd);
    group.setFont(font);
    group.setText("Run Mode");

    localModeRadio = new Button(group, SWT.RADIO);
    localModeRadio.setSelection(true);
    localModeRadio.setText("Local");

    remoteModeRadio = new Button(group, SWT.RADIO);
    remoteModeRadio.setText("Remote");

    localModeRadio.addSelectionListener(this);
    // remoteModeRadio.addSelectionListener(this);
    remoteModeRadio.setEnabled(false);

  }

  private void createLimitDownloadRecordCount(Composite parent) {
    Font font = parent.getFont();

    Group group = new Group(parent, SWT.NONE);
    GridLayout topLayout = new GridLayout();
    topLayout.numColumns = 2;
    group.setLayout(topLayout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.heightHint = 50;
    group.setLayoutData(gd);
    group.setFont(font);
    group.setText("limit record count of downloaded");

    txtLimitRecordCount = new Text(group, SWT.BORDER);
    gd = new GridData();
    gd.widthHint = 40;
    txtLimitRecordCount.setLayoutData(gd);
    txtLimitRecordCount.addFocusListener(this);
    Label label = new Label(group, SWT.NONE);
    label.setText("(0~10000)");

  }

  @Override
  public void widgetSelected(SelectionEvent event) {
    if (event.getSource() == btnConsoleLocation) {
      DirectoryDialog dialog = new DirectoryDialog(this.getShell());
      dialog.setMessage(CONSOLE_LOCATION_TXT);
      dialog.setText(CONSOLE_LOCATION_TXT);
      String consoleDir = dialog.open();
      if (consoleDir != null) {
        txtConsolePath.setText(consoleDir.trim());
        validConsolePath(consoleDir);
      }
    } else if (event.getSource() == localModeRadio) {
      getPreferenceStore().setValue(PreferenceConstants.P_RUN_MODE, "local");
    } else if (event.getSource() == remoteModeRadio) {
      getPreferenceStore().setValue(PreferenceConstants.P_RUN_MODE, "lot");
    } else if (event.getSource() == btnRetainTempDir) {
      if (btnRetainTempDir.getSelection()) {
        getPreferenceStore().setValue(PreferenceConstants.P_RETAIN_TEMP, true);
      } else {
        getPreferenceStore().setValue(PreferenceConstants.P_RETAIN_TEMP, false);
      }
    }

  }

  @Override
  public void widgetDefaultSelected(SelectionEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void focusGained(FocusEvent event) {
    if (event.getSource() == txtConsolePath) {
      setErrorMessage(null);
    }
  }

  @Override
  public void focusLost(FocusEvent event) {
    setErrorMessage(null);
    if (event.getSource() == txtConsolePath) {
      validConsolePath(txtConsolePath.getText().trim());
    } else if (event.getSource() == txtLimitRecordCount) {
      validDownloadRecordLimit(txtLimitRecordCount.getText().trim());
    }
  }

  private void validConsolePath(String consoleDir) {
    if (OdpsConsoleUtil.validateODPSConoleLocation(consoleDir)) {
      setErrorMessage(null);
      getPreferenceStore().setValue(PreferenceConstants.P_CONSOLE_PATH, consoleDir);
      // load odps configuration
      java.util.List<ODPSProjectItem> list = OdpsConsoleUtil.listOdpsProjectFromConsole(consoleDir);
      if (list != null && list.size() > 0) {
        for (ODPSProjectItem item : list) {
          ConfigurationPersistenceUtil.addProject(item,
              Activator.getDefault().getOdpsProjectList(), null);
        }
      }

      updateConsoleVersionWarning(consoleDir);
      updateApplyButton();

    } else {
      setErrorMessage("Invalid Console Path");
    }
  }

  private void updateConsoleVersionWarning(String consoleDir) {
    if (consoleDir == null || consoleDir.trim().isEmpty()) {
      return;
    }
    String consoleVersion = VersionCompatibilityUtil.getConsoleVersion(consoleDir);
    if (consoleVersion == null || consoleVersion.trim().isEmpty()
        || consoleVersion.toLowerCase().equals("null")) {
      curConsoleVersionLabel.setText("Version: lower than 0.13.0");
      curConsoleVersionLabel.setSize(200, curConsoleVersionLabel.getSize().y);
      curConsoleVersionLabel.update();
      versionWarningLabel.setText("Warning: Version is too lower,suggest >="
          + VersionCompatibilityUtil.expectedSdkVersion);
      versionWarningLabel.update();
      return;
    }
    curConsoleVersionLabel.setText("Version: " + consoleVersion);
    curConsoleVersionLabel.setSize(200, curConsoleVersionLabel.getSize().y);
    curConsoleVersionLabel.update();
    if (VersionCompatibilityUtil.compareSdkVersionToEclipseVersion(consoleDir) < 0) {
      versionWarningLabel.setText("Warning: Version is too lower,suggest >="
          + VersionCompatibilityUtil.expectedSdkVersion);
      versionWarningLabel.update();
    }

  }

  private void validDownloadRecordLimit(String str) {
    boolean isOk = true;
    try {
      int count = Integer.parseInt(str);
      if (count < 0 || count > 10000) {
        isOk = false;
      } else {
        getPreferenceStore().setValue(PreferenceConstants.P_DOWNLOAD_RECORD_LIMIT, str);
      }
    } catch (Exception e) {
      isOk = false;
    }

    if (!isOk) {
      setErrorMessage("valid record count of downloaded is 0~10000!");
      txtLimitRecordCount.setText("100");
    }
  }

}
