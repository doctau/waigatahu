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

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

import com.redhat.gss.waigatahu.cases.core.client.RhcpClient;

public class CaseAttachmentHandler extends AbstractTaskAttachmentHandler {
	private final CaseRepositoryConnector connector;

	public CaseAttachmentHandler(CaseRepositoryConnector connector) {
		this.connector = connector;
	}

	public boolean canGetContent(TaskRepository repository, ITask task) {
		//FIXME
		return true;
	}

	public boolean canPostContent(TaskRepository repository, ITask task) {
		//FIXME
		return true;
	}

	public InputStream getContent(TaskRepository repository, ITask task,
			TaskAttribute attachmentAttribute, IProgressMonitor monitor)
			throws CoreException {
		CaseId caseId = connector.taskIdToCaseUrl(task.getTaskId());
		String url = attachmentAttribute.getAttribute(TaskAttribute.ATTACHMENT_URL).getValue();
		String attachmentId = null; //FIXME: attachmentAttribute.getAttribute(TaskAttribute.ATTACHMENT_ID).getValue();

		RhcpClient client = connector.getClient(repository);
		return client.streamAttachment(caseId, attachmentId, url, monitor);
	}

	public void postContent(TaskRepository repository, ITask task,
			AbstractTaskAttachmentSource source, String comment,
			TaskAttribute attachmentAttribute, IProgressMonitor monitor)
			throws CoreException {
		CaseId caseId = connector.taskIdToCaseUrl(task.getTaskId());

		RhcpClient client = connector.getClient(repository);
		client.postAttachment(caseId, comment, attachmentAttribute.getAttribute(TaskAttribute.ATTACHMENT_FILENAME), source, monitor);
	}
}
