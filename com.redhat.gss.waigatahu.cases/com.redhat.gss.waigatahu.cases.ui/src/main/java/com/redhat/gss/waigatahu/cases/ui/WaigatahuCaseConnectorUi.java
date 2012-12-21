package com.redhat.gss.waigatahu.cases.ui;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPage;

import com.redhat.gss.waigatahu.cases.core.CaseRepositoryConnector;
import com.redhat.gss.waigatahu.cases.core.WaigatahuCaseCorePlugin;
import com.redhat.gss.waigatahu.cases.core.client.RhcpClientFactory;
import com.redhat.gss.waigatahu.cases.ui.attachment.CaseAttachmentPage;

public class WaigatahuCaseConnectorUi extends AbstractRepositoryConnectorUi {
	private final String REPOSITORY_PAGE_TITLE = "REPOSITORY_PAGE_TITLE";
	private final String REPOSITORY_PAGE_DESCRIPTION = "REPOSITORY_PAGE_DESCRIPTION";
	
	private CaseRepositoryConnector connector;
	
	public WaigatahuCaseConnectorUi() {
		
	}
	
	public String getConnectorKind() {
		return WaigatahuCaseCorePlugin.CONNECTOR_KIND;
	}

	private RhcpClientFactory getConnector() {
		//FIXME: how to get this?
		return WaigatahuCaseCorePlugin.getConnector();
	}

	public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
		return new CaseRepositoryPage(REPOSITORY_PAGE_TITLE, REPOSITORY_PAGE_DESCRIPTION, taskRepository, getConnector());
	}

	public IWizard getQueryWizard(TaskRepository repository, IRepositoryQuery query) {
		return new CaseQueryWizard(repository, query);
	}

	public IWizardPage getTaskAttachmentPage(TaskAttachmentModel model) {
		return new CaseAttachmentPage(model);
	}

	public IWizard getNewTaskWizard(TaskRepository repository, ITaskMapping selection) {
		return new NewCaseWizard(repository, selection);
	}

	/* search */
	public boolean hasSearchPage() {
		//TODO
		return false;
	}

	public ITaskSearchPage getSearchPage(TaskRepository repository, IStructuredSelection selection) {
		return null;
	}

	/* hyperlink highlighting
	public IHyperlink[] findHyperlinks(TaskRepository repository, ITask task, String text, int index, int textOffset) {
		return findHyperlinks(repository, text, index, textOffset);
	}
	*/


	/* account management URLs */

	public String getAccountCreationUrl(TaskRepository taskRepository) {
		return null;
	}

	public String getAccountManagementUrl(TaskRepository taskRepository) {
		return null;
	}
	
	/* case URLs */
	public String getTaskHistoryUrl(TaskRepository taskRepository, ITask task) {
		return null;
	}
}
