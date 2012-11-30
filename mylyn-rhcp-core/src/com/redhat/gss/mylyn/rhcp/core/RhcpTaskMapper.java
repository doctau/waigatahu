package com.redhat.gss.mylyn.rhcp.core;

import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;

import com.redhat.gss.mylyn.rhcp.core.client.RhcpClient;

public class RhcpTaskMapper extends TaskMapper {
	private final RhcpClient client;

	public RhcpTaskMapper(TaskData taskData, RhcpClient client) {
		super(taskData);
		this.client = client;
	}

	public boolean applyTo(ITask task) {
		boolean ret =  super.applyTo(task);
		TaskData taskData = getTaskData();

		// TODO: map custom things from taskData to task
		//TaskAttribute.

		task.setUrl(client.getCaseUrl(taskData.getRepositoryUrl(), Long.parseLong(getTaskData().getTaskId())));
		return ret;
	}

	public boolean hasChanges(ITask task) {
		if (super.hasChanges(task))
			return true;

		// TODO: add other attributes
		return false;
	}	
}
