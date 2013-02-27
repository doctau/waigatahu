package com.redhat.gss.waigatahu.knowledge.core;

import com.redhat.gss.waigatahu.cases.core.WaigatahuCaseCorePlugin;
import com.redhat.gss.waigatahu.common.client.CustomerPortalClient;
import com.redhat.gss.waigatahu.knowledge.core.client.KnowledgeClient;
import com.redhat.gss.waigatahu.knowledge.core.client.KnowledgeClientFactory;
import com.redhat.gss.waigatahu.knowledge.core.client.KnowledgeClientImpl;

public class KnowledgeConnector implements KnowledgeClientFactory {
	private final KnowledgeClient client = new KnowledgeClientImpl(WaigatahuCaseCorePlugin.getConnector().getWebLocation(), new CustomerPortalClient());

	public KnowledgeClient getClient() {
		return client;
	}

	public void shutdown() {
		client.shutdown();
	}
}
