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
package com.aliyun.odps.eclipse.launch.configuration.udf;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.ResolvedSourceType;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class UDFSearchEngine {

  private class UDFClassCollector extends SearchRequestor {
    private List fResult;

    public UDFClassCollector() {
      fResult = new ArrayList(200);
    }

    public List getResult() {
      return fResult;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.core.search.SearchRequestor#acceptSearchMatch(org.eclipse
     * .jdt.core.search.SearchMatch)
     */
    public void acceptSearchMatch(SearchMatch match) throws CoreException {
      Object enclosingElement = match.getElement();
      if (enclosingElement instanceof ResolvedSourceType) { // defensive code
        try {
          ResolvedSourceType resolvedSourceType = (ResolvedSourceType) enclosingElement;
          fResult.add(resolvedSourceType.getCompilationUnit().getTypes()[0]);
        } catch (JavaModelException e) {
          JDIDebugUIPlugin.log(e.getStatus());
        }
      }
    }
  }

  /**
   * Searches for all main methods in the given scope. Valid styles are
   * IJavaElementSearchConstants.CONSIDER_BINARIES and
   * IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS
   * 
   * @param pm progress monitor
   * @param scope search scope
   * @param includeSubtypes whether to consider types that inherit a main method
   */
  public IType[] searchUDFClass(IProgressMonitor pm, IJavaSearchScope scope, boolean includeSubtypes) {
    int searchTicks = 100;
    if (includeSubtypes) {
      searchTicks = 25;
    }

    SearchPattern udfPattern =
        SearchPattern
            .createPattern(
                "com.aliyun.odps.udf.UDF", IJavaSearchConstants.CLASS, IJavaSearchConstants.IMPLEMENTORS, SearchPattern.R_PATTERN_MATCH); //$NON-NLS-1$
    SearchPattern udtfpattern =
        SearchPattern.createPattern("com.aliyun.odps.udf.UDTF", IJavaSearchConstants.CLASS,
            IJavaSearchConstants.IMPLEMENTORS, SearchPattern.R_PATTERN_MATCH);
    SearchParticipant[] participants =
        new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
    UDFClassCollector collector = new UDFClassCollector();
    IProgressMonitor udfSearchMonitor = new SubProgressMonitor(pm, searchTicks);
    try {
      pm.beginTask("Searching for UDF class...", 100);
      new SearchEngine().search(udfPattern, participants, scope, collector, udfSearchMonitor);
      pm.beginTask("Searching for UDTF class...", 100);
      IProgressMonitor udtfSearchMonitor = new SubProgressMonitor(pm, searchTicks);
      new SearchEngine().search(udtfpattern, participants, scope, collector, udtfSearchMonitor);
      pm.beginTask("Searching for UDF class...", 100);
    } catch (CoreException ce) {
      JDIDebugUIPlugin.log(ce);
    }

    List result = collector.getResult();
    if (includeSubtypes) {
      IProgressMonitor subtypesMonitor = new SubProgressMonitor(pm, 75);
      subtypesMonitor.beginTask("Select UDF|UDTF class", result.size());
      Set set = addSubtypes(result, subtypesMonitor, scope);
      return (IType[]) set.toArray(new IType[set.size()]);
    }
    return (IType[]) result.toArray(new IType[result.size()]);
  }

  /**
   * Adds subtypes and enclosed types to the listing of 'found' types
   * 
   * @param types the list of found types thus far
   * @param monitor progress monitor
   * @param scope the scope of elements
   * @return as set of all types to consider
   */
  private Set addSubtypes(List types, IProgressMonitor monitor, IJavaSearchScope scope) {
    Iterator iterator = types.iterator();
    Set result = new HashSet(types.size());
    IType type = null;
    ITypeHierarchy hierarchy = null;
    IType[] subtypes = null;
    while (iterator.hasNext()) {
      type = (IType) iterator.next();
      if (result.add(type)) {
        try {
          hierarchy = type.newTypeHierarchy(monitor);
          subtypes = hierarchy.getAllSubtypes(type);
          for (int i = 0; i < subtypes.length; i++) {
            if (scope.encloses(subtypes[i])) {
              result.add(subtypes[i]);
            }
          }
        } catch (JavaModelException e) {
          JDIDebugUIPlugin.log(e);
        }
      }
      monitor.worked(1);
    }
    return result;
  }

  /**
   * Returns the package fragment root of <code>IJavaElement</code>. If the given element is already
   * a package fragment root, the element itself is returned.
   */
  public static IPackageFragmentRoot getPackageFragmentRoot(IJavaElement element) {
    return (IPackageFragmentRoot) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
  }

  /**
   * Searches for all main methods in the given scope. Valid styles are
   * IJavaElementSearchConstants.CONSIDER_BINARIES and
   * IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS
   * 
   * @param includeSubtypes whether to consider types that inherit a main method
   */
  public IType[] searchUDFClass(IRunnableContext context, final IJavaSearchScope scope,
      final boolean includeSubtypes) throws InvocationTargetException, InterruptedException {
    final IType[][] res = new IType[1][];

    IRunnableWithProgress runnable = new IRunnableWithProgress() {
      public void run(IProgressMonitor pm) throws InvocationTargetException {
        res[0] = searchUDFClass(pm, scope, includeSubtypes);
      }
    };
    context.run(true, true, runnable);

    return res[0];
  }

}
