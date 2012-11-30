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
import com.redhat.gss.mylyn.rhcp.core.client.RhcpClientFactory;
import com.redhat.gss.mylyn.rhcp.core.client.RhcpClientFactoryImpl;

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
		return true;
	}

	public IStatus performQuery(TaskRepository repository,
			IRepositoryQuery query, TaskDataCollector collector,
			ISynchronizationSession session, IProgressMonitor monitor) {
		return taskDataHandler.performQuery(repository, query, collector, session, monitor);
	}
	
	public boolean canCreateNewTask(TaskRepository repository) {
		RhcpClient client = getClientFactory().getClient(repository);
		return client.canCreateCases();
	}

	public boolean hasTaskChanged(TaskRepository taskRepository, ITask task, TaskData taskData) {
		return getTaskMapping(taskData).hasChanges(task);
	}

	public void updateTaskFromTaskData(TaskRepository repository, ITask task, TaskData taskData) {
		getTaskMapping(taskData).applyTo(task);
	}


	public RhcpTaskMapper getTaskMapping(TaskData taskData) {
		TaskRepository repository = taskData.getAttributeMapper().getTaskRepository();
		RhcpClient client = getClientFactory().getClient(repository);
		return new RhcpTaskMapper(taskData, client);
	}

	public RhcpClientFactory getClientFactory() {
		return new RhcpClientFactoryImpl();
	}

	public String getRepositoryUrlFromTaskUrl(String taskUrl) {
		return getClientFactory().getRepositoryUrlFromCaseUrl(taskUrl);
	}

	public String getTaskIdFromTaskUrl(String taskUrl) {
		return Long.toString(getClientFactory().getCaseNumberFromCaseUrl(taskUrl));
	}

	public String getTaskUrl(String repositoryUrl, String taskId) {
		return getClientFactory().getCaseUrl(repositoryUrl, Long.parseLong(taskId));
	}

	public void updateRepositoryConfiguration(TaskRepository taskRepository,
			IProgressMonitor monitor) throws CoreException {
		throw new IllegalArgumentException();
	}
}
