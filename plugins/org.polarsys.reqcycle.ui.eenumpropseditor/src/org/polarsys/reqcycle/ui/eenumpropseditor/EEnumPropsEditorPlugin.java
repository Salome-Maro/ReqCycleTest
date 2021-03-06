/*******************************************************************************
 *  Copyright (c) 2013 AtoS
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html *
 *  Contributors:
 *    Papa Issa Diakhate (AtoS) - initial API and implementation and/or initial documentation
 *   
 *******************************************************************************/
package org.polarsys.reqcycle.ui.eenumpropseditor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class EEnumPropsEditorPlugin implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		EEnumPropsEditorPlugin.context = bundleContext;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		EEnumPropsEditorPlugin.context = null;
	}

}
