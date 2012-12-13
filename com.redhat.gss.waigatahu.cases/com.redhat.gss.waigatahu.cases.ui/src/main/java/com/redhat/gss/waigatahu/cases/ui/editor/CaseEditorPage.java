package com.redhat.gss.waigatahu.cases.ui.editor;

import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;

public class CaseEditorPage extends AbstractTaskEditorPage {
	public CaseEditorPage(TaskEditor editor, String connectorKind) {
		super(editor, connectorKind);
		init(editor);
	}

	public CaseEditorPage(TaskEditor editor, String id, String label, String connectorKind) {
		super(editor, id, label, connectorKind);
		init(editor);
	}

	private void init(TaskEditor editor) {
		//this.createAttributeEditorFactory();
	}
}
