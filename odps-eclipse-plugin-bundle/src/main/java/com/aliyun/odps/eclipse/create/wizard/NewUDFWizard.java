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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.aliyun.odps.eclipse.utils.NewWizardUtils;

public class NewUDFWizard extends NewElementWizard implements INewWizard {
  private final static String TESTUDFMODEL = "TestUDF.java.model";
  private final static String TESTUDFBASE = "TestUDFBase.java";
  private final static String TESTUDFBASEMODEL = "TestUDFBase.java.model";
  private NewUDFWizardPage page;

  public NewUDFWizard() {
    setWindowTitle("New UDF");
    setNeedsProgressMonitor(true);
  }

  public boolean performFinish() {
    warnAboutTypeCommentDeprecation();
    boolean res = super.performFinish();
    if (res) {
      IResource resource = page.getModifiedResource();
      if (resource != null) {
        selectAndReveal(resource);
        openResource((IFile) resource);
      }
    }

    // IRunnableWithProgress op = new IRunnableWithProgress() {
    // public void run(IProgressMonitor monitor)
    // throws InvocationTargetException {
    // try {
    // doFinish(monitor);
    // } catch (CoreException e) {
    // throw new InvocationTargetException(e);
    // } finally {
    // monitor.done();
    // }
    // }
    // };
    // try {
    // getContainer().run(true, false, op);
    // } catch (InterruptedException localInterruptedException) {
    // return false;
    // } catch (InvocationTargetException e) {
    // Throwable realException = e.getTargetException();
    // MessageDialog.openError(getShell(), "Error", realException.getMessage());
    // return false;
    // }
    return true;
  }

  private void doFinish(IProgressMonitor monitor) throws CoreException {
    String udfName = this.page.getTypeName();
    String testClassName = "Test" + udfName;
    String classPath = this.page.getPackageText();
    String udtfClassPath = classPath;
    String testClassPath = "test." + classPath;
    if (classPath.equals("")) {
      testClassPath = "";
    }

    IJavaProject myjavapro = this.page.getJavaProject();
    IProject myproject = myjavapro.getProject();

    String content =
        NewWizardUtils.getContent(myproject, testClassName, udfName, udtfClassPath, testClassPath,
            TESTUDFMODEL);
    NewWizardUtils.createClass(myjavapro, testClassName, testClassPath, monitor, content);
    content =
        NewWizardUtils.getContent(myproject, testClassName, udfName, udtfClassPath, testClassPath,
            TESTUDFBASEMODEL);
    NewWizardUtils.createTestBase(myjavapro, testClassPath, monitor, content, TESTUDFBASE);
    NewWizardUtils.createDataFile(myproject, testClassName, "udf_test", monitor);
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
    page = new NewUDFWizardPage();
    addPage(page);
    page.setSelection(selection);
  }

  @Override
  protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
    run(monitor);
  }

  @Override
  public IJavaElement getCreatedElement() {
    return page.getCreatedType().getPrimaryElement();
  }

}
