package com.redhat.gss.mylyn.rhcp.core.client;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.redhat.gss.mylyn.rhcp.core.data.RhcpSupportCase;
import com.redhat.gss.mylyn.rhcp.core.data.RhcpSupportCases;

public interface RhcpClient {
	void validateConnection(IProgressMonitor monitor);
	RhcpSupportCase getCase(long caseId, IProgressMonitor monitor);
	Collection<RhcpSupportCase> getAllOpenCases(RhcpClient client, IProgressMonitor monitor);
	boolean canCreateCases();

	// URL structure
	String getRepositoryUrlFromCaseUrl(String taskUrl);
	long getCaseNumberFromCaseUrl(String taskUrl);
	String getCaseUrl(String repositoryUrl, long caseNumber);
}
