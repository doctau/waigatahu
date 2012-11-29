package com.redhat.gss.mylyn.rhcp.ui;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;

import com.redhat.gss.mylyn.rhcp.core.RhcpCaseRepositoryConnector;
import com.redhat.gss.mylyn.rhcp.core.RhcpCorePlugin;
import com.redhat.gss.mylyn.rhcp.core.client.RhcpClientFactory;
import com.redhat.gss.mylyn.rhcp.ui.query.RhcpCaseQueryPage;

public class RhcpCaseConnectorUi extends AbstractRepositoryConnectorUi {
	private final String REPOSITORY_PAGE_TITLE = "REPOSITORY_PAGE_TITLE";
	private final String REPOSITORY_PAGE_DESCRIPTION = "REPOSITORY_PAGE_DESCRIPTION";
	private final String QUERY_PAGE_DESCRIPTION = "QUERY_PAGE_DESCRIPTION";
	
	private RhcpCaseRepositoryConnector connector;
	
	public RhcpCaseConnectorUi() {
		//FIXME: how to get this?
		connector = new RhcpCaseRepositoryConnector();
	}
	
	public String getConnectorKind() {
		return RhcpCorePlugin.CONNECTOR_KIND;
	}

	public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
		RhcpClientFactory clientFactory = connector.getClientFactory();
		return new RhcpTaskRepositoryPage(REPOSITORY_PAGE_TITLE, REPOSITORY_PAGE_DESCRIPTION, taskRepository, clientFactory);
	}

	public IWizard getQueryWizard(TaskRepository repository,
			IRepositoryQuery query) {
		RepositoryQueryWizard wizard = new RepositoryQueryWizard(repository);
		wizard.addPage(new RhcpCaseQueryPage(QUERY_PAGE_DESCRIPTION, repository, query));
		return wizard;
	}

	public IWizard getNewTaskWizard(TaskRepository repository, ITaskMapping selection) {
		throw new IllegalArgumentException();
	}

	public boolean hasSearchPage() {
		//TODO
		return false;
	}
}
