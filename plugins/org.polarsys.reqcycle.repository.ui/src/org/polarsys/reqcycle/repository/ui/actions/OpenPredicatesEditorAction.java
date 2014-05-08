/*******************************************************************************
 * * Copyright (c) 2013 AtoS
 * * All rights reserved. This program and the accompanying materials
 * * are made available under the terms of the Eclipse Public License v1.0
 * * which accompanies this distribution, and is available at
 * * http://www.eclipse.org/legal/epl-v10.html *
 * * Contributors:
 * * Anass Radouani (AtoS) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.polarsys.reqcycle.repository.ui.actions;

import org.eclipse.jface.action.Action;
import org.polarsys.reqcycle.predicates.ui.util.PredicatesUIHelper;

public class OpenPredicatesEditorAction extends Action {

	public OpenPredicatesEditorAction() {
	}

	@Override
	public void run() {
		PredicatesUIHelper.openNewPredicateEditor();
	}


}