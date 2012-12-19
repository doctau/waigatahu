package com.redhat.gss.waigatahu.cases.core.client;

import org.eclipse.mylyn.tasks.core.TaskRepository;

public class RhcpClientFactoryImpl implements RhcpClientFactory {
	public RhcpClient getClient(TaskRepository repository) {
		return new RhcpClientImpl(repository);
	}
}
