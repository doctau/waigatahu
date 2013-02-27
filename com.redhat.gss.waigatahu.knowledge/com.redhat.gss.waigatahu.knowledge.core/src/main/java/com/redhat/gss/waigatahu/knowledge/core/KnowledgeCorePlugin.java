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
