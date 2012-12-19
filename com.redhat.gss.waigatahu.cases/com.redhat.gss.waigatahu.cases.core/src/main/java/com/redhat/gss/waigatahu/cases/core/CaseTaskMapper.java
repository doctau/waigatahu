package com.redhat.gss.waigatahu.cases.core;

import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;

import com.redhat.gss.waigatahu.cases.core.client.RhcpClient;

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
