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
package org.polarsys.reqcycle.ui.numberspropseditor.internal;

import org.polarsys.reqcycle.ui.eattrpropseditor.api.AbstractPropsEditor;
import org.polarsys.reqcycle.ui.eattrpropseditor.api.AbstractPropsEditorComponent;
import org.polarsys.reqcycle.ui.numberspropseditor.internal.components.FloatPropsEditorComponent;

public class FloatPropsEditor extends AbstractPropsEditor<Float> {

	@Override
	protected AbstractPropsEditorComponent<Float> initAndGetComponent() {
		return new FloatPropsEditorComponent(getAttributeName(), getContainer(), getStyle());
	}

}
