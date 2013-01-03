package com.redhat.gss.waigatahu.cases.core;

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
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;

import com.redhat.gss.strata.model.Attachment;
import com.redhat.gss.strata.model.Case;
import com.redhat.gss.strata.model.Comment;
import com.redhat.gss.strata.model.Product;
import com.redhat.gss.waigatahu.cases.core.client.CaseQuery;
import com.redhat.gss.waigatahu.cases.core.client.RhcpClient;
import com.redhat.gss.waigatahu.cases.data.CaseAttribute;
import com.redhat.gss.waigatahu.cases.data.QueryAttribute;

public class CaseDataHandler extends AbstractTaskDataHandler {
	private final String TASK_DATA_VERSION = "1";
	
	private final CaseRepositoryConnector connector;

	public CaseDataHandler(CaseRepositoryConnector connector) {
		this.connector = connector;
	}

	public RepositoryResponse postTaskData(TaskRepository repository,
			TaskData taskData, Set<TaskAttribute> oldAttributes,
			IProgressMonitor monitor) throws CoreException {
		RhcpClient client = connector.getClient(repository);
		if (taskData.isNew()) {
			RepositoryResponse response = createCase(client, repository, taskData, monitor);
			// show the status field
			taskData.getRoot().getAttribute(TaskAttribute.STATUS).getMetaData().setKind(null);
			return response;
		} else {
			RepositoryResponse response = updateCase(client, repository, taskData, oldAttributes, monitor);
			return response;
		}
	}

