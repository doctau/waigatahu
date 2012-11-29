package com.redhat.gss.mylyn.rhcp.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;

import com.redhat.gss.mylyn.rhcp.core.client.RhcpClient;

public class RhcpCaseRepositoryConnector extends AbstractRepositoryConnector {
	private static final String LABEL = "Red Hat Customer Portal";
	
	private final RhcpCaseDataHandler taskDataHandler = new RhcpCaseDataHandler(this);

	public String getConnectorKind() {
		return RhcpCorePlugin.CONNECTOR_KIND;
	}

	public boolean canCreateTaskFromKey(TaskRepository repository) {
		return true;
	}

	public TaskData getTaskData(TaskRepository repository, String taskId,
			IProgressMonitor monitor) throws CoreException {
		return taskDataHandler.getTaskData(repository, taskId, monitor);
	}

	public String getLabel() {
		return LABEL;
	}
	
	public boolean canQuery() {
		//FIXME: add support later
		return false;
	}
	


	/*
	 * Convert a taskId into a case number
	 */
	public long getCaseId(String taskId) {
		throw new IllegalArgumentException();
	}

	public RhcpClient getTracClient(TaskRepository repository) {
		throw new IllegalArgumentException();
	}
	

	
	public boolean canCreateNewTask(TaskRepository repository) {
		return false;
	}

	public String getRepositoryUrlFromTaskUrl(String taskFullUrl) {
		throw new IllegalArgumentException();
	}

	public String getTaskIdFromTaskUrl(String taskFullUrl) {
		throw new IllegalArgumentException();
	}

	public String getTaskUrl(String repositoryUrl, String taskId) {
		throw new IllegalArgumentException();
	}

	public boolean hasTaskChanged(TaskRepository taskRepository, ITask task, TaskData taskData) {
		throw new IllegalArgumentException();
	}

	public IStatus performQuery(TaskRepository repository,
			IRepositoryQuery query, TaskDataCollector collector,
			ISynchronizationSession session, IProgressMonitor monitor) {
		throw new IllegalArgumentException();
	}

	public void updateRepositoryConfiguration(TaskRepository taskRepository,
			IProgressMonitor monitor) throws CoreException {
		throw new IllegalArgumentException();
		
	}

	public void updateTaskFromTaskData(TaskRepository taskRepository, ITask task, TaskData taskData) {
		throw new IllegalArgumentException();
	}
}
