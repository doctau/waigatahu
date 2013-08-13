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

package com.redhat.gss.waigatahu.cases.core.client;

import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.redhat.gss.waigatahu.common.client.CustomerPortalClient;

public interface RhcpClientFactory {
	RhcpClient getClient(TaskRepository repository);
}
