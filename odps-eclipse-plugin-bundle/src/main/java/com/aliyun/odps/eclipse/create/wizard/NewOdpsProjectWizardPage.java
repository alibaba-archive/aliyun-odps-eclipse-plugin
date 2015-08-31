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

package com.aliyun.odps.eclipse.create.wizard;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import com.aliyun.odps.eclipse.Activator;
import com.aliyun.odps.eclipse.constants.PreferenceConstants;
import com.aliyun.odps.eclipse.preferences.PreferencePageOdpsConsole;
import com.aliyun.odps.eclipse.utils.VersionCompatibilityUtil;

public class NewOdpsProjectWizardPage extends WizardNewProjectCreationPage implements
    SelectionListener {

  private final static String WIZARD_TITLE_TXT = "New ODPS Project";
  private final static String PAGE_TITLE_TXT = "";
  private final static String PAGE_DESCRIPTION_TXT = "Create ODPS project";
  private final static String CONSOLE_LOCATION_TXT = "Config ODPS console installation path";
  private final static String DEFAULT_CONSOLE_LOCATION_TXT =
      "Use default ODPS console installation path";
  private final static String SPECIFY_CONSOLE_LOCATION_TXT =
      "Specify ODPS console installation path";
  private final static String CONFIG_CONSOLE_LOCATION_LINK_TXT =
      "<a>Config ODPS console installation path...</a>";

  public NewOdpsProjectWizardPage() {
    super(WIZARD_TITLE_TXT);
    // setImageDescriptor(ImageLibrary.get("wizard.mapreduce.project.new"));
  }

  private Link linkConfigDefaultConsoleLocation;

  private Button radioDefaultConsoleLocation;

  private Button radioNewConsoleLocation;

  private Text txtNewConsoleLocation;

  private Button btnNewConsoleLocation;

  private String defaultConsolePath;

  private String currentConsolePath;

  private Label curConsoleVersionLabel;
  private Label versionWarningLabel;

  // private Button generateDriver;

  @Override
  public void createControl(Composite parent) {
    super.createControl(parent);

    setTitle(PAGE_TITLE_TXT);
    setDescription(PAGE_DESCRIPTION_TXT);

    Group group = new Group((Composite) getControl(), SWT.NONE);
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setText(CONSOLE_LOCATION_TXT);
    GridLayout layout = new GridLayout(2, true);
    layout.marginLeft = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    layout.marginRight = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    layout.marginTop = convertHorizontalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    layout.marginBottom = convertHorizontalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    layout.makeColumnsEqualWidth = false;
    group.setLayout(layout);
    new Label(group, SWT.NONE);
    new Label(group, SWT.NONE);

    radioDefaultConsoleLocation = new Button(group, SWT.RADIO);
    GridData d = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false);
    radioDefaultConsoleLocation.setLayoutData(d);
    radioDefaultConsoleLocation.setSelection(true);

    updateHadoopDirLabelFromPreferences();

    linkConfigDefaultConsoleLocation = new Link(group, SWT.NONE);
    linkConfigDefaultConsoleLocation.setText(CONFIG_CONSOLE_LOCATION_LINK_TXT);
    linkConfigDefaultConsoleLocation.setLayoutData(new GridData(GridData.BEGINNING,
        GridData.CENTER, false, false));
    linkConfigDefaultConsoleLocation.addSelectionListener(this);

    radioNewConsoleLocation = new Button(group, SWT.RADIO);
    GridData gd_radioNewConsoleLocation =
        new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
    gd_radioNewConsoleLocation.horizontalSpan = 2;
    radioNewConsoleLocation.setLayoutData(gd_radioNewConsoleLocation);
    radioNewConsoleLocation.setText(SPECIFY_CONSOLE_LOCATION_TXT);

    txtNewConsoleLocation = new Text(group, SWT.SINGLE | SWT.BORDER);
    txtNewConsoleLocation.setText("");
    // d = new GridData(SWT.LEFT, GridData.BEGINNING, true, false);
    d = new GridData(GridData.FILL_HORIZONTAL | GridData.BEGINNING | SWT.LEFT);
    d.horizontalSpan = 1;
    d.widthHint = 360;
    txtNewConsoleLocation.setLayoutData(d);
    txtNewConsoleLocation.setEnabled(false);

    btnNewConsoleLocation = new Button(group, SWT.NONE);
    btnNewConsoleLocation.setText("Browse...");
    btnNewConsoleLocation.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false,
        false));
    btnNewConsoleLocation.setEnabled(false);
    btnNewConsoleLocation.addSelectionListener(this);

    radioNewConsoleLocation.addSelectionListener(this);
    radioDefaultConsoleLocation.addSelectionListener(this);

    curConsoleVersionLabel = new Label(group, SWT.NONE);
    new Label(group, SWT.NONE);

    versionWarningLabel = new Label(group, SWT.NONE);
    new Label(group, SWT.NONE);

    // generateDriver = new Button((Composite) getControl(), SWT.CHECK);
    // generateDriver.setText("Generate a MapReduce driver");
    // generateDriver.addListener(SWT.Selection, new Listener()
    // {
    // public void handleEvent(Event event) {
    // getContainer().updateButtons(); }
    // });
  }

  @Override
  public boolean isPageComplete() {
    boolean validODPSConsole = validateODPSConoleLocation();

    if (!validODPSConsole && isCurrentPage()) {
      setErrorMessage("Invalid ODPS Console specified; please click 'Configure ODPS Console Directory");
    } else {
      setErrorMessage(null);
      updateConsoleVersionWarning(getCurrentConsolePath());
    }

    return super.isPageComplete() && validODPSConsole;
  }

  private boolean validateODPSConoleLocation() {
    FilenameFilter gotHadoopJar = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return (name.startsWith("odps-sdk-core") || name.startsWith("odps-mapred-local"))
            && name.endsWith(".jar");
      }
    };

    if (radioDefaultConsoleLocation.getSelection()) {
      this.currentConsolePath = defaultConsolePath;
      return new Path(defaultConsolePath).toFile().exists()
          && new Path(defaultConsolePath + Path.SEPARATOR + "lib").toFile().exists()
          && (new Path(defaultConsolePath + Path.SEPARATOR + "lib").toFile().list(gotHadoopJar).length > 0);
    } else {
      this.currentConsolePath = txtNewConsoleLocation.getText();
      File file = new Path(txtNewConsoleLocation.getText()).toFile();
      return file.exists()
          && new Path(txtNewConsoleLocation.getText() + Path.SEPARATOR + "lib").toFile().exists()
          && (new Path(txtNewConsoleLocation.getText() + Path.SEPARATOR + "lib").toFile().list(
              gotHadoopJar).length > 0);
    }
  }

  private void updateHadoopDirLabelFromPreferences() {
    defaultConsolePath =
        Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_CONSOLE_PATH);

    if ((defaultConsolePath != null) && (defaultConsolePath.length() > 0)) {
      radioDefaultConsoleLocation.setText(DEFAULT_CONSOLE_LOCATION_TXT);
    } else {
      radioDefaultConsoleLocation.setText(DEFAULT_CONSOLE_LOCATION_TXT);
    }
  }

  public void widgetDefaultSelected(SelectionEvent e) {}

  public void widgetSelected(SelectionEvent e) {
    if (e.getSource() == linkConfigDefaultConsoleLocation) {
      PreferenceManager manager = new PreferenceManager();
      manager.addToRoot(new PreferenceNode("ODPS Console Directory",
          new PreferencePageOdpsConsole()));
      PreferenceDialog dialog = new PreferenceDialog(this.getShell(), manager);
      dialog.create();
      dialog.setMessage(CONSOLE_LOCATION_TXT);
      dialog.setBlockOnOpen(true);
      dialog.open();
      updateHadoopDirLabelFromPreferences();
    } else if (e.getSource() == btnNewConsoleLocation) {
      DirectoryDialog dialog = new DirectoryDialog(this.getShell());
      dialog.setMessage(CONSOLE_LOCATION_TXT);
      dialog.setText(CONSOLE_LOCATION_TXT);
      String directory = dialog.open();

      if (directory != null) {
        txtNewConsoleLocation.setText(directory);

        if (!validateODPSConoleLocation()) {
          setErrorMessage("No ODPS SDK jar found in specified directory");
        } else {
          setErrorMessage(null);
        }
      }
    } else if (radioNewConsoleLocation.getSelection()) {
      txtNewConsoleLocation.setEnabled(true);
      btnNewConsoleLocation.setEnabled(true);
    } else {
      txtNewConsoleLocation.setEnabled(false);
      btnNewConsoleLocation.setEnabled(false);
    }
    getContainer().updateButtons();
  }

  public String getCurrentConsolePath() {
    return this.currentConsolePath;
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
}
