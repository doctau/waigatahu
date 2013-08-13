/*******************************************************************************
* Copyright (c) 2012 Red Hat, Inc.
* Distributed under license by Red Hat, Inc. All rights reserved.
* This program is made available under the terms of the
* Eclipse Public License v1.0 which accompanies this distribution,
* and is available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Red Hat, Inc. - initial API and implementation
******************************************************************************/

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
