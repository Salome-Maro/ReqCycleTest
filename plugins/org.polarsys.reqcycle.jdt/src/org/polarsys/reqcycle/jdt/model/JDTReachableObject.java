/*******************************************************************************
 *  Copyright (c) 2013 AtoS
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html *
 *  Contributors:
 *    Olivier Melois (AtoS) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.polarsys.reqcycle.jdt.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.polarsys.reqcycle.jdt.utils.JDTUtils;
import org.polarsys.reqcycle.uri.exceptions.VisitableException;
import org.polarsys.reqcycle.uri.model.Reachable;
import org.polarsys.reqcycle.uri.model.ReachableObject;
import org.polarsys.reqcycle.uri.visitors.EmptyVisitable;
import org.polarsys.reqcycle.uri.visitors.IVisitable;

public class JDTReachableObject implements ReachableObject {

	private Reachable reachable;
	IFile file = null;
	private IJavaElement element;

	public JDTReachableObject(Reachable t) {
		this.reachable = t;
		String path = t.trimFragment().toString()
				.replaceFirst(JDTUtils.PLATFORM, "");
		file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
		element = JavaCore.create(file);
		if (element instanceof ITypeRoot) {
			ITypeRoot aClass = (ITypeRoot) element;
			String fragment = t.getFragment();
			if (fragment != null && fragment.length() > 0) {
				String[] splitted = fragment.split(JDTUtils.SEPARATOR);
				if (splitted.length > 0) {
					element = findMethodRecursively(aClass, splitted);
				} else {
					element = aClass;
				}
			} else {
				element = aClass;
			}
		}
	}

	private IJavaElement findMethodRecursively(IParent aClass,
			final String[] javaElement) {
		try {
			IJavaElement result = null;
			boolean found = false;
			IParent currentContainer = aClass;
			int i = 0;
			for (String s : javaElement) {
				found = false;
				if (currentContainer != null) {
					for (IJavaElement j : currentContainer.getChildren()) {
						if (s.equals(j.getElementName())) {
							if (i == javaElement.length - 1) {
								result = j;
								found = true;
							} else if (j instanceof IParent) {
								currentContainer = (IParent) j;
							}
							break;
						}
					}
				}
				i++;
			}
			if (found && result != null) {
				return result;
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (ILabelProvider.class.equals(adapter)) {
			return new JDTLabelProvider();
		}
		if (IJavaElement.class.equals(adapter)) {
			return element;
		}
		return null;
	}

	@Override
	public IVisitable getVisitable() throws VisitableException {
		if (element != null) {
			return new JDTVisitable(element);
		} else {
			return new EmptyVisitable();
		}
	}

	@Override
	public String getRevisionIdentification() {
		return String.valueOf(file.getLocalTimeStamp());
	}

	@Override
	public Reachable getReachable(Object o) {
		return reachable;
	}

}
