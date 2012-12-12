package com.redhat.gss.mylyn.rhcp.ui.query;

import org.eclipse.mylyn.commons.workbench.forms.SectionComposite;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage2;

public class RhcpCaseQueryPage extends AbstractRepositoryQueryPage2 {
	public RhcpCaseQueryPage(String pageName, TaskRepository repository,
			IRepositoryQuery query) {
		super(pageName, repository, query);
	}

	protected void createPageContent(SectionComposite parent) {
		// TODO Auto-generated method stub
		
	}

	protected void doRefreshControls() {
		// TODO Auto-generated method stub
		
	}

	protected boolean hasRepositoryConfiguration() {
		// TODO Auto-generated method stub
		return false;
	}

	protected boolean restoreState(IRepositoryQuery query) {
		// TODO Auto-generated method stub
		return false;
	}

	public void applyTo(IRepositoryQuery query) {
		// TODO Auto-generated method stub
		
	}
}
