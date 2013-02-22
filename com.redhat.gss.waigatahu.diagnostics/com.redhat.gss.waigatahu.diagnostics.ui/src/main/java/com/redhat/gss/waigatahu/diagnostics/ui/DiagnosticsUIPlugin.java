package com.redhat.gss.waigatahu.diagnostics.ui;

import java.io.Serializable;

import org.eclipse.swt.SWT;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.redhat.gss.strata.model.Link;
import com.redhat.gss.strata.model.Problem;
import com.redhat.gss.strata.model.Source;
import com.redhat.gss.waigatahu.diagnostics.core.data.DiagnosticResults;

/**
 * The activator class controls the plug-in life cycle
 */
public class DiagnosticsUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.redhat.gss.waigatahu.diagnostics.ui"; //$NON-NLS-1$

	// The shared instance
	private static DiagnosticsUIPlugin plugin;
	
	/**
	 * The constructor
	 */
	public DiagnosticsUIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DiagnosticsUIPlugin getDefault() {
		return plugin;
	}
}
