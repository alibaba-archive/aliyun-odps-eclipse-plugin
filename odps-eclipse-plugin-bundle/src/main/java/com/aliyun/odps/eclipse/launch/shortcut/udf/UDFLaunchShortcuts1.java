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
package com.aliyun.odps.eclipse.launch.shortcut.udf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.DebugTypeSelectionDialog;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Common behavior for Java launch shortcuts
 * <p>
 * This class may be subclassed.
 * </p>
 * 
 * @since 3.3
 */
public abstract class UDFLaunchShortcuts1 implements ILaunchShortcut2 {

  /**
   * Returns the type of configuration this shortcut is applicable to.
   * 
   * @return the type of configuration this shortcut is applicable to
   */
  protected abstract ILaunchConfigurationType getConfigurationType();

  /**
   * Creates and returns a new configuration based on the specified type.
   * 
   * @param type type to create a launch configuration for
   * @return launch configuration configured to launch the specified type
   */
  protected abstract ILaunchConfiguration createConfiguration(IType type);

  /**
   * Finds and returns the types in the given collection of elements that can be launched.
   * 
   * @param elements scope to search for types that can be launched
   * @param context progress reporting context
   * @return collection of types that can be launched, possibly empty
   * @exception InterruptedException if the search is canceled
   * @exception CoreException if the search fails
   */
  protected abstract IType[] findTypes(Object[] elements, IRunnableContext context)
      throws InterruptedException, CoreException;

  /**
   * Returns a title for a type selection dialog used to prompt the user when there is more than one
   * type that can be launched.
   * 
   * @return type selection dialog title
   */
  protected abstract String getTypeSelectionTitle();

  /**
   * Returns an error message to use when the editor does not contain a type that can be launched.
   * 
   * @return error message when editor cannot be launched
   */
  protected abstract String getEditorEmptyMessage();

  /**
   * Returns an error message to use when the selection does not contain a type that can be
   * launched.
   * 
   * @return error message when selection cannot be launched
   */
  protected abstract String getSelectionEmptyMessage();

  /**
   * Resolves a type that can be launched from the given scope and launches in the specified mode.
   * 
   * @param scope the java elements to consider for a type that can be launched
   * @param mode launch mode
   * @param selectTitle prompting title for choosing a type to launch
   * @param emptyMessage error message when no types are resolved for launching
   */
  private void searchAndLaunch(Object[] scope, String mode, String selectTitle, String emptyMessage) {
    IType[] types = null;
    try {
      types = findTypes(scope, PlatformUI.getWorkbench().getProgressService());
    } catch (InterruptedException e) {
      return;
    } catch (CoreException e) {
      MessageDialog.openError(getShell(), LauncherMessages.JavaLaunchShortcut_0, e.getMessage());
      return;
    }
    IType type = null;
    if (types.length == 0) {
      MessageDialog.openError(getShell(), LauncherMessages.JavaLaunchShortcut_1, emptyMessage);
    } else if (types.length > 1) {
      type = chooseType(types, selectTitle);
    } else {
      type = types[0];
    }
    if (type != null) {
      launch(type, mode);
    }
  }

  /**
   * Prompts the user to select a type from the given types.
   * 
   * @param types the types to choose from
   * @param title the selection dialog title
   * 
   * @return the selected type or <code>null</code> if none.
   */
  protected IType chooseType(IType[] types, String title) {
    DebugTypeSelectionDialog mmsd =
        new DebugTypeSelectionDialog(JDIDebugUIPlugin.getShell(), types, title);
    if (mmsd.open() == Window.OK) {
      return (IType) mmsd.getResult()[0];
    }
    return null;
  }

  /**
   * Launches the given type in the specified mode.
   * 
   * @param type type to launch
   * @param mode launch mode
   * @since 3.5
   */
  protected void launch(IType type, String mode) {
    ILaunchConfiguration config = findLaunchConfiguration(type, getConfigurationType());
    if (config == null) {
      config = createConfiguration(type);
    }
    if (config != null) {
      DebugUITools.launch(config, mode);
    }
  }

