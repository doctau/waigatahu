package com.redhat.gss.waigatahu.cases.ui.editor;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
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
}
