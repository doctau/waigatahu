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
