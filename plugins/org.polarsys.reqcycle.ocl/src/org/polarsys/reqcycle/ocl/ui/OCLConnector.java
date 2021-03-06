/*******************************************************************************
 *  Copyright (c) 2013, 2014 AtoS and others
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html *
 *  Contributors:
 *    Olivier Melois (AtoS) - initial API and implementation and/or initial documentation
 *    Raphael Faudou (Samares Engineering) - Fixed some bugs in OCL connector to manage types and align
 * 		connector on other connectors with a destination file
 *******************************************************************************/
package org.polarsys.reqcycle.ocl.ui;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.polarsys.reqcycle.ocl.utils.OCLUtilities;
import org.polarsys.reqcycle.repository.connector.ui.wizard.IConnectorWizard;
import org.polarsys.reqcycle.repository.data.IDataManager;
import org.polarsys.reqcycle.repository.data.IDataModelManager;
import org.polarsys.reqcycle.repository.data.MappingModel.MappingElement;
import org.polarsys.reqcycle.repository.data.RequirementSourceConf.RequirementSource;
import org.polarsys.reqcycle.repository.data.RequirementSourceData.AbstractElement;
import org.polarsys.reqcycle.repository.data.RequirementSourceData.Requirement;
import org.polarsys.reqcycle.repository.data.RequirementSourceData.RequirementsContainer;
import org.polarsys.reqcycle.repository.data.RequirementSourceData.Section;
import org.polarsys.reqcycle.repository.data.ScopeConf.Scope;
import org.polarsys.reqcycle.repository.data.types.IAttribute;
import org.polarsys.reqcycle.repository.data.types.IDataModel;
import org.polarsys.reqcycle.repository.data.types.IRequirementType;
import org.polarsys.reqcycle.repository.data.util.IRequirementSourceProperties;
import org.polarsys.reqcycle.utils.ocl.OCLEvaluator;
import org.polarsys.reqcycle.utils.ocl.ZigguratOCLPlugin;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class OCLConnector extends Wizard implements IConnectorWizard, Listener {

	protected SettingBean bean = new SettingBean();

	private RequirementSource requirementSource = null;

	@Inject
	IDataModelManager manager;

	@Inject
	IDataManager dataManager;

	@Override
	public void addPages() {
		addPage(new SettingPage(bean));
		addPage(new OCLPage(bean));
		super.addPages();
	}

	@Override
	public Callable<RequirementSource> createRequirementSource() {
		return new Callable<RequirementSource>() {
			Map<EObject, Section> sections = Maps.newHashMap();
			@Override
			public RequirementSource call() throws Exception {
				RequirementSource result = null;
				if(OCLConnector.this.requirementSource != null) {
					result = OCLConnector.this.requirementSource;
				} else {
					result = dataManager.createRequirementSource();
					// RFU add ReqContainer based on req source destination file
					RequirementsContainer rc = dataManager.createRequirementsContainer(URI.createPlatformResourceURI(bean.getDestination(), true));
					result.setContents(rc);
				}
				result.setProperty(IRequirementSourceProperties.PROPERTY_URI, bean.getUri());
				fillRequirements(result);
				return result;
			}
			
			protected void fillRequirements(RequirementSource requirementSource) throws Exception {
				requirementSource.clearContent();
				Collection<MappingElement> mapping = requirementSource.getMappings();
				ResourceSet resourceSet = new ResourceSetImpl();

				String repositoryUri = requirementSource.getRepositoryUri();

				Resource resource = resourceSet.getResource(URI.createPlatformResourceURI(repositoryUri, true), true);
				OCLEvaluator evaluator = ZigguratOCLPlugin.compileOCL(resourceSet, URI.createPlatformResourceURI(bean.getOclUri(), true));
				TreeIterator<EObject> contents = resource.getAllContents();
				Collection<IRequirementType> requirementTypes = bean.getDataPackage().getRequirementTypes();
				while(contents.hasNext()) {
					EObject eObject = contents.next();
					if (OCLUtilities.isSection(evaluator,eObject)){
						String id = (String) OCLUtilities.getAttributeValue(evaluator, eObject, "getId", EcorePackage.Literals.ESTRING);
						String name = (String) OCLUtilities.getAttributeValue(evaluator, eObject, "getName", EcorePackage.Literals.ESTRING);
						Section section = dataManager.createSection(id, name, "");
						sections.put(eObject, section);
						addToSection(requirementSource, eObject, section);
					}
					for(IRequirementType reqType : requirementTypes) {
						if(OCLUtilities.isDataType(evaluator, eObject, reqType)) {
							AbstractElement requirement = createRequirement(evaluator, mapping, eObject, reqType);
							addToSection(requirementSource, eObject, requirement);
						}
					}
				}
			}

			private void addToSection(RequirementSource requirementSource,
					EObject eObject, AbstractElement current) {
				Section container = null;
				EObject tmp = eObject;
				while (container == null && tmp.eContainer() != null){
					container = sections.get(tmp.eContainer());
					tmp = tmp.eContainer();
				}
				if (container == null){
					dataManager.addElementsToSource(requirementSource, current);
				}
				else {
					dataManager.addElementsToSection(container, current);
				}
			}
		};
	}

	protected AbstractElement createRequirement(OCLEvaluator evaluator, Collection<MappingElement> mappings, EObject eObject, IRequirementType reqType) throws Exception {
		Requirement requirement = reqType.createInstance();
		for(IAttribute attribute : Iterables.filter(reqType.getAttributes(), IAttribute.class)) {
			Object value = OCLUtilities.getAttributeValue(evaluator, eObject, attribute);
			if(value != null) {
				dataManager.addAttribute(requirement, attribute, value);
			}
		}
		return requirement;
	}

	@Override
	public void initializeWithRequirementSource(RequirementSource requirementSource) {
		this.requirementSource = requirementSource;
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	protected class SettingBean {

		private String uri = "";

		private String oclUri = "";

		private IDataModel dataPackage;

		private Scope scope;
		
		private String destination;
		
		//FIXME add listener to change destination file
		//private Listener listenerDestination;

		public SettingBean() {
		}

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
			notifyChange();
		}

		public String getOclUri() {
			return oclUri;
		}

		public void setOclUri(String oclUri) {
			this.oclUri = oclUri;
			notifyChange();
		}

		public IDataModel getDataPackage() {
			return dataPackage;
		}

		public void setDataPackage(IDataModel dataPackage) {
			this.dataPackage = dataPackage;
			notifyChange();
		}

		public Scope getScope() {
			return scope;
		}

		public void setScope(Scope scope) {
			this.scope = scope;
			notifyChange();
		}

		public void notifyChange() {
			IWizardPage[] pages = getPages();
			if(pages != null) {
				for(int i = 0; i < pages.length; i++) {
					IWizardPage iWizardPage = pages[i];
					iWizardPage.getWizard().getContainer().updateButtons();
					if(iWizardPage instanceof Listener) {
						((Listener)iWizardPage).handleEvent(new Event());
					}
				}
			}
		}

		public String getDestination() {
			return destination;
		}

		public void setDestination(String destination) {
			this.destination = destination;
			notifyChange();
		
		}
	}

	@Override
	public boolean canFinish() {
		return bean != null && bean.getOclUri() != null && bean.getDataPackage() != null && bean.getUri() != null 
				&& !bean.getOclUri().isEmpty() && !bean.getUri().isEmpty()
				&& !bean.getDestination().isEmpty();
	}

	@Override
	public void handleEvent(Event event) {
		getContainer().updateButtons();
	}

	@Override
	public void init(ISelection selection) {
		// TODO Auto-generated method stub
	}

}
