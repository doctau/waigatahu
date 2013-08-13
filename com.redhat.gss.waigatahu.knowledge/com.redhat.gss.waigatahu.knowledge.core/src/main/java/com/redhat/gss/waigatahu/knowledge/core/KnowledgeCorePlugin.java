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

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class KnowledgeCorePlugin extends Plugin {
	public static final String PLUGIN_ID = "com.redhat.gss.waigatahu.knowledge.core";

	private static KnowledgeCorePlugin plugin;
	private static KnowledgeConnector connector;

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		KnowledgeCorePlugin.context = bundleContext;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		KnowledgeCorePlugin.context = null;
		plugin = null;
		connector.shutdown();
		super.stop(bundleContext);
	}


	public static KnowledgeCorePlugin getDefault() {
		return plugin;
	}

	public static synchronized KnowledgeConnector getConnector() {
		if (connector == null)
			connector = new KnowledgeConnector();
		return connector;
	}
}
