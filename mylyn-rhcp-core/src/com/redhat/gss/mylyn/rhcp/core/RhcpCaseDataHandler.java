package com.redhat.gss.mylyn.rhcp.core;

import java.util.Collection;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;

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
		return new RhcpAttributeMapper(repository);
	}

	public TaskData getTaskData(TaskRepository repository, String taskId,
			IProgressMonitor monitor) throws CoreException {
		IProgressMonitor myMonitor = Policy.monitorFor(monitor);
		try {
			myMonitor.beginTask("Task Download", IProgressMonitor.UNKNOWN);
			RhcpClient client = connector.getClientFactory().getClient(repository);
			return downloadTaskData(client, repository, Long.parseLong(taskId), myMonitor);
		} finally {
			myMonitor.done();
		}
	}

	public IStatus performQuery(TaskRepository repository,
			IRepositoryQuery query, TaskDataCollector collector,
			ISynchronizationSession session, IProgressMonitor monitor) {
		IProgressMonitor myMonitor = Policy.monitorFor(monitor);
		try {
			myMonitor.beginTask("Running query", IProgressMonitor.UNKNOWN);
			RhcpClient client = connector.getClientFactory().getClient(repository);

			Collection<RhcpSupportCase> cases;

			//TODO: check the query type
			cases = client.getAllOpenCases(client, monitor);

			for (RhcpSupportCase c: cases) {
				TaskData task = createTaskDataFromCase(client, repository, c, monitor);
				collector.accept(task);
			}
		} catch (CoreException e) {
			return RepositoryStatus.createInternalError(RhcpCorePlugin.CONNECTOR_KIND, "ERROR!", e);
		} finally {
			myMonitor.done();
		}
		return Status.OK_STATUS;
	}

	private TaskData downloadTaskData(RhcpClient client, TaskRepository repository,
			long caseId, IProgressMonitor monitor)
			throws CoreException {
		RhcpSupportCase supportCase = client.getCase(caseId, monitor);
		return createTaskDataFromCase(client, repository, supportCase, monitor);
	}
	
	private TaskData createTaskDataFromCase(RhcpClient client, TaskRepository repository,
			RhcpSupportCase supportCase, IProgressMonitor monitor) throws CoreException {
		TaskData taskData = new TaskData(getAttributeMapper(repository), RhcpCorePlugin.CONNECTOR_KIND,
				repository.getRepositoryUrl(), supportCase.getCaseNumber() + "");
		taskData.setVersion(TASK_DATA_VERSION);
		createDefaultAttributes(taskData, client);
		updateTaskData(taskData, supportCase);
		return taskData;
	}

	private void createDefaultAttributes(TaskData taskData, RhcpClient client) {
		taskData.getRoot().createAttribute("x").setValue("y");
		
	}

	private void updateTaskData(TaskData taskData, RhcpSupportCase supportCase) {
		// TODO Auto-generated method stub
		
	}
}
