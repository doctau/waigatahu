package com.redhat.gss.waigatahu.diagnostics.ui.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.redhat.gss.strata.model.Problems;

public class DiagnostisResultsEditorInput implements IEditorInput {
	private final Problems problems;

	public DiagnostisResultsEditorInput(Problems problems) {
		this.problems = problems;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter == IEditorInput.class) {
			return this;
		} else {
			return null;
		}
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		// TODO Auto-generated method stub
		return null;
	}

	public Problems getProblems() {
		return problems;
	}

}
