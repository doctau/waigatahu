package com.redhat.gss.waigatahu.diagnostics.core;

import com.redhat.gss.waigatahu.cases.core.WaigatahuCaseCorePlugin;
import com.redhat.gss.waigatahu.common.client.CustomerPortalClient;
import com.redhat.gss.waigatahu.diagnostics.core.client.DiagnosticsClient;
import com.redhat.gss.waigatahu.diagnostics.core.client.DiagnosticsClientFactory;
import com.redhat.gss.waigatahu.diagnostics.core.client.DiagnosticsClientImpl;

public class DiagnosticsConnector implements DiagnosticsClientFactory {
	private final DiagnosticsClient client = new DiagnosticsClientImpl(WaigatahuCaseCorePlugin.getConnector().getWebLocation(), new CustomerPortalClient());

	public DiagnosticsClient getClient() {
		return client;
	}


	public void shutdown() {
		client.shutdown();
	}
}
