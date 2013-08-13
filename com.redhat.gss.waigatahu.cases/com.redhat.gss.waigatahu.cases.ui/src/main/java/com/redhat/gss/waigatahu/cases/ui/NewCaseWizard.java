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
