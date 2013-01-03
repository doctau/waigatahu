package com.redhat.gss.waigatahu.cases.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelListener;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import com.redhat.gss.waigatahu.cases.core.CaseRepositoryConnector;
import com.redhat.gss.waigatahu.cases.core.client.RhcpClient;

public class CaseEditorPage extends AbstractTaskEditorPage {
	private final CaseRepositoryConnector connector;
	
	public CaseEditorPage(TaskEditor editor, String connectorKind, CaseRepositoryConnector connector) {
		super(editor, connectorKind);
		this.connector = connector;
		init(editor);
	}

	public CaseEditorPage(TaskEditor editor, String id, String label, String connectorKind, CaseRepositoryConnector connector) {
		super(editor, id, label, connectorKind);
		this.connector = connector;
		init(editor);
	}

	private void init(TaskEditor editor) {
		//this.createAttributeEditorFactory();
	}

	protected TaskDataModel createModel(TaskEditorInput input) throws CoreException {
		TaskDataModel model = super.createModel(input);
		model.addModelListener(new TaskDataModelListener() {
			public void attributeChanged(TaskDataModelEvent event) {
				CaseEditorPage.this.attributeChanged(event);
			}
		});
		return model;
	}

	protected void attributeChanged(TaskDataModelEvent event) {
		TaskAttribute changedAttr = event.getTaskAttribute();
		TaskAttribute rootAttr = event.getModel().getTaskData().getRoot();
		
		if (changedAttr.getId().equals(TaskAttribute.PRODUCT)) {
			TaskRepository repository = event.getModel().getTaskRepository();
			RhcpClient client = connector.getClient(repository);

			TaskAttribute versionAttr = rootAttr.getAttribute(TaskAttribute.VERSION);
			// update version field
			versionAttr.clearOptions();
			for (String v: client.getVersions(changedAttr.getValue())) {
				versionAttr.putOption(v, v);
			}
			//update the UI field
			getModel().attributeChanged(versionAttr);
		}
	}

	public void doSubmit() {
		final List<TaskAttribute> invalid = validate();
		if (invalid.isEmpty()) {
			super.doSubmit();
		} else {
			//FIXME HACK this obviously sucks for i18n
			// how do you append a list to the message in a nice way?
			StringBuilder sb = new StringBuilder();
			for (TaskAttribute attr: invalid) {
				sb.append(attr.getMetaData().getLabel());
				sb.append(',');
			}
			//FIXME: add the list of fields to the error
			getTaskEditor().setMessage(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AbstractTaskEditorPage_Could_not_save_task, IMessageProvider.ERROR,
					new HyperlinkAdapter() {
						public void linkActivated(HyperlinkEvent event) {
							EditorUtil.reveal(getManagedForm().getForm(), invalid.get(0).getId());
						}
					});
		}
	}

	private List<TaskAttribute> validate() {
		TaskAttribute root = getModel().getTaskData().getRoot();
		List<TaskAttribute> invalid = new ArrayList<TaskAttribute>();
		
		if (root.getAttribute(TaskAttribute.DESCRIPTION).getValue().isEmpty())
			invalid.add(root.getAttribute(TaskAttribute.DESCRIPTION));
		
		if (root.getAttribute(TaskAttribute.SUMMARY).getValue().isEmpty())
			invalid.add(root.getAttribute(TaskAttribute.SUMMARY));
		
		if (root.getAttribute(TaskAttribute.PRODUCT).getValue().isEmpty())
			invalid.add(root.getAttribute(TaskAttribute.PRODUCT));
		else if (root.getAttribute(TaskAttribute.VERSION).getValue().isEmpty())
			invalid.add(root.getAttribute(TaskAttribute.VERSION));
		
		return invalid;
	}
}
