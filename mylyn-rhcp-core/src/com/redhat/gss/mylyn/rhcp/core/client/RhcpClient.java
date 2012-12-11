package com.redhat.gss.mylyn.rhcp.core.client;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;

import com.redhat.gss.strata.model.Case;

public interface RhcpClient {
	void validateConnection(IProgressMonitor monitor);
	Case getCase(long caseId, IProgressMonitor monitor);
	Collection<Case> getAllOpenCases(RhcpClient client, IProgressMonitor monitor);
	boolean canCreateCases();

	// URL structure
	String getRepositoryUrlFromCaseUrl(String taskUrl);
	long getCaseNumberFromCaseUrl(String taskUrl);
	String getCaseUrl(String repositoryUrl, long caseNumber);
}