  /**
   * Finds and returns an <b>existing</b> configuration to re-launch for the given type, or
   * <code>null</code> if there is no existing configuration.
   * 
   * @return a configuration to use for launching the given type or <code>null</code> if none
   */
  protected ILaunchConfiguration findLaunchConfiguration(IType type,
      ILaunchConfigurationType configType) {
    List candidateConfigs = Collections.EMPTY_LIST;
    try {
      ILaunchConfiguration[] configs =
          DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(configType);
      candidateConfigs = new ArrayList(configs.length);
      for (int i = 0; i < configs.length; i++) {
        ILaunchConfiguration config = configs[i];
        if (config
            .getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "").equals(type.getFullyQualifiedName())) { //$NON-NLS-1$
          if (config
              .getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "").equals(type.getJavaProject().getElementName())) { //$NON-NLS-1$
            candidateConfigs.add(config);
          }
        }
      }
    } catch (CoreException e) {
      JDIDebugUIPlugin.log(e);
    }
    int candidateCount = candidateConfigs.size();
    if (candidateCount == 1) {
      return (ILaunchConfiguration) candidateConfigs.get(0);
    } else if (candidateCount > 1) {
      return chooseConfiguration(candidateConfigs);
    }
    return null;
  }

  /**
   * Returns a configuration from the given collection of configurations that should be launched, or
   * <code>null</code> to cancel. Default implementation opens a selection dialog that allows the
   * user to choose one of the specified launch configurations. Returns the chosen configuration, or
   * <code>null</code> if the user cancels.
   * 
   * @param configList list of configurations to choose from
   * @return configuration to launch or <code>null</code> to cancel
   */
  protected ILaunchConfiguration chooseConfiguration(List configList) {
    IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
    ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
    dialog.setElements(configList.toArray());
    dialog.setTitle(getTypeSelectionTitle());
    dialog.setMessage(LauncherMessages.JavaLaunchShortcut_2);
    dialog.setMultipleSelection(false);
    int result = dialog.open();
    labelProvider.dispose();
    if (result == Window.OK) {
      return (ILaunchConfiguration) dialog.getFirstResult();
    }
    return null;
  }

  /**
   * Convenience method to return the active workbench window shell.
   * 
   * @return active workbench window shell
   */
  protected Shell getShell() {
    return JDIDebugUIPlugin.getActiveWorkbenchShell();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.ui.IEditorPart, java.lang.String)
   */
  public void launch(IEditorPart editor, String mode) {
    IEditorInput input = editor.getEditorInput();
    IJavaElement je = (IJavaElement) input.getAdapter(IJavaElement.class);
    if (je != null) {
      searchAndLaunch(new Object[] {je}, mode, getTypeSelectionTitle(), getEditorEmptyMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.jface.viewers.ISelection,
   * java.lang.String)
   */
  public void launch(ISelection selection, String mode) {
    if (selection instanceof IStructuredSelection) {
      searchAndLaunch(((IStructuredSelection) selection).toArray(), mode, getTypeSelectionTitle(),
          getSelectionEmptyMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchShortcut2#getLaunchableResource(org.eclipse.ui.IEditorPart)
   */
  public IResource getLaunchableResource(IEditorPart editorpart) {
    return getLaunchableResource(editorpart.getEditorInput());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.debug.ui.ILaunchShortcut2#getLaunchableResource(org.eclipse.jface.viewers.ISelection
   * )
   */
  public IResource getLaunchableResource(ISelection selection) {
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection ss = (IStructuredSelection) selection;
      if (ss.size() == 1) {
        Object element = ss.getFirstElement();
        if (element instanceof IAdaptable) {
          return getLaunchableResource((IAdaptable) element);
        }
      }
    }
    return null;
  }

  /**
   * Returns the resource containing the Java element associated with the given adaptable, or
   * <code>null</code>.
   * 
   * @param adaptable adaptable object
   * @return containing resource or <code>null</code>
   */
  private IResource getLaunchableResource(IAdaptable adaptable) {
    IJavaElement je = (IJavaElement) adaptable.getAdapter(IJavaElement.class);
    if (je != null) {
      return je.getResource();
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchShortcut2#getLaunchConfigurations(org.eclipse.ui.IEditorPart)
   */
  public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
    // let the framework resolve configurations based on resource mapping
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.debug.ui.ILaunchShortcut2#getLaunchConfigurations(org.eclipse.jface.viewers.ISelection
   * )
   */
  public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
    // let the framework resolve configurations based on resource mapping
    return null;
  }


}
