package com.redhat.gss.mylyn.rhcp.core;

import java.util.Collection;
import java.util.List;
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
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;

import com.redhat.gss.mylyn.rhcp.core.client.RhcpClient;
import com.redhat.gss.strata.model.Case;
import com.redhat.gss.strata.model.Comment;
import com.redhat.gss.strata.model.Link;

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

			Collection<Case> cases;

			//TODO: check the query type
			cases = client.getAllOpenCases(client, monitor);

			for (Case c: cases) {
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
		Case supportCase = client.getCase(caseId, monitor);
		return createTaskDataFromCase(client, repository, supportCase, monitor);
	}
	
	private TaskData createTaskDataFromCase(RhcpClient client, TaskRepository repository,
			Case supportCase, IProgressMonitor monitor) throws CoreException {
		TaskData taskData = new TaskData(getAttributeMapper(repository), RhcpCorePlugin.CONNECTOR_KIND,
				repository.getRepositoryUrl(), supportCase.getCaseNumber() + "");
		taskData.setVersion(TASK_DATA_VERSION);
		createDefaultAttributes(taskData, client);
		updateTaskData(repository, taskData, supportCase);
		return taskData;
	}

	
	// Mapings from RhcpSupportCase to TaskData
	
	private void createDefaultAttributes(TaskData taskData, RhcpClient client) {
		taskData.getRoot().createAttribute("x").setValue("y");
		
	}

	private void updateTaskData(TaskRepository repository, TaskData taskData, Case supportCase) {
		boolean readOnly = true;

		addAttribute(taskData, TaskAttribute.STATUS, supportCase.getStatus(),
				TaskAttribute.TYPE_SINGLE_SELECT, "Status",
				true, readOnly);
		addAttribute(taskData, TaskAttribute.SEVERITY, supportCase.getSeverity(),
				TaskAttribute.TYPE_SINGLE_SELECT, "Severity",
				true, readOnly);

		if (supportCase.getProduct() != null) {
			addAttribute(taskData, TaskAttribute.PRODUCT, supportCase.getProduct(),
					TaskAttribute.TYPE_SINGLE_SELECT, "Product",
					true, readOnly);
		}
		if (supportCase.getVersion() != null) {
			addAttribute(taskData, TaskAttribute.VERSION, supportCase.getVersion(),
					TaskAttribute.TYPE_SINGLE_SELECT, "Version",
					true, readOnly);
		}

		if (supportCase.getCreatedDate() != null) {
			addAttribute(taskData, TaskAttribute.DATE_CREATION, supportCase.getCreatedDate().getTime().toString(),
					TaskAttribute.TYPE_DATETIME, "Created at",
					true, true);
		}
		if (supportCase.getLastModifiedDate() != null) {
			addAttribute(taskData, TaskAttribute.DATE_MODIFICATION, supportCase.getLastModifiedDate().getTime().toString(),
					TaskAttribute.TYPE_DATETIME, "Modified at",
					true, true);
		}


		addAttribute(taskData, TaskAttribute.SUMMARY, supportCase.getSummary(),
				TaskAttribute.TYPE_SHORT_TEXT, "Summary",
				true, readOnly);

		addAttribute(taskData, TaskAttribute.USER_ASSIGNED, supportCase.getOwner(),
				TaskAttribute.TYPE_PERSON, "Owner",
				true, true);
		addAttribute(taskData, TaskAttribute.USER_REPORTER, supportCase.getContactName(),
				TaskAttribute.TYPE_PERSON, "Contact",
				true, true);


		List<Comment> comments = supportCase.getComments().getComment();
		int count = 0;
		for (Comment c: comments) {
			if (c == comments.get(0)) {
				addAttribute(taskData, TaskAttribute.DESCRIPTION, c.getText(),
						TaskAttribute.TYPE_LONG_TEXT, "Description",
						true, readOnly);
			} else {
		        TaskCommentMapper mapper = new TaskCommentMapper();  // Create a new one each time, to be safe.
		        // Set properties and text associated with this comment.
		        mapper.setAuthor(repository.createPerson(c.getCreatedBy()));
		        mapper.setCreationDate(c.getCreatedDate().getTime());
		        mapper.setText(c.getText());
		        mapper.setCommentId(c.getId());
		        mapper.setIsPrivate(!c.getPublic());
		        mapper.setUrl(c.getUri());

				//mapper.setNumber(count);
		        //c.getDraft()
		        //c.getLastModifiedBy()
		        //c.getLastModifiedDate()
			 
			    // Create, in the task data object, a new attribute that will hold this comment.
				TaskAttribute attribute = taskData.getRoot().createAttribute(TaskAttribute.PREFIX_COMMENT + count);
				mapper.applyTo(attribute);
			}
			count++;
		}
		
		/*for (Link l: supportCase.getNotifiedUsers().getLink()) {
			//FIXME: add CC'd users
		}*/
		
		/*
		 * closed
		 * caseNumber
		 * createdBy
		 * createdDate
		 * lastModifiedBy
		 * lastModifiedDate
		 * id
		 * uri
		 * type
		 * accountNumber
		 * escalated
		 * contactName
		 * contactSsoUsername
		 * origin
		 * entitlement
		 */
		
	}

	private TaskAttribute addAttribute(TaskData taskData, String key, String value,
			String attrType, String label,
			boolean isVisible, boolean readOnly) {
		TaskAttribute attr = taskData.getRoot().createAttribute(key);
		attr.setValue(value);
        TaskAttributeMetaData metaData = attr.getMetaData();
        metaData.setType(attrType);
        metaData.setKind(isVisible ? TaskAttribute.KIND_DEFAULT : null);
        metaData.setLabel(label);
        metaData.setReadOnly(readOnly);
        return attr;
	}
}
