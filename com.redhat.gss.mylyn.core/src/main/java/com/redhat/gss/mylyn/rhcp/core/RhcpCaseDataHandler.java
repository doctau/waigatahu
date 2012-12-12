package com.redhat.gss.mylyn.rhcp.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;

import com.redhat.gss.mylyn.rhcp.core.client.RhcpClient;
import com.redhat.gss.strata.model.Attachment;
import com.redhat.gss.strata.model.Case;
import com.redhat.gss.strata.model.Comment;
import com.redhat.gss.strata.model.Product;

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
		return new RhcpTaskAttributeMapper(repository);
	}

	public TaskData getTaskData(TaskRepository repository, String taskId,
			IProgressMonitor monitor) throws CoreException {
		IProgressMonitor myMonitor = Policy.monitorFor(monitor);
		try {
			RhcpClient client = connector.getClientFactory().getClient(repository);

			myMonitor.beginTask("Task Download", IProgressMonitor.UNKNOWN);
			TaskData data = downloadTaskData(client, repository, Long.parseLong(taskId), myMonitor);

			myMonitor.beginTask("Listing Attachments", IProgressMonitor.UNKNOWN);
			updateAttachmentList(client, repository, data, myMonitor);
			
			return data;
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
		updateTaskData(client, repository, taskData, supportCase);
		return taskData;
	}


	// query the attachment list and update the TaskData
	private void updateAttachmentList(RhcpClient client,
			TaskRepository repository, TaskData data, IProgressMonitor monitor) {
		List<Attachment> currentAttachments = client.getCaseAttachments(data.getTaskId(), monitor);
		
		TaskAttribute root = data.getRoot();

		// from uuid to the attribute key
		Map<String, String> oldAttachments = new HashMap<String,String>();
		Set<TaskAttribute> attachmentAttributes = getPrefixedAttributes(root, TaskAttribute.PREFIX_ATTACHMENT);
		for (TaskAttribute ta: attachmentAttributes) {
			String uuid = ta.getAttribute(TaskAttribute.ATTACHMENT_ID).getValue();
			String key = ta.getId();
			oldAttachments.put(uuid, key);
		}

		List<Attachment> newAttachments = new ArrayList<Attachment>();
		Map<String, Attachment> keptAttachments = new HashMap<String, Attachment>();
		for (Attachment a: currentAttachments) {
			String key = oldAttachments.get(a.getUuid());
			if (key != null) {
				keptAttachments.put(key, a);
				oldAttachments.remove(a.getUuid());
			} else {
				newAttachments.add(a);
			}
		}

		//remove non-existant attachments
		for (String k: oldAttachments.values()) {
			root.removeAttribute(k);
		}

		//update keptAttachments (may not actually be changed)
		for (Entry<String, Attachment> e: keptAttachments.entrySet()) {
			TaskAttribute attr = root.getAttribute(e.getKey());
			TaskAttachmentMapper mapper = RhcpAttachmentMapper.createFrom(repository, e.getValue());
			mapper.applyTo(attr);
		}
		
		//add newAttachments
		long suffix = findHighestSuffix(attachmentAttributes, TaskAttribute.PREFIX_ATTACHMENT);
		for (Attachment a: newAttachments) {
			TaskAttribute attr = root.createAttribute(TaskAttribute.PREFIX_ATTACHMENT + /*a.getUuid()*/ (suffix + 2));
			TaskAttachmentMapper mapper = RhcpAttachmentMapper.createFrom(repository, a);
			mapper.applyTo(attr);
		}
	}

	// Mappings from RhcpSupportCase to TaskData
	
	private void createDefaultAttributes(TaskData taskData, RhcpClient client) {
		final boolean readOnly = false;

		TaskAttribute attr = addAttribute(taskData, TaskAttribute.STATUS, null,
				TaskAttribute.TYPE_SINGLE_SELECT, "Status",
				true, readOnly);
		for (String s: client.getStatuses()) {
			attr.putOption(s, s);
		}
		
		attr = addAttribute(taskData, TaskAttribute.SEVERITY, null,
			TaskAttribute.TYPE_SINGLE_SELECT, "Severity",
			true, readOnly);
		for (String s: client.getSeverities()) {
			attr.putOption(s, s);
		}

		attr = addAttribute(taskData, TaskAttribute.PRODUCT, null,
				TaskAttribute.TYPE_SINGLE_SELECT, "Product",
				true, readOnly);
		for (Product p: client.getProducts()) {
			attr.putOption(p.getName(), p.getCode());
		}

		attr = addAttribute(taskData, TaskAttribute.VERSION, null,
				TaskAttribute.TYPE_SINGLE_SELECT, "Version",
				true, readOnly);

		addAttribute(taskData, TaskAttribute.DATE_CREATION, null,
				TaskAttribute.TYPE_DATETIME, "Created at",
				true, true);
		addAttribute(taskData, TaskAttribute.DATE_MODIFICATION, null,
				TaskAttribute.TYPE_DATETIME, "Modified at",
				true, true);
	
		addAttribute(taskData, TaskAttribute.SUMMARY, null,
				TaskAttribute.TYPE_SHORT_TEXT, "Summary",
				true, readOnly);

		addAttribute(taskData, TaskAttribute.USER_ASSIGNED, null,
				TaskAttribute.TYPE_PERSON, "Owner",
				true, true);
		addAttribute(taskData, TaskAttribute.USER_REPORTER, null,
				TaskAttribute.TYPE_PERSON, "Contact",
				true, true);
		addAttribute(taskData, CaseAttribute.CLOSED, null,
				TaskAttribute.TYPE_BOOLEAN, "Closed",
				false, true);
		addAttribute(taskData, TaskAttribute.DESCRIPTION, null,
				TaskAttribute.TYPE_LONG_TEXT, "Description",
				true, readOnly);
	}

	private void updateTaskData(RhcpClient client, TaskRepository repository, TaskData taskData, Case supportCase) {
		TaskAttribute root = taskData.getRoot();


		root.getAttribute(TaskAttribute.DATE_CREATION).setValue(supportCase.getCreatedDate().getTime().toString());
		root.getAttribute(TaskAttribute.DATE_MODIFICATION).setValue(supportCase.getLastModifiedDate().getTime().toString());
		root.getAttribute(TaskAttribute.SUMMARY).setValue(supportCase.getSummary());
		root.getAttribute(TaskAttribute.USER_ASSIGNED).setValue(supportCase.getOwner());
		root.getAttribute(TaskAttribute.USER_REPORTER).setValue(supportCase.getContactName());
		root.getAttribute(TaskAttribute.STATUS).setValue(supportCase.getStatus());
		root.getAttribute(TaskAttribute.SEVERITY).setValue(supportCase.getSeverity());

		root.getAttribute(TaskAttribute.PRODUCT).setValue(supportCase.getProduct());
		root.getAttribute(TaskAttribute.VERSION).setValue(blankNull(supportCase.getVersion()));
		if (supportCase.getProduct() != null) {
			for (String v: client.getVersions(supportCase.getProduct())) {
				root.getAttribute(TaskAttribute.VERSION).putOption(v, v);
			}
		} else {
			// no product but there was a version...
			//FIXME: report the error?
		}

		root.getAttribute(CaseAttribute.CLOSED).setValue(boolToString(supportCase.getClosed()));
		/* FIXME: TODO:
		 * closed
		 * caseNumber
		 * createdBy
		 * createdDate
		 * lastModifiedBy
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

		/*for (Link l: supportCase.getNotifiedUsers().getLink()) {
			//FIXME: add CC'd users
		}*/

		
		List<Comment> comments = supportCase.getComments().getComment();
		int count = 0;
		for (Comment c: comments) {
			if (c == comments.get(0)) {
				root.getAttribute(TaskAttribute.DESCRIPTION).setValue(c.getText());
			} else {
				RhcpCommentMapper mapper = RhcpCommentMapper.createFrom(repository, c);  // Create a new one each time, to be safe.
				//mapper.setNumber(count);
			 
			    // Create, in the task data object, a new attribute that will hold this comment.
				TaskAttribute attribute = root.createAttribute(TaskAttribute.PREFIX_COMMENT + count);
				mapper.applyTo(attribute);
			}
			count++;
		}
		

	}

	private String dateToString(Calendar date) {
		 return (date != null) ? date.getTime().toString() : null;
	}

	private String dateToString(Date date) {
		return (date != null) ? date.toString() : null;
	}

	private String boolToString(Boolean b) {
		return (b != null) ? b.toString() : "";
	}

	private String blankNull(String s) {
		return (s != null) ? s : "";
	}

	private long findHighestSuffix(Set<TaskAttribute> attrs, String prefix) {
		int len = prefix.length();
		long max = -1;
		for (TaskAttribute ta: attrs) {
			long l = Long.parseLong(ta.getId().substring(len));
			if (l > max)
				max = l;
		}
		return max;
	}

	private TaskAttribute addAttribute(TaskData taskData, String key, String value,
			String attrType, String label,
			boolean isVisible, boolean readOnly) {
		TaskAttribute attr = taskData.getRoot().createAttribute(key);
		if (value != null)
			attr.setValue(value);
        TaskAttributeMetaData metaData = attr.getMetaData();
        metaData.setType(attrType);
        metaData.setKind(isVisible ? TaskAttribute.KIND_DEFAULT : null);
        metaData.setLabel(label);
        metaData.setReadOnly(readOnly);
        return attr;
	}
	
	private Set<TaskAttribute> getPrefixedAttributes(TaskAttribute root,
			String prefix) {
		Set<TaskAttribute> tas = new HashSet<TaskAttribute>();
		for (Entry<String, TaskAttribute> e: root.getAttributes().entrySet()) {
			if (e.getKey().startsWith(prefix))
				tas.add(e.getValue());
		}
		return tas;
	}
}
