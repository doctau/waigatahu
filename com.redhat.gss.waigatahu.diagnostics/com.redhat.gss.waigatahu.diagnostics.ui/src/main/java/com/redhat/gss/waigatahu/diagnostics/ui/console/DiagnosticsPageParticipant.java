package com.redhat.gss.waigatahu.diagnostics.ui.console;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.part.IPageBookViewPage;

import com.redhat.gss.waigatahu.diagnostics.core.WaigatahuDiagnosticsCorePlugin;
import com.redhat.gss.waigatahu.diagnostics.core.client.DiagnosticsClientFactory;

public class DiagnosticsPageParticipant implements IConsolePageParticipant {
	public void init(IPageBookViewPage page, IConsole console) {
		IActionBars actionBars = page.getSite().getActionBars();
		DiagnosticsClientFactory clientFactory = WaigatahuDiagnosticsCorePlugin.getConnector();
		
		if (page instanceof TextConsolePage) {
			TextConsolePage tcp = (TextConsolePage) page;
			TextConsole tc = (TextConsole) console;
			SubmitDiagnosticsAction action = new SubmitDiagnosticsAction(clientFactory, tcp, tc);
			action.setActionDefinitionId("com.redhat.gss.waigatahu.diagnotics.submitDiagnostics");
			actionBars.getToolBarManager().add(action);
		}
		
		actionBars.updateActionBars();
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void activated() {
		// TODO Auto-generated method stub
		
	}

	public void deactivated() {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("rawtypes") 
	public Object getAdapter(Class adapter) {
		if (adapter == IConsolePageParticipant.class) {
			return this;
		}
		return null;
	}
}
