package com.redhat.gss.waigatahu.diagnostics.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class WaigatahuDiagnosticsCorePlugin extends Plugin {
	public static final String PLUGIN_ID = "com.redhat.gss.waigatahu.diagnostics";

	private static WaigatahuDiagnosticsCorePlugin plugin;
	private static DiagnosticsConnector connector;


	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
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
