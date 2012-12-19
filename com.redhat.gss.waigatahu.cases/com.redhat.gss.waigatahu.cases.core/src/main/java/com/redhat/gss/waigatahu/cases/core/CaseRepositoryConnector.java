package com.redhat.gss.waigatahu.cases.core;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;

import com.redhat.gss.waigatahu.cases.core.client.RhcpClient;
import com.redhat.gss.waigatahu.cases.core.client.RhcpClientFactory;
import com.redhat.gss.waigatahu.cases.core.client.RhcpClientFactoryImpl;

public class CaseRepositoryConnector extends AbstractRepositoryConnector implements RhcpClientFactory {
	private static final String LABEL = "Red Hat Customer Portal";
	
	private final CaseDataHandler taskDataHandler = new CaseDataHandler(this);
	private final AbstractTaskAttachmentHandler taskAttachmentHandler = new CaseAttachmentHandler(this);

	private final RhcpClientFactory factory = new RhcpClientFactoryImpl();
	private final Map<TaskRepository, RhcpClient> clients = new HashMap<TaskRepository, RhcpClient>();

	public String getConnectorKind() {
		return WaigatahuCaseCorePlugin.CONNECTOR_KIND;
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
		return getClient(repository).canCreateCases();
	}

	public boolean hasTaskChanged(TaskRepository taskRepository, ITask task, TaskData taskData) {
		return getTaskMapping(taskData).hasChanges(task);
	}

	public void updateTaskFromTaskData(TaskRepository repository, ITask task, TaskData taskData) {
		getTaskMapping(taskData).applyTo(task);
	}


	public CaseTaskMapper getTaskMapping(TaskData taskData) {
		return new CaseTaskMapper(taskData, this);
	}


	public RhcpClient getClient(TaskRepository repository) {
		synchronized (this) {
			RhcpClient ref = clients.get(repository);
			if (ref != null)
				return ref;
		}
		RhcpClient client = factory.getClient(repository);
		synchronized (this) {
			RhcpClient old = clients.put(repository, client);
			if (old != null) {
				// revert
				 clients.put(repository, old);
				 return old;
			} else {
				return client;
			}
		}
	}
	

	/*
	 * Task ID handling
	 */
	public String getRepositoryUrlFromTaskUrl(String taskUrl) {
		throw new IllegalArgumentException();
	}

	public String getTaskUrl(String repositoryUrl, String taskId) {
		return taskIdToCaseUrl(taskId).getUrl();
	}
	public String getTaskIdFromTaskUrl(String taskUrl) {
		return "rhcp:" + taskUrl;
	}

	public CaseId taskIdToCaseUrl(String taskId) {
		if (!taskId.startsWith("rhcp:"))
			throw new IllegalArgumentException();
		return new CaseId(taskId.substring("rhcp:".length()));
	}

	public void updateRepositoryConfiguration(TaskRepository taskRepository,
			IProgressMonitor monitor) throws CoreException {
		RhcpClient client = getClient(taskRepository);
		client.updateData();
	}

	

	public AbstractTaskDataHandler getTaskDataHandler() {
		return taskDataHandler;
	}

	public AbstractTaskAttachmentHandler getTaskAttachmentHandler() {
		return taskAttachmentHandler;
	}
}
