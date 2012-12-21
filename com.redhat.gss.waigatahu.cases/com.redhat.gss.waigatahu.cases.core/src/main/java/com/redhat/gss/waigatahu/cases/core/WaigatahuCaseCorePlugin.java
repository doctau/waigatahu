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
