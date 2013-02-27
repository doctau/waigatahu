package com.redhat.gss.waigatahu.cases.core.client;

import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.redhat.gss.waigatahu.common.client.CustomerPortalClient;

public class RhcpClientFactoryImpl implements RhcpClientFactory {
	private final CustomerPortalClient portal;

	public RhcpClientFactoryImpl(CustomerPortalClient portal) {
		this.portal = portal;
	}
	
	public RhcpClient getClient(TaskRepository repository) {
		return new RhcpClientImpl(repository, portal);
	}
}