	private RepositoryResponse updateCase(RhcpClient client, TaskRepository repository, TaskData taskData,
			Set<TaskAttribute> oldAttributes, IProgressMonitor monitor) {
		try {
			monitor.beginTask("Updating Case", IProgressMonitor.UNKNOWN);
			Case supportCase = createCaseFromTaskData(repository, taskData, monitor);
			String uri = taskData.getRoot().getAttribute(CaseAttribute.CASE_URI).getValue();
			client.updateCaseMetadata(new CaseId(uri), supportCase, monitor);

			TaskAttribute newComment = taskData.getRoot().getAttribute(TaskAttribute.COMMENT_NEW);
			if (!newComment.getValue().isEmpty()) {
				//FIXME: post comment
				CaseId caseId = connector.taskIdToCaseUrl(taskData.getTaskId());
				client.postComment(caseId, newComment.getValue(), newComment, monitor);
			}
			
			//FIXME: post attachments

			//FIXME: update case metadata, especially if there was a new comment
			return new RepositoryResponse(ResponseKind.TASK_UPDATED, taskData.getTaskId());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	private RepositoryResponse createCase(RhcpClient client, TaskRepository repository, TaskData taskData,
			IProgressMonitor monitor) {
		try {
			monitor.beginTask("Creating Case", IProgressMonitor.UNKNOWN);
			Case tempCase = createCaseFromTaskData(repository, taskData, monitor);
			Case createdCase = client.createCase(tempCase, monitor);
			updateTaskData(client, repository, taskData, createdCase);

			return new RepositoryResponse(ResponseKind.TASK_CREATED, taskData.getTaskId());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean initializeTaskData(TaskRepository repository, TaskData taskData,
			ITaskMapping initializationData, IProgressMonitor monitor)
			throws CoreException {
		RhcpClient client = connector.getClient(repository);
		taskData.setVersion(TASK_DATA_VERSION);
		createDefaultAttributes(taskData, client);
		taskData.getRoot().getAttribute(TaskAttribute.STATUS).getMetaData().setKind(null);
		
		return true;
	}

	public TaskAttributeMapper getAttributeMapper(TaskRepository repository) {
		return new CaseAttributeMapper(repository);
	}

	public TaskData getTaskData(TaskRepository repository, String taskId,
			IProgressMonitor monitor) throws CoreException {
		CaseId caseUrl = connector.taskIdToCaseUrl(taskId);
		IProgressMonitor myMonitor = Policy.monitorFor(monitor);
		try {
			RhcpClient client = connector.getClient(repository);

			myMonitor.beginTask("Task Download", IProgressMonitor.UNKNOWN);
			TaskData data = downloadTaskData(client, repository, caseUrl, myMonitor);

			myMonitor.beginTask("Listing Attachments", IProgressMonitor.UNKNOWN);
			updateAttachmentList(client, repository, caseUrl, data, myMonitor);
			data.setPartial(false);
			
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
			RhcpClient client = connector.getClient(repository);
			
			CaseQuery cquery = new CaseQuery();
			cquery.setClosed(query.getAttribute(QueryAttribute.CLOSED));
			cquery.setCaseGroup(query.getAttribute(QueryAttribute.CASE_GROUP));
			cquery.setSearchTerms(query.getAttribute(QueryAttribute.SEARCH_TERMS));
			//FIXME: parse the string// cquery.setStartDate(query.getAttribute(QueryAttribute.START_DATE));
			//cquery.setEndDate(query.getAttribute(QueryAttribute.END_DATE));
			cquery.setQueryUseTime(Boolean.parseBoolean(query.getAttribute(QueryAttribute.QUERY_USE_TIME)));
			Collection<Case> cases = client.queryCases(cquery, monitor);

			for (Case c: cases) {
				String taskId = connector.getTaskIdFromTaskUrl(c.getUri());
				try {
					TaskData task = createTaskDataFromCase(client, repository, c, monitor, taskId);
					collector.accept(task);
				} catch (CoreException e) {
					collector.failed(taskId, new Status(Status.ERROR, connector.getConnectorKind(), "FAILED!", e));
				}
			}
		} finally {
			myMonitor.done();
		}
		return Status.OK_STATUS;
	}
	
	

	private TaskData downloadTaskData(RhcpClient client, TaskRepository repository,
			CaseId caseId, IProgressMonitor monitor)
			throws CoreException {
		Case supportCase = client.getCase(caseId, monitor);
		String taskId = connector.getTaskIdFromTaskUrl(supportCase.getUri());
		return createTaskDataFromCase(client, repository, supportCase, monitor, taskId);
	}
	
	private TaskData createTaskDataFromCase(RhcpClient client, TaskRepository repository,
			Case supportCase, IProgressMonitor monitor, String taskId) throws CoreException {
		TaskData taskData = new TaskData(getAttributeMapper(repository), WaigatahuCaseCorePlugin.CONNECTOR_KIND,
				repository.getRepositoryUrl(), taskId);
		taskData.setVersion(TASK_DATA_VERSION);
		createDefaultAttributes(taskData, client);
		updateTaskData(client, repository, taskData, supportCase);

		// attachments are not loaded yet
		taskData.setPartial(true);
		return taskData;
	}


	// query the attachment list and update the TaskData
	private void updateAttachmentList(RhcpClient client,
			TaskRepository repository, CaseId caseId, TaskData data, IProgressMonitor monitor) {
		List<Attachment> currentAttachments = client.getCaseAttachments(caseId, monitor);
		
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
			TaskAttachmentMapper mapper = CaseAttachmentMapper.createFrom(repository, e.getValue());
			mapper.applyTo(attr);
		}
		
		//add newAttachments
		for (Attachment a: newAttachments) {
			TaskAttribute attr = root.createAttribute(TaskAttribute.PREFIX_ATTACHMENT + a.getUuid());
			TaskAttachmentMapper mapper = CaseAttachmentMapper.createFrom(repository, a);
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
				false, true);
		addAttribute(taskData, TaskAttribute.DATE_MODIFICATION, null,
				TaskAttribute.TYPE_DATETIME, "Modified at",
				false, true);
	
		addAttribute(taskData, TaskAttribute.SUMMARY, null,
				TaskAttribute.TYPE_SHORT_TEXT, "Summary",
				false, readOnly);

		addAttribute(taskData, TaskAttribute.USER_ASSIGNED, null,
				TaskAttribute.TYPE_PERSON, "Owner",
				false, true);
		addAttribute(taskData, TaskAttribute.USER_REPORTER, null,
				TaskAttribute.TYPE_PERSON, "Contact",
				false, true);
		addAttribute(taskData, CaseAttribute.CLOSED, null,
				TaskAttribute.TYPE_BOOLEAN, "Closed",
				false, true);
		addAttribute(taskData, TaskAttribute.DESCRIPTION, null,
				TaskAttribute./*TYPE_LONG_TEXT*/TYPE_LONG_RICH_TEXT, "Description",
				false, readOnly);
		addAttribute(taskData, TaskAttribute.TASK_KEY, null,
				TaskAttribute.TYPE_LONG, "Case number",
				false, readOnly);

		addAttribute(taskData, TaskAttribute.COMMENT_NEW, null,
				TaskAttribute.TYPE_LONG_RICH_TEXT, "New comment",
				false, readOnly);

		addAttribute(taskData, CaseAttribute.CASE_URI, null,
				TaskAttribute.TASK_URL, "RS URI",
				false, true);
	}

	private void updateTaskData(RhcpClient client, TaskRepository repository, TaskData taskData, Case supportCase) {
		TaskAttribute root = taskData.getRoot();


		root.getAttribute(TaskAttribute.TASK_KEY).setValue(supportCase.getCaseNumber());
		root.getAttribute(CaseAttribute.CASE_URI).setValue(supportCase.getUri());
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
				CommentMapper mapper = CommentMapper.createFrom(repository, c);  // Create a new one each time, to be safe.
				//mapper.setNumber(count);
			 
			    // Create, in the task data object, a new attribute that will hold this comment.
				TaskAttribute attribute = root.createAttribute(TaskAttribute.PREFIX_COMMENT + count);
				mapper.applyTo(attribute);
			}
			count++;
		}
	}
	
	private Case createCaseFromTaskData(TaskRepository repository, TaskData taskData, IProgressMonitor monitor) throws CoreException {
		TaskAttribute root = taskData.getRoot();

		Case supportCase = new Case();
		TaskAttribute keyAttr = root.getAttribute(TaskAttribute.TASK_KEY);
		if (!keyAttr.getValues().isEmpty())
			supportCase.setCaseNumber(keyAttr.getValue());
		
		String summary = root.getAttribute(TaskAttribute.SUMMARY).getValue();
		if (summary.isEmpty())
			throw new CoreException(new Status(IStatus.ERROR, WaigatahuCaseCorePlugin.CONNECTOR_KIND, "Summary is missing"));
		supportCase.setSummary(summary);
		
		String description = root.getAttribute(TaskAttribute.DESCRIPTION).getValue();
		if (description.isEmpty())
			throw new CoreException(new Status(IStatus.ERROR, WaigatahuCaseCorePlugin.CONNECTOR_KIND, "Description is missing"));
		supportCase.setDescription(description);

		supportCase.setStatus(root.getAttribute(TaskAttribute.STATUS).getValue());
		//FIXME: are these required?
		supportCase.setSeverity(root.getAttribute(TaskAttribute.SEVERITY).getValue());
		supportCase.setProduct(root.getAttribute(TaskAttribute.PRODUCT).getValue());
		
		String version = root.getAttribute(TaskAttribute.VERSION).getValue();
		if (version.isEmpty())
			throw new CoreException(new Status(IStatus.ERROR, WaigatahuCaseCorePlugin.CONNECTOR_KIND, "Version is missing"));
		supportCase.setVersion(version);

		// root.getAttribute(TaskAttribute.USER_REPORTER).setValue(supportCase.getContactName());
		return supportCase;
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
