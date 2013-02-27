package com.redhat.gss.waigatahu.cases.core.client;

import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.redhat.gss.waigatahu.common.client.CustomerPortalClient;

public interface RhcpClientFactory {
	RhcpClient getClient(TaskRepository repository);
}
