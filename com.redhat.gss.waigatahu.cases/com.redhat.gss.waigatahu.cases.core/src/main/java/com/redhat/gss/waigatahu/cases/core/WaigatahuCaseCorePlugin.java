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

package com.redhat.gss.waigatahu.cases.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class WaigatahuCaseCorePlugin extends Plugin {
	public static final String CONNECTOR_KIND = "com.redhat.gss.waigatahu.cases";

	private static WaigatahuCaseCorePlugin plugin;

	private static CaseRepositoryConnector connector;


	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		if (connector == null)
			connector = new CaseRepositoryConnector();
	}

	public void stop(BundleContext context) throws Exception {
		connector.shutdown();
		connector = null;
		plugin = null;
		super.stop(context);
	}

	public static WaigatahuCaseCorePlugin getDefault() {
		return plugin;
	}

	public static synchronized CaseRepositoryConnector getConnector() {
		if (connector == null)
			connector = new CaseRepositoryConnector();
		return connector;
	}

}
