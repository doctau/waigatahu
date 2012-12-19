package com.redhat.gss.waigatahu.cases.core.client;

import org.eclipse.mylyn.tasks.core.TaskRepository;

public interface RhcpClientFactory {
	RhcpClient getClient(TaskRepository repository);
}
