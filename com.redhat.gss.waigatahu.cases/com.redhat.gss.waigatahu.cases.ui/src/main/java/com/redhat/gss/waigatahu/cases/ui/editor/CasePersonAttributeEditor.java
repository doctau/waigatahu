/*******************************************************************************
* Copyright (c) 2012 Red Hat, Inc.
* Distributed under license by Red Hat, Inc. All rights reserved.
* This program is made available under the terms of the
* Eclipse Public License v1.0 which accompanies this distribution,
* and is available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Red Hat, Inc. - initial API and implementation
******************************************************************************/

package com.redhat.gss.waigatahu.cases.ui.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.editors.PersonAttributeEditor;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.redhat.gss.waigatahu.cases.data.PersonAttribute;

public class CasePersonAttributeEditor extends PersonAttributeEditor {
	private Label imageLabel;

	public CasePersonAttributeEditor(TaskDataModel manager,
			TaskAttribute taskAttribute) {
		super(manager, taskAttribute);
	}

	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayout parentLayout = new GridLayout(2, false);
		parentLayout.marginHeight = 0;
		parentLayout.marginWidth = 0;
		parentLayout.horizontalSpacing = 0;
		composite.setLayout(parentLayout);

		// add the logo if they are a RH person
		imageLabel = new Label(composite, SWT.NONE);
		updateImage();

		// add the normal person stuff, and give it the most space
		super.createControl(composite, toolkit);
		Control personControl = getControl();
		GridDataFactory.fillDefaults().grab(true, false).applyTo(personControl);
		//toolkit.paintBordersFor(composite);
		setControl(composite);
	}

	@Override
	public void setValue(String text) {
		super.setValue(text);
		updateImage();
	}

	protected void updateImage() {
		TaskAttribute redhatAttr = getTaskAttribute().getMappedAttribute(PersonAttribute.IS_REDHAT);
		boolean isRedHat = (redhatAttr != null) ? getAttributeMapper().getBooleanValue(redhatAttr) : false;

		// are they a RH person?
		if (isRedHat)
			imageLabel.setImage(CommonImages.getImage(CommonImages.WARNING));
		else 
			imageLabel.setImage(null);
	}
}
