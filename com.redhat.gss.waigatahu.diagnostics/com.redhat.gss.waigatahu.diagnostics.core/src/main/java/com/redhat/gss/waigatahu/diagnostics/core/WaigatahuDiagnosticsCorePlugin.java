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

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class WaigatahuDiagnosticsCorePlugin extends Plugin {
	public static final String PLUGIN_ID = "com.redhat.gss.waigatahu.diagnostics.core";

	private static WaigatahuDiagnosticsCorePlugin plugin;
	private static DiagnosticsConnector connector;


	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		connector.shutdown();
		super.stop(context);
	}

	public static WaigatahuDiagnosticsCorePlugin getDefault() {
		return plugin;
	}

	public static synchronized DiagnosticsConnector getConnector() {
		if (connector == null)
			connector = new DiagnosticsConnector();
		return connector;
	}
}
