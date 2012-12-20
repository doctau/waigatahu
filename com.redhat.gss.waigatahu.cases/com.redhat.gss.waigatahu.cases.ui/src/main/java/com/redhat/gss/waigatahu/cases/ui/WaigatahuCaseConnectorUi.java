package com.redhat.gss.waigatahu.cases.ui;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;

import com.redhat.gss.waigatahu.cases.core.CaseRepositoryConnector;
import com.redhat.gss.waigatahu.cases.core.WaigatahuCaseCorePlugin;
import com.redhat.gss.waigatahu.cases.ui.attachment.CaseAttachmentPage;

public class WaigatahuCaseConnectorUi extends AbstractRepositoryConnectorUi {
	private final String REPOSITORY_PAGE_TITLE = "REPOSITORY_PAGE_TITLE";
	private final String REPOSITORY_PAGE_DESCRIPTION = "REPOSITORY_PAGE_DESCRIPTION";
	
	private CaseRepositoryConnector connector;
	
	public WaigatahuCaseConnectorUi() {
		//FIXME: how to get this?
		connector = new CaseRepositoryConnector();
	}
	
	public String getConnectorKind() {
		return WaigatahuCaseCorePlugin.CONNECTOR_KIND;
	}

	public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
		return new CaseRepositoryPage(REPOSITORY_PAGE_TITLE, REPOSITORY_PAGE_DESCRIPTION, taskRepository, connector);
	}

	public IWizard getQueryWizard(TaskRepository repository, IRepositoryQuery query) {
		return new CaseQueryWizard(repository, query);
	}

	public IWizardPage getTaskAttachmentPage(TaskAttachmentModel model) {
		return new CaseAttachmentPage(model);
	}

	public IWizard getNewTaskWizard(TaskRepository repository, ITaskMapping selection) {
		// no pages, so create the task immediately
		return new NewTaskWizard(repository, selection);
	}

	public boolean hasSearchPage() {
		//TODO
		return false;
	}
}
