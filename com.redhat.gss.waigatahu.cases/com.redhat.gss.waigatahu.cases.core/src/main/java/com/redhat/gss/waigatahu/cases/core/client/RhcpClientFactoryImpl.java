package com.redhat.gss.waigatahu.cases.core.client;

import org.eclipse.mylyn.tasks.core.TaskRepository;

public class RhcpClientFactoryImpl implements RhcpClientFactory {
	public RhcpClient getClient(TaskRepository repository) {
		return new RhcpClientImpl(repository, this);
	}

	public String getRepositoryUrlFromCaseUrl(String taskUrl) {
		throw new IllegalArgumentException();
	}

	public long getCaseNumberFromCaseUrl(String taskUrl) {
		throw new IllegalArgumentException();
	}

	public String getCaseUrl(String repositoryUrl, long caseNumber) {
		return repositoryUrl + "/cases/" + caseNumber;
	}

}
