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

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.ui.services.IServiceLocator;

public class CaseAttributeEditorFactory extends AttributeEditorFactory {
	private final TaskDataModel model;

	public CaseAttributeEditorFactory(TaskDataModel model,
			TaskRepository taskRepository) {
		super(model, taskRepository);
		this.model = model;
	}

	public CaseAttributeEditorFactory(TaskDataModel model,
			TaskRepository taskRepository, IServiceLocator serviceLocator) {
		super(model, taskRepository, serviceLocator);
		this.model = model;
	}
	
	public AbstractAttributeEditor createEditor(String type, TaskAttribute taskAttribute) {
		if (TaskAttribute.TYPE_PERSON.equals(type)) {
			return new CasePersonAttributeEditor(model, taskAttribute);
		} else if (TaskAttribute.TYPE_COMMENT.equals(type)) {
			// why doesn't AttributeEditorFactory know about this? FIXME: upstream bug?
			return super.createEditor(TaskAttribute.TYPE_LONG_RICH_TEXT, taskAttribute);
		} else {
			return super.createEditor(type, taskAttribute);
		}
	}
}
