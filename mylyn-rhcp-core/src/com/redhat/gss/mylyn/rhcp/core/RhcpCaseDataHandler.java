package com.redhat.gss.mylyn.rhcp.core;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import com.redhat.gss.mylyn.rhcp.core.client.RhcpClient;
import com.redhat.gss.mylyn.rhcp.core.data.RhcpSupportCase;

public class RhcpCaseDataHandler extends AbstractTaskDataHandler {
	private final String TASK_DATA_VERSION = "1";
	
	private final RhcpCaseRepositoryConnector connector;

	public RhcpCaseDataHandler(RhcpCaseRepositoryConnector connector) {
		this.connector = connector;
	}

	public RepositoryResponse postTaskData(TaskRepository repository,
			TaskData taskData, Set<TaskAttribute> oldAttributes,
			IProgressMonitor monitor) throws CoreException {
		throw new IllegalArgumentException();
	}

	public boolean initializeTaskData(TaskRepository repository, TaskData data,
			ITaskMapping initializationData, IProgressMonitor monitor)
			throws CoreException {
		throw new IllegalArgumentException();
	}

	public TaskAttributeMapper getAttributeMapper(TaskRepository repository) {
		throw new IllegalArgumentException();
	}

	public TaskData getTaskData(TaskRepository repository, String taskId,
			IProgressMonitor monitor) throws CoreException {
		IProgressMonitor myMonitor = Policy.monitorFor(monitor);
		try {
			myMonitor.beginTask("Task Download", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
			return downloadTaskData(repository, connector.getCaseId(taskId), myMonitor);
		} finally {
			myMonitor.done();
		}
	}


	public TaskData downloadTaskData(TaskRepository repository, long caseId, IProgressMonitor monitor)
			throws CoreException {
		RhcpClient client = connector.getTracClient(repository);
		RhcpSupportCase supportCase = client.getCase(caseId, monitor);

		return createTaskDataFromCase(client, repository, supportCase, monitor);
	}
	
	public TaskData createTaskDataFromCase(RhcpClient client, TaskRepository repository,
			RhcpSupportCase supportCase, IProgressMonitor monitor) throws CoreException {
		TaskData taskData = new TaskData(getAttributeMapper(repository), RhcpCorePlugin.CONNECTOR_KIND,
				repository.getRepositoryUrl(), supportCase.getCaseNumber() + ""); //$NON-NLS-1$
		taskData.setVersion(TASK_DATA_VERSION);
		createDefaultAttributes(taskData, client);
		//TODO: updateTaskData(repository, taskData, supportCase);
		return taskData;
	}

	private void createDefaultAttributes(TaskData taskData, RhcpClient client) {
		// TODO Auto-generated method stub
		
	}
}
