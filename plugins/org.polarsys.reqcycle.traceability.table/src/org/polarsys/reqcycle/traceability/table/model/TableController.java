/*******************************************************************************
 * Copyright (c) 2013, 2014 AtoS and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html *
 * Contributors:
 *  Olivier Melois (AtoS) - initial API and implementation and/or initial documentation
 *  Raphael Faudou (Samares Engineering) : Fixed performance issue
 *******************************************************************************/
package org.polarsys.reqcycle.traceability.table.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.polarsys.reqcycle.traceability.engine.ITraceabilityEngine;
import org.polarsys.reqcycle.traceability.engine.ITraceabilityEngine.DIRECTION;
import org.polarsys.reqcycle.traceability.engine.Request;
import org.polarsys.reqcycle.traceability.engine.Request.DEPTH;
import org.polarsys.reqcycle.traceability.exceptions.EngineException;
import org.polarsys.reqcycle.traceability.model.Link;
import org.polarsys.reqcycle.traceability.model.Pair;
import org.polarsys.reqcycle.traceability.model.scopes.CompositeScope;
import org.polarsys.reqcycle.traceability.model.scopes.Scopes;
import org.polarsys.reqcycle.traceability.storage.IStorageProvider;
import org.polarsys.reqcycle.traceability.storage.ITraceabilityStorage;
import org.polarsys.reqcycle.traceability.table.providers.TraceabilityLazyContentProvider;
import org.polarsys.reqcycle.traceability.types.scopes.ConfigurationScope;
import org.polarsys.reqcycle.uri.IReachableListenerManager;
import org.polarsys.reqcycle.uri.IReachableManager;
import org.polarsys.reqcycle.uri.model.Reachable;
import org.polarsys.reqcycle.utils.inject.ZigguratInject;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TableController {


	private static final String RDF_FILE = "./.traceability.rdf"; //$NON-NLS-1$

	@Inject
	@Named("RDF")
	protected IStorageProvider provider;
	
	@Inject
	protected IReachableListenerManager listenerManager ;

	@Inject
	protected ITraceabilityEngine engine;

	protected TableViewer viewer;

	protected Callable<Iterable<?>> callable;

	public TableController(TableViewer viewer) {
		this.viewer = viewer;
	}

	public void displayAllLinks() {
		callable = new Callable<Iterable<?>>() {

			@Override
			public Iterable<?> call() throws Exception {
				return getLinksFromEngine();
			}
		};
		refreshViewerData();
	}

	public void displayExplicitLinks(final IProject project) {
		callable = new Callable<Iterable<?>>() {

			@Override
			public Iterable<?> call() throws Exception {
				return getLinksFromProject(project);
			}
		};
		refreshViewerData();
	}

	protected Iterable<Link> getLinksFromEngine() {
		return new Iterable<Link>() {

			@Override
			public Iterator<Link> iterator() {
				long start = new Date().getTime();
				Request request = new Request();
				CompositeScope scope = new CompositeScope();
				scope.add(Scopes.getWorkspaceScope());
				scope.add(new ConfigurationScope());
				request.setScope(scope);
				request.setDepth(DEPTH.INFINITE);
				try {
					Iterator<Pair<Link, Reachable>> traceability = engine.getTraceability(request);
					return Iterators.transform(traceability, new Function<Pair<Link, Reachable>, Link>() {

						@Override
						public Link apply(Pair<Link, Reachable> arg0) {
							return arg0.getFirst();
						}
					});
				} catch (EngineException e) {
					e.printStackTrace();
				}
				long duration = new Date().getTime()-start;
				//System.out.println(" duration getLinks " + duration);
				return new ArrayList<Link>().iterator();
			}
		};
	}

	protected Iterable<Link> getLinksFromProject(final IProject project) {
		IFile file = project.getFile(new Path(RDF_FILE));
		if(file != null && file.exists()) {
			// get the storage for the file path
			String uri = file.getLocationURI().getPath();
			ITraceabilityStorage storage = provider.getStorage(uri);
			Iterable<Pair<Link, Reachable>> allTraceabilityLinks = storage.getAllTraceability(DIRECTION.DOWNWARD);

			return Iterables.transform(allTraceabilityLinks, new Function<Pair<Link, Reachable>, Link>() {

				@Override
				public Link apply(Pair<Link, Reachable> arg0) {
					return new TransverseLink(arg0.getFirst(), project);
				}
			});
		}
		return Lists.newArrayList();
	}

	public void deleteTraceabilityLinks(Iterator<TransverseLink> links) {
		ITraceabilityStorage storage = null;
		IProject project = null;
		try {
			Collection<Reachable> notification = Sets.newHashSet();
			while(links.hasNext()) {
				TransverseLink link = links.next();
				if(storage == null) {
					project = link.getProject();
					storage = getStorage(project, provider);
					storage.startTransaction();
				}
				Reachable source = Iterables.get(link.getSources(), 0);
				Reachable target = Iterables.get(link.getTargets(), 0);
				if(storage != null) {
					storage.removeUpwardRelationShip(link.getKind(), null, target, source);
					notification.add(link.getId());
					notification.add(source);
					notification.add(target);
					notification.add(link.getId().trimFragment());
				}
			}
			if(storage != null) {
				storage.commit();
				listenerManager.notifyChanged(notification.toArray(new Reachable[]{}));
			}
		} catch (Exception e) {
			e.printStackTrace();
			storage.rollback();
		} finally {
			storage.save();
			refreshViewerData();
			IFile file = project.getFile(new Path(RDF_FILE));
			try {
				file.getParent().refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
			} catch (CoreException e) {
				//DO NOTHING
			}
		}
	}

	protected static ITraceabilityStorage getStorage(IProject project, IStorageProvider provider) {
		IFile file = project.getFile(new Path(RDF_FILE));
		if(file != null && file.exists()) {
			String uri = file.getLocationURI().getPath();
			return provider.getStorage(uri);
		}
		return null;
	}


	/**
	 * Refreshes the viewer's data.
	 */
	public void refreshViewerData() {
		Predicate<Object> filter = new Predicate<Object>() {

			@Override
			public boolean apply(Object arg0) {
				ViewerFilter[] filters = TableController.this.viewer.getFilters();
				for(int i = 0; i < filters.length; i++) {
					ViewerFilter filter = filters[i];
					if(!filter.select(viewer, null, arg0)) {
						return false;
					}
				}
				return true;
			}
		};
		Iterable<?> input;
		try {
			
			input = callable.call();
			
			
			
			Iterable<?> filtered = Iterables.filter(input, filter);
			long start = new Date().getTime();
			int count= Iterables.size(filtered);
			viewer.setItemCount(count);
			long duration = new Date().getTime()-start;
			
			viewer.setInput(filtered);
			
			viewer.refresh();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void refreshViewerVisuals() {
		TraceabilityLazyContentProvider<?> contentProvider = (TraceabilityLazyContentProvider<?>)viewer.getContentProvider();
		viewer.setItemCount(Iterables.size((Iterable<?>)viewer.getInput()));
		contentProvider.clearCache();
		viewer.refresh();
	}


}
