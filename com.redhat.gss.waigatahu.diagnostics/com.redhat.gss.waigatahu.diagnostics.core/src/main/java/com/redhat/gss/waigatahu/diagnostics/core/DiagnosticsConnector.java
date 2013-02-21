package com.redhat.gss.waigatahu.diagnostics.core;

import org.eclipse.mylyn.commons.net.WebLocation;

import com.redhat.gss.waigatahu.diagnostics.core.client.DiagnosticsClient;
import com.redhat.gss.waigatahu.diagnostics.core.client.DiagnosticsClientFactory;
import com.redhat.gss.waigatahu.diagnostics.core.client.DiagnosticsClientImpl;

public class DiagnosticsConnector implements DiagnosticsClientFactory {
        private final DiagnosticsClient client = new DiagnosticsClientImpl(new WebLocation("http://URL", "user", "password"));
	
	public DiagnosticsClient getClient() {
		return client;
	}


	public void shutdown() {
		client.shutdown();
	}
}
