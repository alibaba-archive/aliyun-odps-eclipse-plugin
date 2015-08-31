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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class NewUDFWizardPage extends NewTypeWizardPage {

  public NewUDFWizardPage() {
    super(true, "UDF");
    setTitle("New UDF");
    setDescription("Create a new UDF implementation.");
  }

  public void setSelection(IStructuredSelection selection) {
    initContainerPage(getInitialJavaElement(selection));
    initTypePage(getInitialJavaElement(selection));
  }

  @Override
  public void createType(IProgressMonitor monitor) throws CoreException, InterruptedException {
    super.createType(monitor);
  }

  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    imports.addImport("com.aliyun.odps.udf.UDF");
    super.createTypeMembers(newType, imports, monitor);

    newType
        .createMethod(
            "// TODO define parameters and return type, e.g., public Long evaluate(Long a, Long b)\npublic ... evaluate(...){\n}",
            null, false, monitor);
  }

  public void createControl(Composite parent) {
    initializeDialogUnits(parent);
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.numColumns = 4;
    composite.setLayout(layout);

    createContainerControls(composite, 4);
    createPackageControls(composite, 4);
    createSeparator(composite, 4);

    createTypeNameControls(composite, 4);
    createSuperClassControls(composite, 4);
    createSuperInterfacesControls(composite, 4);
    setControl(composite);

    updateSuperClass();

    setFocus();
    validate();
  }

  private void updateSuperClass() {
    setSuperClass("com.aliyun.odps.udf.UDF", true);
  }

  @Override
  protected void handleFieldChanged(String fieldName) {
    super.handleFieldChanged(fieldName);
    validate();
  }

  private void validate() {

    updateStatus(new IStatus[] {fContainerStatus, fPackageStatus, fTypeNameStatus,
        fSuperClassStatus, fSuperInterfacesStatus});
  }
}
