package com.redhat.gss.mylyn.rhcp.core.client;

import org.eclipse.mylyn.tasks.core.TaskRepository;

public class RhcpClientFactoryImpl implements RhcpClientFactory {
	public RhcpClient getClient(TaskRepository repository) {
		return new RhcpClientImpl(repository);
	}

}
