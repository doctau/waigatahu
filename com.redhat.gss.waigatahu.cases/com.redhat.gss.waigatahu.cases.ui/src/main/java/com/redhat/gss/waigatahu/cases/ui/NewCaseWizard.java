package com.redhat.gss.waigatahu.cases.ui;

import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;

public class NewCaseWizard extends NewTaskWizard {
	public NewCaseWizard(TaskRepository taskRepository,
			ITaskMapping taskSelection) {
		super(taskRepository, taskSelection);
	}

	public void addPages() {
		// no pages, so create the task immediately
	}

	protected ITaskMapping getInitializationData() {
		return null;
	}
}
