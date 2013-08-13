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

package com.redhat.gss.waigatahu.diagnostics.ui.console;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.statushandlers.StatusManager;

import com.redhat.gss.waigatahu.diagnostics.core.WaigatahuDiagnosticsCorePlugin;
import com.redhat.gss.waigatahu.diagnostics.core.client.DiagnosticsClient;
import com.redhat.gss.waigatahu.diagnostics.core.client.DiagnosticsClientFactory;
import com.redhat.gss.waigatahu.diagnostics.core.data.DiagnosticResults;
import com.redhat.gss.waigatahu.diagnostics.ui.editor.DiagnosisResultsEditor;
import com.redhat.gss.waigatahu.diagnostics.ui.editor.DiagnostisResultsEditorInput;

public class SubmitDiagnosticsAction extends Action {
	private final DiagnosticsClientFactory clientFactory;
	private final TextConsolePage page;
	private final TextConsole console;

	public SubmitDiagnosticsAction(DiagnosticsClientFactory clientFactory, TextConsolePage page, TextConsole console) {
		this.clientFactory = clientFactory;
		this.page = page;
		this.console = console;

		setText("Diagnose");
		setToolTipText("Submit data to Red Hat for diagnosis");
		setDescription("Submit data to Red Hat for diagnosis!");
	}

	public void run() {
		//FIXME: there must be a better way of doing this
		final byte[] bs;
		try {
			bs = console.getDocument().get().getBytes("UTF-8");

			final IWorkbenchWindow window = page.getSite().getWorkbenchWindow();
			window.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					DiagnosticsClient client = clientFactory.getClient();
					DiagnosticResults results;
					try {
						results = client.diagnose(bs, new NullProgressMonitor()); // FIXME: actually add progress to the UI
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					}

					IEditorInput input = new DiagnostisResultsEditorInput(results);
					try {
						window.getActivePage().openEditor(input, DiagnosisResultsEditor.ID);
					} catch (PartInitException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InvocationTargetException e) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, WaigatahuDiagnosticsCorePlugin.PLUGIN_ID,
							"Error diagnosing data", e));
		} catch (InterruptedException e) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, WaigatahuDiagnosticsCorePlugin.PLUGIN_ID,
							"Error diagnosing data", e));
		} catch (UnsupportedEncodingException e) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, WaigatahuDiagnosticsCorePlugin.PLUGIN_ID,
							"Error diagnosing data", e));
		}
	}
}
