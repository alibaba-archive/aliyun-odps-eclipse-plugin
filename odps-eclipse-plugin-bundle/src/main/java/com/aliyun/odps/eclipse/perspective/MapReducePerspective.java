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

package com.aliyun.odps.eclipse.perspective;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.IProgressConstants;

public class MapReducePerspective implements IPerspectiveFactory {

  public static final String ODPS_MAPREDUCE_ID = "com.aliyun.odps.eclipse.perspective.mapreduce";

  @Override
  public void createInitialLayout(IPageLayout layout) {
    String editorArea = layout.getEditorArea();

    IFolderLayout folder = layout.createFolder("left", IPageLayout.LEFT, (float) 0.25, editorArea); //$NON-NLS-1$
    folder.addView(JavaUI.ID_PACKAGES);
    folder.addPlaceholder(JavaUI.ID_TYPE_HIERARCHY);
    folder.addPlaceholder(JavaPlugin.ID_RES_NAV);
    folder.addPlaceholder(IPageLayout.ID_PROJECT_EXPLORER);

    IFolderLayout outputfolder =
        layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.75, editorArea); //$NON-NLS-1$
    outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
    outputfolder.addView(JavaUI.ID_JAVADOC_VIEW);
    outputfolder.addView(JavaUI.ID_SOURCE_VIEW);
    // outputfolder.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
    outputfolder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
    outputfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
    outputfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);

    IFolderLayout outlineFolder =
        layout.createFolder("right", IPageLayout.RIGHT, (float) 0.75, editorArea); //$NON-NLS-1$
    outlineFolder.addView(IPageLayout.ID_OUTLINE);

    // outlineFolder.addPlaceholder(TemplatesView.ID);

    layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
    layout.addActionSet(JavaUI.ID_ACTION_SET);
    layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
    layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);

    // views - java
    layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
    layout.addShowViewShortcut(JavaUI.ID_TYPE_HIERARCHY);
    layout.addShowViewShortcut(JavaUI.ID_SOURCE_VIEW);
    layout.addShowViewShortcut(JavaUI.ID_JAVADOC_VIEW);

    // views - search
    // layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);

    // views - debugging
    layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);

    // views - standard workbench
    layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
    layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
    layout.addShowViewShortcut(JavaPlugin.ID_RES_NAV);
    layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
    layout.addShowViewShortcut(IProgressConstants.PROGRESS_VIEW_ID);
    layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
    // layout.addShowViewShortcut(TemplatesView.ID);
    layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$

    // new actions - ODPS porject creation wizard
    layout.addNewWizardShortcut("com.aliyun.odps.eclipse.NewOdpsProjectWizard");
    layout.addNewWizardShortcut("com.aliyun.odps.eclipse.NewDriverWizard");
    layout.addNewWizardShortcut("com.aliyun.odps.eclipse.NewMapperWizard");
    layout.addNewWizardShortcut("com.aliyun.odps.eclipse.NewReducerWizard");
    layout.addNewWizardShortcut("com.aliyun.odps.eclipse.NewUDFWizard");
    layout.addNewWizardShortcut("com.aliyun.odps.eclipse.NewUDTFWizard");

    // new actions - Java project creation wizard
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.JavaProjectWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewPackageCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewClassCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewEnumCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewAnnotationCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSourceFolderCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSnippetFileCreationWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewJavaWorkingSetWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$

    // 'Window' > 'Open Perspective' contributions
    // layout.addPerspectiveShortcut(ODPSExplorerPerspective.ODPS_EXPLORER_ID);
    layout.addPerspectiveShortcut(JavaUI.ID_BROWSING_PERSPECTIVE);
    layout.addPerspectiveShortcut(IDebugUIConstants.ID_DEBUG_PERSPECTIVE);

  }

}
