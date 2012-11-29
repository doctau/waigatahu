package com.redhat.gss.mylyn.rhcp.core.client;

import org.eclipse.mylyn.tasks.core.TaskRepository;

public interface RhcpClientFactory {
	RhcpClient getClient(TaskRepository repository);
}
