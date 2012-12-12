package com.redhat.gss.mylyn.rhcp.core;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

import com.redhat.gss.mylyn.rhcp.core.client.RhcpClient;

public class RhcpCaseAttachmentHandler extends AbstractTaskAttachmentHandler {
	private final RhcpCaseRepositoryConnector connector;

	public RhcpCaseAttachmentHandler(RhcpCaseRepositoryConnector connector) {
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
		String url = attachmentAttribute.getAttribute(TaskAttribute.ATTACHMENT_URL).getValue();
		String attachmentId = null; //FIXME: attachmentAttribute.getAttribute(TaskAttribute.ATTACHMENT_ID).getValue();
		String caseId = task.getTaskId();

		RhcpClient client = connector.getClientFactory().getClient(repository);
		return client.streamAttachment(caseId, attachmentId, url, monitor);
	}

	public void postContent(TaskRepository repository, ITask task,
			AbstractTaskAttachmentSource source, String comment,
			TaskAttribute attachmentAttribute, IProgressMonitor monitor)
			throws CoreException {
		String caseId = task.getTaskId();

		RhcpClient client = connector.getClientFactory().getClient(repository);
		client.postAttachment(caseId, comment, attachmentAttribute.getAttribute(TaskAttribute.ATTACHMENT_FILENAME), source, monitor);
	}
}
