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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ComboDialogField;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewReducerWizard extends NewElementWizard implements INewWizard {

  private Page page;

  public NewReducerWizard() {
    setWindowTitle("New Reducer");
  }

  public void run(IProgressMonitor monitor) {
    try {
      page.createType(monitor);
    } catch (CoreException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    super.init(workbench, selection);

    page = new Page();
    addPage(page);
    page.setSelection(selection);
  }

  public static class Page extends NewTypeWizardPage {

    private ComboDialogField keyCombo;
    private ComboDialogField valueCombo;

    public Page() {
      super(true, "Reducer");

      setTitle("Reducer");
      setDescription("Create a new Reducer implementation.");
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
      super.createTypeMembers(newType, imports, monitor);

      imports.addImport("java.io.IOException");
      imports.addImport("java.util.Iterator");
      imports.addImport("com.aliyun.odps.data.Record");
      imports.addImport("com.aliyun.odps.mapred.ReducerBase");
      imports.addImport("com.aliyun.odps.mapred.TaskContext");

      newType.createMethod(
          "@Override\npublic void setup(TaskContext context) throws IOException \n{\n}\n", null,
          false, monitor);
      newType
          .createMethod(
              "@Override\npublic void reduce(Record key, Iterator<Record> values, TaskContext context) throws IOException \n{\n\n"
                  + "\twhile (values.hasNext()){\n"
                  + "values.next();\n"
                  + "\t\t// TODO process value\n" + "\t}\n" + "}\n", null, false, monitor);
      newType.createMethod(
          "@Override\npublic void cleanup(TaskContext context) throws IOException \n{\n}\n", null,
          false, monitor);
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

      updateSuperClass(keyCombo, valueCombo);

      setFocus();
      validate();
    }

    private void updateSuperClass(ComboDialogField keyCombo, ComboDialogField valueCombo) {
      setSuperClass("com.aliyun.odps.mapred.ReducerBase", true);
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

  @Override
  public boolean performFinish() {
    if (super.performFinish()) {
      if (getCreatedElement() != null) {
        selectAndReveal(page.getModifiedResource());
        openResource((IFile) page.getModifiedResource());
      }

      return true;
    } else {
      return false;
    }
  }

  @Override
  protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
    this.run(monitor);
  }

  @Override
  public IJavaElement getCreatedElement() {
    return (page.getCreatedType() == null) ? null : page.getCreatedType().getPrimaryElement();
  }
}
