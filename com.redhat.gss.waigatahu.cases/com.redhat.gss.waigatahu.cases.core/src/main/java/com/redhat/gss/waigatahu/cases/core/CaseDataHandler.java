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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;

import com.redhat.gss.strata.model.Attachment;
import com.redhat.gss.strata.model.Case;
import com.redhat.gss.strata.model.Comment;
import com.redhat.gss.strata.model.Group;
import com.redhat.gss.strata.model.Link;
import com.redhat.gss.strata.model.NotifiedUsers;
import com.redhat.gss.strata.model.Product;
import com.redhat.gss.strata.model.User;
import com.redhat.gss.waigatahu.cases.core.client.CaseQuery;
import com.redhat.gss.waigatahu.cases.core.client.RhcpClient;
import com.redhat.gss.waigatahu.cases.data.CaseAttribute;
import com.redhat.gss.waigatahu.cases.data.PersonAttribute;
import com.redhat.gss.waigatahu.cases.data.QueryAttribute;

public class CaseDataHandler extends AbstractTaskDataHandler {
	private final String TASK_DATA_VERSION = "1";
	
	private final List<String> FIELDS_HIDDEN_NEW_CASE = Arrays.asList(
			TaskAttribute.STATUS,
			TaskAttribute.USER_ASSIGNED,
			TaskAttribute.USER_REPORTER,
			CaseAttribute.USER_LAST_MODIFIER
	);

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

			// show the fields hidden for new cases
			for (String field: FIELDS_HIDDEN_NEW_CASE)  {
				taskData.getRoot().getAttribute(field).getMetaData().setKind(TaskAttribute.KIND_PEOPLE);
			}
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
			CaseId caseId = new CaseId(taskData.getRoot().getAttribute(CaseAttribute.CASE_URI).getValue());
			client.updateCaseMetadata(caseId, supportCase, monitor);

