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

package com.redhat.gss.waigatahu.cases.core.client;

import java.io.InputStream;
import java.net.URL;
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
	String getAccountNumber(IProgressMonitor monitor);
	String getContactName(IProgressMonitor monitor);

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
	void updateData(IProgressMonitor monitor);
	List<Product> getProducts(IProgressMonitor monitor);
	List<String> getVersions(String product, IProgressMonitor monitor);
	List<String> getStatuses(IProgressMonitor monitor);
	List<String> getSeverities(IProgressMonitor monitor);
	List<String> getTypes(IProgressMonitor monitor);
	List<Group> getGroups(IProgressMonitor monitor);
	List<User> getUsers(String accountNumber, IProgressMonitor monitor);
	

	// other data
	boolean isUserRedHat(String userId, IProgressMonitor monitor);
	URL getAccountManagementUrl(IProgressMonitor monitor);
	URL getUserProfileUrl(IProgressMonitor monitor);
}
