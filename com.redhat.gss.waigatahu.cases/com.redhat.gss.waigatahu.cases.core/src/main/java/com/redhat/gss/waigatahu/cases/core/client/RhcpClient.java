package com.redhat.gss.waigatahu.cases.core.client;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

import com.redhat.gss.strata.model.Attachment;
import com.redhat.gss.strata.model.Case;
import com.redhat.gss.strata.model.Product;

public interface RhcpClient {
	//cases
	void validateConnection(IProgressMonitor monitor);
	boolean canCreateCases();

	//cases
	Case getCase(long caseId, IProgressMonitor monitor);
	List<Attachment> getCaseAttachments(long caseId, IProgressMonitor monitor);
	void updateCaseMetadata(long caseId, Case supportCase, IProgressMonitor monitor);
	
	//queries
	Collection<Case> getAllOpenCases(RhcpClient client, IProgressMonitor monitor);
	
	//attachments
	InputStream streamAttachment(long caseId, String attachmentId,
			String url, IProgressMonitor monitor);
	void postAttachment(long caseId, String comment, TaskAttribute attribute,
			AbstractTaskAttachmentSource source, IProgressMonitor monitor);

	// URL structure
	String getRepositoryUrlFromCaseUrl(String taskUrl);
	long getCaseNumberFromCaseUrl(String taskUrl);
	String getCaseUrl(String repositoryUrl, long caseNumber);

	// field values
	List<Product> getProducts();
	List<String> getVersions(String product);
	List<String> getStatuses();
	List<String> getSeverities();
	List<String> getTypes();
}
