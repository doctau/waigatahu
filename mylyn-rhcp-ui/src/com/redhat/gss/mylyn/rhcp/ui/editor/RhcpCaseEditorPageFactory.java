package com.redhat.gss.mylyn.rhcp.ui.editor;

import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.IFormPage;

public class RhcpCaseEditorPageFactory extends AbstractTaskEditorPageFactory {
	public boolean canCreatePageFor(TaskEditorInput input) {
		throw new IllegalArgumentException();
	}

	public Image getPageImage() {
		throw new IllegalArgumentException();
	}

	public String getPageText() {
		throw new IllegalArgumentException();
	}

	public IFormPage createPage(TaskEditor parentEditor) {
		throw new IllegalArgumentException();
	}

}
