/*******************************************************************************
 *  Copyright (c) 2013 AtoS
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html *
 *  Contributors:
 *    Tristan Faure (AtoS) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.polarsys.reqcycle.uri.impl;

import java.net.URI;

import javax.inject.Singleton;

import org.polarsys.reqcycle.uri.IReachableCreator;
import org.polarsys.reqcycle.uri.model.Reachable;
import org.polarsys.reqcycle.uri.services.IReachableExtender;

@Singleton
public class ReachableCreator implements IReachableCreator {

	ExtenderManager manager = new ExtenderManager();

	public Reachable getReachable(URI uri) {
		return getReachable(uri, null);
	}

	public Reachable getReachable(URI uri, Object originalObject) {
		Reachable t = new Reachable();
		t.setFragment(uri.getFragment());
		t.setScheme(uri.getScheme());
		t.setSchemeSpecificPart(uri.getSchemeSpecificPart());
		t.setAuthority(uri.getAuthority());
		t.setUserInfo(uri.getUserInfo());
		t.setHost(uri.getHost());
		t.setPath(uri.getPath());
		t.setQuery(uri.getQuery());
		t.setFragment(uri.getFragment());
		t.setPort(uri.getPort());
		for (IReachableExtender ext : manager.getExtenders(uri, originalObject)) {
			t.putAll(ext.getExtendedProperties(uri, originalObject));
		}
		return t;
	}

}
