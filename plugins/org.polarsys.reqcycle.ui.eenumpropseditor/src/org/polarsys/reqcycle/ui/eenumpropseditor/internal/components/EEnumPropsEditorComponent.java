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
package org.polarsys.reqcycle.ui.eenumpropseditor.internal.components;

import java.util.Collection;

import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.polarsys.reqcycle.ui.eattrpropseditor.api.AbstractPropsEditorComponent;

public class EEnumPropsEditorComponent extends AbstractPropsEditorComponent<String> {

	private ComboViewer comboViewer;

	public EEnumPropsEditorComponent(String attributeName, Collection<Object> possibleValues, Composite parent, int style) {
		super(String.class, parent, style);
		setLayout(new GridLayout(2, false));

		Label lblName = new Label(this, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblName.setText(attributeName);

		comboViewer = new ComboViewer(this);
		Combo combo = comboViewer.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		comboViewer.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(Object element) {
				if(element instanceof EEnumLiteral) {
					return ((EEnumLiteral)element).getLiteral();
				}
				return super.getText(element);
			}
		});
		comboViewer.setInput(possibleValues);
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if(comboViewer.getSelection() instanceof IStructuredSelection && !comboViewer.getSelection().isEmpty()) {
					IStructuredSelection selection = (IStructuredSelection)comboViewer.getSelection();
					final Object selectedElement = selection.getFirstElement();
					if(selectedElement instanceof String) {
						setValue((String) selectedElement);
					}
				}
			}
		});
	}

	@Override
	public boolean isValid() {
		return true;
	}

}
