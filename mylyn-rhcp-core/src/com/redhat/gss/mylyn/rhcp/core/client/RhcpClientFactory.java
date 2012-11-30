package com.redhat.gss.mylyn.rhcp.core.client;

import org.eclipse.mylyn.tasks.core.TaskRepository;

public interface RhcpClientFactory {
	RhcpClient getClient(TaskRepository repository);

	// URL structure
	String getRepositoryUrlFromCaseUrl(String taskUrl);
	long getCaseNumberFromCaseUrl(String taskUrl);
	String getCaseUrl(String repositoryUrl, long caseNumber);
}
