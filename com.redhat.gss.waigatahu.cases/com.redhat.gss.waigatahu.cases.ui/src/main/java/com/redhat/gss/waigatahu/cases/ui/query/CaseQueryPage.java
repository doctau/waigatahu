package com.redhat.gss.waigatahu.cases.ui.query;

import org.eclipse.mylyn.commons.workbench.forms.SectionComposite;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.redhat.gss.waigatahu.cases.data.QueryAttribute;


public class CaseQueryPage extends AbstractRepositoryQueryPage2 {
	private Button openCases;
	private Button allCases;

	public CaseQueryPage(String pageName, TaskRepository repository,
			IRepositoryQuery query) {
		super(pageName, repository, query);
	}

	protected void createPageContent(SectionComposite parent) {
		Composite group = new Composite(parent.getContent(), SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		group.setLayout(layout);
		
		openCases = new Button(group, SWT.RADIO);
		openCases.setText("Open Cases");
		openCases.setVisible(true);
		
		allCases = new Button(group, SWT.RADIO);
		allCases.setText("All Cases");
		allCases.setVisible(true);
	}

	protected void doRefreshControls() {
		// TODO Auto-generated method stub
		
	}

	protected boolean hasRepositoryConfiguration() {
		// TODO Auto-generated method stub
		return false;
	}

	protected boolean restoreState(IRepositoryQuery query) {
		String closed = query.getAttribute(QueryAttribute.CLOSED);
		if (closed == null)
			allCases.setSelection(true);
		else if (closed.equals("false")) {
			openCases.setSelection(true);
		} else {
			return false;
		}
		
		return true;
	}

	public void applyTo(IRepositoryQuery query) {
		if (openCases.getSelection()) {
			query.setAttribute(QueryAttribute.CLOSED, "false");
		} else if (allCases.getSelection()) {
			query.setAttribute(QueryAttribute.CLOSED, null);
		} else {
			// fail!
		}
	}
}