			TaskAttribute newComment = taskData.getRoot().getAttribute(TaskAttribute.COMMENT_NEW);
			if (!newComment.getValue().isEmpty()) {
				//FIXME: post comment
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
			updateTaskData(client, repository, taskData, createdCase, monitor);
			postCaseCreation(taskData);

			String taskId = connector.getTaskIdFromTaskUrl(createdCase.getUri());
			return new RepositoryResponse(ResponseKind.TASK_CREATED, taskId);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean initializeTaskData(TaskRepository repository, TaskData taskData,
			ITaskMapping initializationData, IProgressMonitor monitor)
			throws CoreException {
		RhcpClient client = connector.getClient(repository);
		taskData.setVersion(TASK_DATA_VERSION);
		createDefaultAttributes(taskData, client, client.getAccountNumber(monitor), monitor);

		TaskAttribute root = taskData.getRoot();

		// hide fields that don't make sense before it's created
		for (String field: FIELDS_HIDDEN_NEW_CASE)  {
			taskData.getRoot().getAttribute(field).getMetaData().setKind(null);
		}

		// default values for new tasks
		String user = client.getContactName(monitor);
		root.getAttribute(CaseAttribute.USER_CONTACT).setValue(user);
		
		return true;
	}

	public CaseAttributeMapper getAttributeMapper(TaskRepository repository) {
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

			CaseQuery cquery = mapQuery(query);
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

	private CaseQuery mapQuery(IRepositoryQuery query) {
		CaseQuery cquery = new CaseQuery();
		cquery.setClosed(query.getAttribute(QueryAttribute.CLOSED));
		cquery.setCaseGroup(query.getAttribute(QueryAttribute.CASE_GROUP));
		cquery.setSearchTerms(query.getAttribute(QueryAttribute.SEARCH_TERMS));
		cquery.setStartDate(QueryAttribute.Convert.toDate(query.getAttribute(QueryAttribute.START_DATE)));
		cquery.setEndDate(QueryAttribute.Convert.toDate(query.getAttribute(QueryAttribute.END_DATE)));
		cquery.setQueryUseTime(Boolean.parseBoolean(query.getAttribute(QueryAttribute.QUERY_USE_TIME)));
		return cquery;
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
		createDefaultAttributes(taskData, client, client.getAccountNumber(monitor), monitor);

		// create the attributes which only exist for real tasks
		addAttribute(taskData, TaskAttribute.COMMENT_NEW, "",
				TaskAttribute.TYPE_LONG_RICH_TEXT, "New comment",
				CaseAttribute.KIND_HIDDEN, false);
		updateTaskData(client, repository, taskData, supportCase, monitor);
		postCaseCreation(taskData);

		// attachments are not loaded yet
		taskData.setPartial(true);
		return taskData;
	}

	private void postCaseCreation(TaskData taskData) {
		//shouldn't be allowed to modify later
		taskData.getRoot().getAttribute(TaskAttribute.DESCRIPTION).getMetaData().setReadOnly(true);
	}


	// query the attachment list and update the TaskData
	private void updateAttachmentList(RhcpClient client,
			TaskRepository repository, CaseId caseId, TaskData data, IProgressMonitor monitor) {
		List<Attachment> currentAttachments = client.getCaseAttachments(caseId, monitor);
		
		TaskAttribute root = data.getRoot();
		CaseAttributeMapper mapper = getAttributeMapper(repository);

		// from uuid to the attribute key
		Map<String, String> oldAttachments = new HashMap<String,String>();
		List<TaskAttribute> attachmentAttributes = mapper.getAttributesByType(data, TaskAttribute.TYPE_ATTACHMENT);

		for (TaskAttribute ta: attachmentAttributes) {
			String uuid = mapper.getValue(ta.getAttribute(TaskAttribute.ATTACHMENT_ID));
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

		//remove non-existent attachments
		for (String k: oldAttachments.values()) {
			root.removeAttribute(k);
		}

		//update keptAttachments (may not actually be changed)
		for (Entry<String, Attachment> e: keptAttachments.entrySet()) {
			TaskAttribute attr = root.getAttribute(e.getKey());
			TaskAttachmentMapper amapper = CaseAttachmentMapper.createFrom(repository, e.getValue());
			amapper.applyTo(attr);
		}

		//add newAttachments
		for (Attachment a: newAttachments) {
			TaskAttribute attr = root.createAttribute(TaskAttribute.PREFIX_ATTACHMENT + a.getUuid());
			TaskAttachmentMapper amapper = CaseAttachmentMapper.createFrom(repository, a);
			amapper.applyTo(attr);
		}
	}

	// Mappings from RhcpSupportCase to TaskData
	
	private void createDefaultAttributes(TaskData taskData, RhcpClient client, String accountNumber, IProgressMonitor monitor) {
		final boolean readOnly = false;

		TaskAttribute attr = addAttribute(taskData, TaskAttribute.STATUS, null,
				TaskAttribute.TYPE_SINGLE_SELECT, "Status",
				TaskAttribute.KIND_DEFAULT, readOnly);
		for (String s: client.getStatuses(monitor)) {
			attr.putOption(s, s);
		}

		attr = addAttribute(taskData, TaskAttribute.SEVERITY, null,
			TaskAttribute.TYPE_SINGLE_SELECT, "Severity",
			TaskAttribute.KIND_DEFAULT, readOnly);
		for (String s: client.getSeverities(monitor)) {
			attr.putOption(s, s);
		}

		attr = addAttribute(taskData, CaseAttribute.CASE_TYPE, null,
				TaskAttribute.TYPE_SINGLE_SELECT, "Case type",
				TaskAttribute.KIND_DEFAULT, readOnly);
		attr.putOption("", ""); // default is none
		for (String s: client.getTypes(monitor)) {
			attr.putOption(s, s);
		}

		attr = addAttribute(taskData, TaskAttribute.PRODUCT, null,
				TaskAttribute.TYPE_SINGLE_SELECT, "Product",
				TaskAttribute.KIND_DEFAULT, readOnly);
		for (Product p: client.getProducts(monitor)) {
			attr.putOption(p.getName(), p.getCode());
		}

		attr = addAttribute(taskData, TaskAttribute.VERSION, null,
				TaskAttribute.TYPE_SINGLE_SELECT, "Version",
				TaskAttribute.KIND_DEFAULT, readOnly);
	
		addAttribute(taskData, CaseAttribute.ALTERNATE_ID, null,
				TaskAttribute.TYPE_SHORT_TEXT, "Alternate ID",
				TaskAttribute.KIND_DEFAULT, readOnly);
	
		addAttribute(taskData, CaseAttribute.ACCOUNT_NUMBER, null,
				TaskAttribute.TYPE_LONG, "Account Number",
				CaseAttribute.KIND_HIDDEN, true);
	
		addAttribute(taskData, CaseAttribute.FOLDER, null,
				TaskAttribute.TYPE_SINGLE_SELECT, "Case Group",
				TaskAttribute.KIND_DEFAULT, readOnly);

		addAttribute(taskData, TaskAttribute.DATE_CREATION, null,
				TaskAttribute.TYPE_DATETIME, "Created at",
				CaseAttribute.KIND_HIDDEN, true);
		addAttribute(taskData, TaskAttribute.DATE_MODIFICATION, null,
				TaskAttribute.TYPE_DATETIME, "Modified at",
				CaseAttribute.KIND_HIDDEN, true);
	
		addAttribute(taskData, TaskAttribute.SUMMARY, null,
				TaskAttribute.TYPE_SHORT_TEXT, "Summary",
				CaseAttribute.KIND_HIDDEN, readOnly);

		TaskAttribute userContactAttr = addAttribute(taskData, CaseAttribute.USER_CONTACT, null,
				TaskAttribute.TYPE_PERSON, "Primary Customer Contact",
				TaskAttribute.KIND_PEOPLE, readOnly);
		TaskAttribute usersCcAttr = addAttribute(taskData, TaskAttribute.USER_CC, null,
				TaskAttribute.TYPE_MULTI_SELECT/*TYPE_PERSON*/, "Notified users",
				TaskAttribute.KIND_PEOPLE, readOnly);

		addAttribute(taskData, CaseAttribute.USER_LAST_MODIFIER, null,
				TaskAttribute.TYPE_PERSON, "Last Modified By",
				TaskAttribute.KIND_PEOPLE, true);
		addAttribute(taskData, TaskAttribute.USER_ASSIGNED, null,
				TaskAttribute.TYPE_PERSON, "Red Hat Owner",
				TaskAttribute.KIND_PEOPLE, true);
		addAttribute(taskData, TaskAttribute.USER_REPORTER, null,
				TaskAttribute.TYPE_PERSON, "Created by",
				TaskAttribute.KIND_PEOPLE, true);
		addAttribute(taskData, CaseAttribute.CLOSED, null,
				TaskAttribute.TYPE_BOOLEAN, "Closed",
				CaseAttribute.KIND_HIDDEN, true);
		addAttribute(taskData, TaskAttribute.DESCRIPTION, null,
				TaskAttribute./*TYPE_LONG_TEXT*/TYPE_LONG_RICH_TEXT, "Description",
				CaseAttribute.KIND_HIDDEN, readOnly);
		addAttribute(taskData, TaskAttribute.TASK_KEY, null,
				TaskAttribute.TYPE_LONG, "Case number",
				CaseAttribute.KIND_HIDDEN, readOnly);

		addAttribute(taskData, CaseAttribute.CASE_URI, null,
				TaskAttribute.TASK_URL, "RS URI",
				CaseAttribute.KIND_HIDDEN, true);

		addAttribute(taskData, CaseAttribute.WEB_URL, null,
				TaskAttribute.TASK_URL, "Customer Portal case URL",
				CaseAttribute.KIND_HIDDEN, true);


		for (User u: client.getUsers(accountNumber, monitor)) {
			//FIXME: i18n of combining names sucks
			String name = u.getFirstName() + " " + u.getLastName();
			userContactAttr.putOption(u.getSsoUsername(), name);
			usersCcAttr.putOption( u.getSsoUsername(), name);
		}

		// support level (RO)
		// group - how do we find out if it's available to them?
	}

	private void updateTaskData(RhcpClient client, TaskRepository repository, TaskData taskData, Case supportCase, IProgressMonitor monitor) {
		TaskAttribute root = taskData.getRoot();
		CaseAttributeMapper mapper = getAttributeMapper(repository);

		// core data
		mapper.setValue(root.getAttribute(TaskAttribute.TASK_KEY), supportCase.getCaseNumber());
		mapper.setValue(root.getAttribute(CaseAttribute.CASE_URI), supportCase.getUri());
		mapper.setDateValue(root.getAttribute(TaskAttribute.DATE_CREATION), supportCase.getCreatedDate().getTime());
		mapper.setDateValue(root.getAttribute(TaskAttribute.DATE_MODIFICATION), supportCase.getLastModifiedDate().getTime());
		mapper.setValue(root.getAttribute(TaskAttribute.SUMMARY), supportCase.getSummary());
		mapper.setValue(root.getAttribute(TaskAttribute.STATUS), supportCase.getStatus());
		mapper.setValue(root.getAttribute(TaskAttribute.SEVERITY), supportCase.getSeverity());

		
		// custom data
		mapper.setBooleanValue(root.getAttribute(CaseAttribute.CLOSED), supportCase.getClosed());
		mapper.setNullableStringValue(root.getAttribute(CaseAttribute.CASE_TYPE), supportCase.getType());
		mapper.setNullableStringValue(root.getAttribute(CaseAttribute.ALTERNATE_ID), supportCase.getAlternateId());
		mapper.setValue(root.getAttribute(CaseAttribute.ACCOUNT_NUMBER), supportCase.getAccountNumber());
		mapper.setNullableStringValue(root.getAttribute(CaseAttribute.WEB_URL), supportCase.getViewUri());


		// users
		bindUserAttribute(client, repository, root.getAttribute(TaskAttribute.USER_ASSIGNED), supportCase.getOwner(), monitor);
		bindUserAttribute(client, repository, root.getAttribute(TaskAttribute.USER_REPORTER), supportCase.getCreatedBy(), monitor);
		bindUserAttribute(client, repository, root.getAttribute(CaseAttribute.USER_LAST_MODIFIER), supportCase.getLastModifiedBy(), monitor);

		// contacts aren't RH people, so don't set the flag
		IRepositoryPerson contactPerson = repository.createPerson((supportCase.getContactSsoUsername() != null) ? supportCase.getContactSsoUsername() : supportCase.getContactName());
		contactPerson.setName(supportCase.getContactName());
		TaskAttribute userContactAttr = root.getAttribute(CaseAttribute.USER_CONTACT);
		mapper.setRepositoryPerson(userContactAttr, contactPerson);
		userContactAttr.putOption(contactPerson.getName(), contactPerson.getPersonId());

		// CC'd users
		TaskAttribute usersCcAttr = root.getAttribute(TaskAttribute.USER_CC);
		NotifiedUsers notifiedUsers = supportCase.getNotifiedUsers();
		if (notifiedUsers != null) {
			for (Link l: notifiedUsers.getLink()) {
				//add CC'd users
				IRepositoryPerson ccPerson = repository.createPerson(l.getSsoUsername());
				ccPerson.setName(l.getValue());
				mapper.addRepositoryPerson(usersCcAttr, ccPerson);
			}
		}

		
		// product and version
		mapper.setValue(root.getAttribute(TaskAttribute.PRODUCT), supportCase.getProduct());
		if (supportCase.getProduct() != null) {
			root.getAttribute(TaskAttribute.VERSION).clearOptions();
			for (String v: client.getVersions(supportCase.getProduct(), monitor)) {
				root.getAttribute(TaskAttribute.VERSION).putOption(v, v);
			}
		} else {
			// no product but there was a version...
			//FIXME: report the error?
		}
		mapper.setNullableStringValue(root.getAttribute(TaskAttribute.VERSION), supportCase.getVersion());
		/* FIXME: TODO:
		 * component
		 * id
		 * escalated
		 * origin
		 * entitlement
		 */

		
		// case groups
		TaskAttribute folderAttr = root.getAttribute(CaseAttribute.FOLDER);
		mapper.setNullableStringValue(folderAttr, supportCase.getFolderNumber());
		folderAttr.putOption("", ""); // default is none
		for (Group p: client.getGroups(monitor)) {
			folderAttr.putOption(p.getName(), p.getNumber());
		}
		
		//FIXME: attached bugzilla data?
		//meta: TYPE_TASK_DEPENDENCY

		
		// comments
		List<Comment> comments = supportCase.getComments().getComment();
		int count = 0;
		for (Comment c: comments) {
			if (c == comments.get(0)) {
				root.getAttribute(TaskAttribute.DESCRIPTION).setValue(c.getText());
			} else {
				CommentMapper cmapper = CommentMapper.createFrom(repository, c);  // Create a new one each time, to be safe.
				//mapper.setNumber(count);

			    // Create, in the task data object, a new attribute that will hold this comment.
				TaskAttribute attribute = root.createAttribute(TaskAttribute.PREFIX_COMMENT + count);
				cmapper.applyTo(attribute);
			}
			count++;
		}
	}

	private void bindUserAttribute(RhcpClient client,
			TaskRepository repository, TaskAttribute attr,
			String userId, IProgressMonitor monitor) {
		CaseAttributeMapper mapper = getAttributeMapper(repository);

		IRepositoryPerson person = repository.createPerson(userId);
		mapper.setRepositoryPerson(attr, person);

		TaskAttribute isRedhatAttr = new TaskAttribute(attr, PersonAttribute.IS_REDHAT);
		mapper.setBooleanValue(isRedhatAttr, client.isUserRedHat(userId, monitor));
	}
	
	private Case createCaseFromTaskData(TaskRepository repository, TaskData taskData, IProgressMonitor monitor) throws CoreException {
		CaseAttributeMapper mapper = getAttributeMapper(repository);
		TaskAttribute root = taskData.getRoot();

		Case supportCase = new Case();
		supportCase.setCaseNumber(mapper.getNullableStringValue(root.getAttribute(TaskAttribute.TASK_KEY)));

		String summary = mapper.getValue(root.getAttribute(TaskAttribute.SUMMARY));
		if (summary.isEmpty())
			throw new CoreException(new Status(IStatus.ERROR, WaigatahuCaseCorePlugin.CONNECTOR_KIND, "Summary is missing"));
		supportCase.setSummary(summary);
		
		String description = mapper.getValue(root.getAttribute(TaskAttribute.DESCRIPTION));
		if (description.isEmpty())
			throw new CoreException(new Status(IStatus.ERROR, WaigatahuCaseCorePlugin.CONNECTOR_KIND, "Description is missing"));
		supportCase.setDescription(description);

		String status = mapper.getValue(root.getAttribute(TaskAttribute.STATUS));
		supportCase.setStatus(status.isEmpty() ? null : status);

		String product = mapper.getValue(root.getAttribute(TaskAttribute.PRODUCT));
		if (product.isEmpty())
			throw new CoreException(new Status(IStatus.ERROR, WaigatahuCaseCorePlugin.CONNECTOR_KIND, "Product is missing"));
		supportCase.setProduct(product);

		String version = mapper.getValue(root.getAttribute(TaskAttribute.VERSION));
		if (version.isEmpty())
			throw new CoreException(new Status(IStatus.ERROR, WaigatahuCaseCorePlugin.CONNECTOR_KIND, "Version is missing"));
		supportCase.setVersion(version);


		String customerContact = mapper.getValue(root.getAttribute(CaseAttribute.USER_CONTACT));
		if (customerContact.isEmpty())
			throw new CoreException(new Status(IStatus.ERROR, WaigatahuCaseCorePlugin.CONNECTOR_KIND, "Customer contact is missing"));
		supportCase.setContactName(customerContact);

		//FIXME: is this required?
		supportCase.setSeverity(mapper.getNullableStringValue(root.getAttribute(TaskAttribute.SEVERITY)));

		supportCase.setType(mapper.getNullableStringValue(root.getAttribute(CaseAttribute.CASE_TYPE)));
		supportCase.setAlternateId(mapper.getNullableStringValue(root.getAttribute(CaseAttribute.ALTERNATE_ID)));
		return supportCase;
	}

	private TaskAttribute addAttribute(TaskData taskData, String key, String value,
			String attrType, String label,
			String kind, boolean readOnly) {
		TaskAttribute attr = taskData.getRoot().createAttribute(key);
		if (value != null)
			attr.setValue(value);
        TaskAttributeMetaData metaData = attr.getMetaData();
        metaData.setType(attrType);
        metaData.setKind(kind); // FIXME: KIND_PEOPLE for some?
        metaData.setLabel(label);
        metaData.setReadOnly(readOnly);
        return attr;
	}
}
