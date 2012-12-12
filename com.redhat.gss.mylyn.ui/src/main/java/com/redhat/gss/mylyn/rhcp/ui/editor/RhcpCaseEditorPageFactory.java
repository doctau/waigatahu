package com.redhat.gss.mylyn.rhcp.ui.editor;

import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.IFormPage;

import com.redhat.gss.mylyn.rhcp.core.RhcpCorePlugin;

public class RhcpCaseEditorPageFactory extends AbstractTaskEditorPageFactory {
	private static final String PAGE_TEXT = "PAGE TEXT";
	
	public boolean canCreatePageFor(TaskEditorInput input) {
		if (input.getTask().getConnectorKind().equals(RhcpCorePlugin.CONNECTOR_KIND)) {
			return true;
		} else {
			return false;
		}
	}

	public Image getPageImage() {
		//TODO: add
		return null;
	}

	public String getPageText() {
		return PAGE_TEXT;
	}

	public int getPriority() {
		return PRIORITY_TASK;
	}

	public IFormPage createPage(TaskEditor parentEditor) {
		return new RhcpTaskEditorPage(parentEditor, RhcpCorePlugin.CONNECTOR_KIND);
	}
}