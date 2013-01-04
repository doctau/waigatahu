package com.redhat.gss.waigatahu.cases.core.client;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

import com.redhat.gss.strata.model.Attachment;
import com.redhat.gss.strata.model.Case;
import com.redhat.gss.strata.model.Group;
import com.redhat.gss.strata.model.Product;
import com.redhat.gss.strata.model.User;
import com.redhat.gss.waigatahu.cases.core.CaseId;

public interface RhcpClient {
	void shutdown();

	//core
	void validateConnection(IProgressMonitor monitor);
	boolean canCreateCases();

	//cases
	Case getCase(CaseId caseId, IProgressMonitor monitor);
	List<Attachment> getCaseAttachments(CaseId caseId, IProgressMonitor monitor);
	void updateCaseMetadata(CaseId caseId, Case supportCase, IProgressMonitor monitor);
	Case createCase(Case supportCase, IProgressMonitor monitor);
	
	//queries
	Collection<Case> queryCases(CaseQuery query, IProgressMonitor monitor);
	
	//attachments
	InputStream streamAttachment(CaseId caseId, String attachmentId,
			String url, IProgressMonitor monitor);
	void postAttachment(CaseId caseId, String comment, TaskAttribute attribute,
			AbstractTaskAttachmentSource source, IProgressMonitor monitor);


	//comments
	void postComment(CaseId caseId, String text, TaskAttribute attribute, IProgressMonitor monitor);


	// field values
	void updateData();
	List<Product> getProducts();
	List<String> getVersions(String product);
	List<String> getStatuses();
	List<String> getSeverities();
	List<String> getTypes();
	List<Group> getGroups();
	List<User> getUsers(String accountNumber);
}
