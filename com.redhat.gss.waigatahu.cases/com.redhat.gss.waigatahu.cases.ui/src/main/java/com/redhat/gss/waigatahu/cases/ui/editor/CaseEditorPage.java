package com.redhat.gss.waigatahu.cases.ui.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelListener;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import com.redhat.gss.waigatahu.cases.core.CaseRepositoryConnector;
import com.redhat.gss.waigatahu.cases.core.client.RhcpClient;
import com.redhat.gss.waigatahu.cases.data.CaseAttribute;

public class CaseEditorPage extends AbstractTaskEditorPage {
	private final CaseRepositoryConnector connector;
	
	public CaseEditorPage(TaskEditor editor, String connectorKind, CaseRepositoryConnector connector) {
		super(editor, connectorKind);
		this.connector = connector;
	}

	public CaseEditorPage(TaskEditor editor, String id, String label, String connectorKind, CaseRepositoryConnector connector) {
		super(editor, id, label, connectorKind);
		this.connector = connector;
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


	@Override
	protected Set<TaskEditorPartDescriptor> createPartDescriptors() {
		Set<TaskEditorPartDescriptor> descriptors = super.createPartDescriptors();
		// remove unnecessary default editor parts
		for (Iterator<TaskEditorPartDescriptor> it = descriptors.iterator(); it.hasNext();) {
			TaskEditorPartDescriptor taskEditorPartDescriptor = it.next();
			if (taskEditorPartDescriptor.getId().equals(ID_PART_PEOPLE)) {
				it.remove();
			}
		}
		descriptors.add(new TaskEditorPartDescriptor(ID_PART_PEOPLE) {
			@Override
			public AbstractTaskEditorPart createPart() {
				return new TaskEditorPeoplePart() {
					protected List<String> getDisplayedAttributes() {
						List<String> attrs = super.getDisplayedAttributes();
						attrs.add(CaseAttribute.USER_CONTACT);
						attrs.add(CaseAttribute.USER_LAST_MODIFIER);
						return attrs;
					}
				};
			}
		}.setPath(PATH_PEOPLE));
		return descriptors;
	}

	protected AttributeEditorFactory createAttributeEditorFactory() {
		return new CaseAttributeEditorFactory(getModel(), getTaskRepository(), getEditorSite());
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
				String msg = Messages.bind(Messages.CaseEditorPage_Validation_failed, attr.getMetaData().getLabel());
				sb.append(msg);
				sb.append('\n');
			}
			//FIXME: add the list of fields to the error
			getTaskEditor().setMessage(sb.toString(), IMessageProvider.ERROR,
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
		
		if (root.getAttribute(TaskAttribute.DESCRIPTION).getValue().isEmpty()) {
			invalid.add(root.getAttribute(TaskAttribute.DESCRIPTION));
		}
		
		if (root.getAttribute(TaskAttribute.SUMMARY).getValue().isEmpty()) {
			invalid.add(root.getAttribute(TaskAttribute.SUMMARY));
		}
		
		if (root.getAttribute(TaskAttribute.PRODUCT).getValue().isEmpty()) {
			invalid.add(root.getAttribute(TaskAttribute.PRODUCT));
		} else if (root.getAttribute(TaskAttribute.VERSION).getValue().isEmpty()) {
			invalid.add(root.getAttribute(TaskAttribute.VERSION));
		}
		
		TaskAttribute contactAttr = root.getAttribute(CaseAttribute.USER_CONTACT);
		if (contactAttr.getOption(contactAttr.getValue()) == null) {
			//unknown contact
			invalid.add(root.getAttribute(CaseAttribute.USER_CONTACT));
		}

		TaskAttribute ccAttr = root.getAttribute(TaskAttribute.USER_CC);
		for (String c: ccAttr.getValues())
		if (contactAttr.getOption(c) == null) {
			//unknown contact
			invalid.add(root.getAttribute(TaskAttribute.USER_CC));
		}
		
		return invalid;
	}
}
