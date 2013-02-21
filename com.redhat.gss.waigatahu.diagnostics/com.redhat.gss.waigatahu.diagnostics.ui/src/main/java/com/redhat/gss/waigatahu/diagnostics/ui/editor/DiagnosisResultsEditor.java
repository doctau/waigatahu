package com.redhat.gss.waigatahu.diagnostics.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

public class DiagnosisResultsEditor extends FormEditor {
	public static final String ID = "com.redhat.gss.waigatahu.diagnostics.ui.editor.DiagnosisResultsEditor";

	protected void addPages() {
		try {
			addPage(new DiagnosticResultsFormPage(this, DiagnosticResultsFormPage.ID, "Diagnosis results"));
		} catch (PartInitException e) {
			throw new RuntimeException(e);
		}
	}

	public void doSave(IProgressMonitor monitor) {
		// do nothing
	}

	public void doSaveAs() {
		// do nothing

	}

	public boolean isSaveAsAllowed() {
		return false;
	}

}
