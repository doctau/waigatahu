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

package com.redhat.gss.waigatahu.cases.core;

import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;

import com.redhat.gss.waigatahu.cases.data.CaseAttribute;

// Maps TaskData to ITasks
public class CaseTaskMapper extends TaskMapper {
	private final CaseRepositoryConnector connector;

	public CaseTaskMapper(TaskData taskData, CaseRepositoryConnector connector) {
		super(taskData);
		this.connector = connector;
	}

	public boolean applyTo(ITask task) {
		boolean ret =  super.applyTo(task);
		TaskData taskData = getTaskData();

		// TODO: map custom things from taskData to task
		//TaskAttribute.
		task.setAttribute(CaseAttribute.WEB_URL, taskData.getRoot().getAttribute(CaseAttribute.WEB_URL).getValue());

		
		task.setUrl(connector.taskIdToCaseUrl(taskData.getTaskId()).getUrl());
		return ret;
	}

	public boolean hasChanges(ITask task) {
		if (super.hasChanges(task))
			return true;

		// TODO: add other attributes
		return false;
	}	
}
