/*******************************************************************************
 *  Copyright (c) 2013 AtoS
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html *
 *  Contributors:
 *    Tristan Faure (AtoS) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.polarsys.reqcycle.traceability.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.polarsys.reqcycle.uri.model.Reachable;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

public class Link {
	Set<Reachable> sources = new HashSet<Reachable>();
	Set<Reachable> targets = new HashSet<Reachable>();
	TType kind;
	Reachable id;

	public Link(Reachable id, TType kind, Iterable<Reachable> sources,
			Iterable<Reachable> targets) {
		this.id = id;
		this.kind = kind;
		this.sources = Sets.newHashSet(sources);
		this.targets = Sets.newHashSet(targets);
	}

	public Link(Reachable id, TType kind, Reachable source, Reachable target) {
		this(id, kind, Collections.singleton(source), Collections
				.singleton(target));
	}

	public Reachable getId() {
		return id;
	}

	public String getLabel() {
		String label = kind.getLabel();
		if (kind.getSuperType() != null) {
			label += " [" + kind.getSuperType().getLabel() + "]";
		}
		return label;
	}

	public TType getKind() {
		return kind;
	}

	public Set<Reachable> getSources() {
		return Collections.unmodifiableSet(sources);
	}

	public Set<Reachable> getTargets() {
		return Collections.unmodifiableSet(targets);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Link) {
			Link link = (Link) obj;
			return Objects.equal(sources, link.sources)
					&& Objects.equal(targets, link.targets)
					&& Objects.equal(kind, link.kind);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(sources, targets, kind);
	}

}
