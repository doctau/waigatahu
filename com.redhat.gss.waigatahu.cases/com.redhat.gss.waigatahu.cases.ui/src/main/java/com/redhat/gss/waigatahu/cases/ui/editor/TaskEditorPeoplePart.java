package com.redhat.gss.waigatahu.cases.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.Messages;
import org.eclipse.mylyn.internal.tasks.ui.editors.MultiSelectionAttributeEditor;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/*
 * Clone of upstream, letting you override attribute list.
 * 
 * TODO: file bug to get this upstream
 */
public class TaskEditorPeoplePart extends AbstractTaskEditorPart {
	private static final int COLUMN_MARGIN = 5;

	public TaskEditorPeoplePart() {
		setPartName(Messages.TaskEditorPeoplePart_People);
	}

	private void addAttribute(Composite composite, FormToolkit toolkit, TaskAttribute attribute) {
		AbstractAttributeEditor editor = createAttributeEditor(attribute);
		if (editor != null) {
			editor.createLabelControl(composite, toolkit);
			GridDataFactory.defaultsFor(editor.getLabelControl())
					.indent(COLUMN_MARGIN, 0)
					.applyTo(editor.getLabelControl());
			editor.createControl(composite, toolkit);
			getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);

			GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP);

			if (editor instanceof MultiSelectionAttributeEditor) {
				gridDataFactory.hint(SWT.DEFAULT, 95);
			}

			gridDataFactory.applyTo(editor.getControl());
		}
	}

	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		Section section = createSection(parent, toolkit, true);

		Composite peopleComposite = toolkit.createComposite(section);
		GridLayout layout = EditorUtil.createSectionClientLayout();
		layout.numColumns = 2;
		peopleComposite.setLayout(layout);

		for (String attrName: getDisplayedAttributes()) {
			addAttribute(peopleComposite, toolkit, getTaskData().getRoot().getMappedAttribute(attrName));
		}

		toolkit.paintBordersFor(peopleComposite);
		section.setClient(peopleComposite);
		setSection(toolkit, section);
	}

	protected List<String> getDisplayedAttributes() {
		List<String> attrs = new ArrayList<String>();
		attrs.add(TaskAttribute.USER_ASSIGNED);
		attrs.add(TaskAttribute.USER_REPORTER);
		attrs.add(TaskAttribute.ADD_SELF_CC);
		attrs.add(TaskAttribute.USER_CC);
		return attrs;
	}

}
