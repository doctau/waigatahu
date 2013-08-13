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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;

import com.redhat.gss.waigatahu.cases.core.client.RhcpClient;
import com.redhat.gss.waigatahu.cases.core.client.RhcpClientFactory;
import com.redhat.gss.waigatahu.cases.core.client.RhcpClientFactoryImpl;
import com.redhat.gss.waigatahu.common.client.CustomerPortalClient;
import com.redhat.gss.waigatahu.common.client.LoginException;

public class CaseRepositoryConnector extends AbstractRepositoryConnector implements RhcpClientFactory {
	private static final String LABEL = "Red Hat Customer Portal";
	
	private final CaseDataHandler taskDataHandler = new CaseDataHandler(this);
	private final AbstractTaskAttachmentHandler taskAttachmentHandler = new CaseAttachmentHandler(this);

	private final RhcpClientFactory factory = new RhcpClientFactoryImpl(new CustomerPortalClient());
	private final Map<TaskRepository, RhcpClient> clients = new HashMap<TaskRepository, RhcpClient>();

	public String getConnectorKind() {
		return WaigatahuCaseCorePlugin.CONNECTOR_KIND;
	}

	public void shutdown() {
		synchronized (this) {
			for (RhcpClient c: clients.values()) {
				c.shutdown();
			}
			clients.clear();
		}
	}
	
	/* FIXME: Complete hack to allow other plugisn access to the authentication */
	public synchronized AbstractWebLocation getWebLocation() {
		Iterator<Entry<TaskRepository, RhcpClient>> it = clients.entrySet().iterator();
		TaskRepository repository = it.next().getKey();
		return new TaskRepositoryLocationFactory().createWebLocation(repository);
	}

	public boolean canCreateTaskFromKey(TaskRepository repository) {
		return true;
	}

	public TaskData getTaskData(TaskRepository repository, String taskId,
			IProgressMonitor monitor) throws CoreException {
		try {
			return taskDataHandler.getTaskData(repository, taskId, monitor);
		} catch (LoginException e) {
			throw new CoreException(exceptionToStatus(repository, e));
		}
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
		try {
			return taskDataHandler.performQuery(repository, query, collector, session, monitor);
		} catch (LoginException e) {
			return exceptionToStatus(repository, e);
		}
	}
	
	public boolean canCreateNewTask(TaskRepository repository) {
		try {
			return getClient(repository).canCreateCases();
		} catch (LoginException e) {
			throw new RuntimeException(new CoreException(exceptionToStatus(repository, e)));
		}
	}

	public boolean hasTaskChanged(TaskRepository taskRepository, ITask task, TaskData taskData) {
		try {
			return getTaskMapping(taskData).hasChanges(task);
		} catch (LoginException e) {
			throw new RuntimeException(new CoreException(exceptionToStatus(taskRepository, e)));
		}
	}

	public void updateTaskFromTaskData(TaskRepository repository, ITask task, TaskData taskData) {
		try {
			getTaskMapping(taskData).applyTo(task);
		} catch (LoginException e) {
			throw new RuntimeException(new CoreException(exceptionToStatus(repository, e)));
		}
	}

	private IStatus exceptionToStatus(TaskRepository repository, Exception e) {
		if (e instanceof LoginException) {
			return RepositoryStatus.createLoginError(repository.getRepositoryUrl(), WaigatahuCaseCorePlugin.CONNECTOR_KIND);
		} else {
			return new RepositoryStatus(IStatus.ERROR, WaigatahuCaseCorePlugin.CONNECTOR_KIND, RepositoryStatus.ERROR_INTERNAL, "FAILED!", e);
		}
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
		client.updateData(monitor);
	}

	public AbstractTaskDataHandler getTaskDataHandler() {
		return taskDataHandler;
	}

	public AbstractTaskAttachmentHandler getTaskAttachmentHandler() {
		return taskAttachmentHandler;
	}
}
