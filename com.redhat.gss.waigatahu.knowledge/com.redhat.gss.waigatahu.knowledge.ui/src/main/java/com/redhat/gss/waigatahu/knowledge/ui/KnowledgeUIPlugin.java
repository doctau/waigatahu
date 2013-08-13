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

package com.redhat.gss.waigatahu.knowledge.ui;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

import com.redhat.gss.strata.model.Solution;
import com.redhat.gss.waigatahu.knowledge.core.KnowledgeCorePlugin;
import com.redhat.gss.waigatahu.knowledge.core.client.KnowledgeClient;

/**
 * The activator class controls the plug-in life cycle
 */
public class KnowledgeUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.redhat.gss.waigatahu.knowledge.ui"; //$NON-NLS-1$

	// The shared instance
	private static KnowledgeUIPlugin plugin;
	
	/**
	 * The constructor
	 */
	public KnowledgeUIPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static KnowledgeUIPlugin getDefault() {
		return plugin;
	}

	public void createKnowledgeEditor(final String uri, final String label) {
		try {
			getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
			    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					KnowledgeClient client = KnowledgeCorePlugin.getConnector().getClient();
					try {
						Solution s = client.getSolution(new URI(uri), monitor);
						String view_uri = s.getViewUri();
	
						//TODO: local editor rather than web?
						try {
							IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
							IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.AS_EDITOR, view_uri, label,label);
							browser.openURL(new URL(view_uri));
						} catch (PartInitException ex) {
							throw new InvocationTargetException(ex);
						} catch (MalformedURLException ex) {
							throw new InvocationTargetException(ex);
						}
					} catch (URISyntaxException e) {
						throw new InvocationTargetException(e);
					}
			    }
			});
		} catch (InvocationTargetException ex) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, KnowledgeUIPlugin.PLUGIN_ID,
							"Error opening browser", ex));
		} catch (InterruptedException ex) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, KnowledgeUIPlugin.PLUGIN_ID,
							"Error opening browser", ex));
		}
	}
}
