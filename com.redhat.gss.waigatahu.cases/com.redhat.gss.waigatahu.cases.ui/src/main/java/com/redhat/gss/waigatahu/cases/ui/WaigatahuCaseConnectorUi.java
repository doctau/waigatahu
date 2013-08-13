/*******************************************************************************
* Copyright (c) 2012 Red Hat, Inc.
* Distributed under license by Red Hat, Inc. All rights reserved.
* This program is made available under the terms of the
* Eclipse Public License v1.0 which accompanies this distribution,
* and is available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Red Hat, Inc. - initial API and implementation
******************************************************************************/

package com.redhat.gss.waigatahu.cases.ui;

import org.eclipse.core.runtime.NullProgressMonitor;
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
import com.redhat.gss.waigatahu.cases.data.CaseAttribute;
import com.redhat.gss.waigatahu.cases.ui.attachment.CaseAttachmentPage;
import com.redhat.gss.waigatahu.cases.ui.query.CaseQueryPage;

public class WaigatahuCaseConnectorUi extends AbstractRepositoryConnectorUi {
	private final String REPOSITORY_PAGE_TITLE = "REPOSITORY_PAGE_TITLE";
	private final String REPOSITORY_PAGE_DESCRIPTION = "REPOSITORY_PAGE_DESCRIPTION";
	private final String SEARCH_PAGE_DESCRIPTION = "SEARCH_PAGE_DESCRIPTION";
	
	public WaigatahuCaseConnectorUi() {
		
	}
	
	public String getConnectorKind() {
		return WaigatahuCaseCorePlugin.CONNECTOR_KIND;
	}

	private CaseRepositoryConnector getConnector() {
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
		return true;
	}

	public ITaskSearchPage getSearchPage(TaskRepository repository, IStructuredSelection selection) {
		return new CaseQueryPage(SEARCH_PAGE_DESCRIPTION, repository, null);
	}

	/* hyperlink highlighting
	public IHyperlink[] findHyperlinks(TaskRepository repository, ITask task, String text, int index, int textOffset) {
		return findHyperlinks(repository, text, index, textOffset);
	}
	*/


	/* account management URLs */

	public String getAccountCreationUrl(TaskRepository taskRepository) {
		NullProgressMonitor monitor = new NullProgressMonitor();
		return getConnector().getClient(taskRepository).getAccountManagementUrl(monitor).toExternalForm();
	}

	public String getAccountManagementUrl(TaskRepository taskRepository) {
		NullProgressMonitor monitor = new NullProgressMonitor();
		return getConnector().getClient(taskRepository).getUserProfileUrl(monitor).toExternalForm();
	}
	
	/* case URLs */
	public String getTaskHistoryUrl(TaskRepository taskRepository, ITask task) {
		return task.getAttribute(CaseAttribute.WEB_URL);
	}
}
