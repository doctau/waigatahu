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
