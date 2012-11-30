package com.redhat.gss.mylyn.rhcp.ui.editor;

import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;

public class RhcpTaskEditorPage extends AbstractTaskEditorPage {
	public RhcpTaskEditorPage(TaskEditor editor, String connectorKind) {
		super(editor, connectorKind);
	}

	public RhcpTaskEditorPage(TaskEditor editor, String id, String label, String connectorKind) {
		super(editor, id, label, connectorKind);
	}
}
