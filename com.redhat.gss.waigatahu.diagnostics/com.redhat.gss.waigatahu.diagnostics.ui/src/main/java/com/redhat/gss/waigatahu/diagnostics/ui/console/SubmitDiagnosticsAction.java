package com.redhat.gss.waigatahu.diagnostics.ui.console;

import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
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
		DiagnosticsClient client = clientFactory.getClient();

		//FIXME: there must be a better way of doing this
		byte[] bs;
		try {
			bs = console.getDocument().get().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		try {
			DiagnosticResults results = client.diagnose(bs, new NullProgressMonitor()); // FIXME: actually add progress to the UI
			
			IEditorInput input = new DiagnostisResultsEditorInput(results);
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, DiagnosisResultsEditor.ID);
		} catch (Exception e) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, WaigatahuDiagnosticsCorePlugin.PLUGIN_ID,
							"Error diagnosing data", e));
		}
	}
}
