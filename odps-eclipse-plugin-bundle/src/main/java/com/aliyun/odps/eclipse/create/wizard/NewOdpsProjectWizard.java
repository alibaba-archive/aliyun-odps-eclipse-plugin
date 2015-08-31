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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPage;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import com.aliyun.odps.eclipse.Activator;
import com.aliyun.odps.eclipse.template.ODPSProjectItem;
import com.aliyun.odps.eclipse.utils.ConfigurationPersistenceUtil;
import com.aliyun.odps.eclipse.utils.OdpsConsoleUtil;
import com.aliyun.odps.eclipse.utils.ProjectUtils;

@SuppressWarnings({"deprecation"})
public class NewOdpsProjectWizard extends NewElementWizard implements IWorkbenchWizard,
    IExecutableExtension {
  static Logger log = Logger.getLogger(NewOdpsProjectWizard.class.getName());

  private NewOdpsProjectWizardPage firstPage;

  private NewJavaProjectWizardPage javaPage;

  public NewDriverWizardPage newDriverPage;

  private IConfigurationElement config;

  public NewOdpsProjectWizard() {
    setWindowTitle("New ODPS Project Wizard");
    // this(null, null);
  }

  public void init(IWorkbench workbench, IStructuredSelection selection) {

  }

  @Override
  public boolean canFinish() {
    return firstPage.isPageComplete() && javaPage.isPageComplete()
    // && ((!firstPage.generateDriver.getSelection())
    // || newDriverPage.isPageComplete()
    ;
  }

  @Override
  public IWizardPage getNextPage(IWizardPage page) {
    // if (page == firstPage
    // && firstPage.generateDriver.getSelection()
    // )
    // {
    // return newDriverPage; // if "generate mapper" checked, second page is
    // new driver page
    // }
    // else
    // {
    IWizardPage answer = super.getNextPage(page);
    if (answer == newDriverPage) {
      return null; // dont flip to new driver page unless "generate
      // driver" is checked
    } else if (answer == javaPage) {
      return answer;
    } else {
      return answer;
    }
    // }
  }

  @Override
  public IWizardPage getPreviousPage(IWizardPage page) {
    if (page == newDriverPage) {
      return firstPage; // newDriverPage, if it appears, is the second
      // page
    } else {
      return super.getPreviousPage(page);
    }
  }

  @Override
  public void addPages() {
    /*
     * firstPage = new HadoopFirstPage(); addPage(firstPage ); addPage( new
     * JavaProjectWizardSecondPage(firstPage) );
     */

    firstPage = new NewOdpsProjectWizardPage();
    javaPage = new NewJavaProjectWizardPage(ResourcesPlugin.getWorkspace().getRoot(), firstPage);
    // newDriverPage = new NewDriverWizardPage(false);
    // newDriverPage.setPageComplete(false); // ensure finish button
    // initially disabled
    addPage(firstPage);
    addPage(javaPage);

    // addPage(newDriverPage);
  }

  @Override
  public boolean performFinish() {
    try {
      PlatformUI.getWorkbench().getProgressService()
          .runInUI(this.getContainer(), new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) {
              try {
                monitor.beginTask("Create ODPS Project", 300);

                javaPage.getRunnable().run(new SubProgressMonitor(monitor, 100));

                // if( firstPage.generateDriver.getSelection())
                // {
                // newDriverPage.setPackageFragmentRoot(javaPage.getNewJavaProject().getAllPackageFragmentRoots()[0],
                // false);
                // newDriverPage.getRunnable().run(new
                // SubProgressMonitor(monitor,100));
                // }

                IProject project = javaPage.getNewJavaProject().getResource().getProject();
                IProjectDescription description = project.getDescription();
                String[] existingNatures = description.getNatureIds();
                String[] natures = new String[existingNatures.length + 1];
                for (int i = 0; i < existingNatures.length; i++) {
                  natures[i + 1] = existingNatures[i];
                }

                natures[0] = ProjectNature.NATURE_ID;
                description.setNatureIds(natures);

                project.setPersistentProperty(new QualifiedName(Activator.PLUGIN_ID,
                    "ODPS.runtime.path"), firstPage.getCurrentConsolePath());
                project.setDescription(description, new NullProgressMonitor());

                String[] natureIds = project.getDescription().getNatureIds();
                for (int i = 0; i < natureIds.length; i++) {
                  log.fine("Nature id # " + i + " > " + natureIds[i]);
                }

                try {
                  ProjectUtils.addODPSNature(project, monitor, firstPage.getCurrentConsolePath());
                } catch (IOException e) {
                  e.printStackTrace();
                }

                monitor.worked(100);
                monitor.done();

                BasicNewProjectResourceWizard.updatePerspective(config);

                loadOdpsProject();
              } catch (CoreException e) {
                // TODO Auto-generated catch block
                log.log(Level.SEVERE, "CoreException thrown.", e);
              } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }
          }, null);
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return true;
  }

  public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
      throws CoreException {
    this.config = config;
  }

  @Override
  protected void finishPage(IProgressMonitor arg0) throws InterruptedException, CoreException {
    // TODO Auto-generated method stub

  }

  @Override
  public IJavaElement getCreatedElement() {
    // TODO Auto-generated method stub
    return null;
  }

  private void loadOdpsProject() {
    if (firstPage.getCurrentConsolePath() == null) {
      return;
    }
    java.util.List<ODPSProjectItem> list =
        OdpsConsoleUtil.listOdpsProjectFromConsole(firstPage.getCurrentConsolePath());
    if (list != null && list.size() > 0) {
      for (ODPSProjectItem item : list) {
        ConfigurationPersistenceUtil.addProject(item, Activator.getDefault().getOdpsProjectList(),
            null);
      }
    }
  }
}
