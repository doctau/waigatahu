package com.redhat.gss.waigatahu.cases.ui.query;

import java.util.Calendar;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.workbench.forms.DatePicker;
import org.eclipse.mylyn.commons.workbench.forms.SectionComposite;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.redhat.gss.waigatahu.cases.core.WaigatahuCaseCorePlugin;
import com.redhat.gss.waigatahu.cases.data.QueryAttribute;


public class CaseQueryPage extends AbstractRepositoryQueryPage2 {
	private Button openCases;
	private Button allCases;
	private DatePicker startDate;
	private DatePicker endDate;

	public CaseQueryPage(String pageName, TaskRepository repository,
			IRepositoryQuery query) {
		super(pageName, repository, query);
	}

	protected void createPageContent(SectionComposite parent) {
		createOpenControls(parent);
		createDateControls(parent);
	}

	private void createOpenControls(SectionComposite parent) {
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

	private void createDateControls(SectionComposite parent) {
		Composite group = new Composite(parent.getContent(), SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		group.setLayout(layout);
		
		startDate = new DatePicker(group, SWT.NONE, DatePicker.LABEL_CHOOSE, false, 0);
		startDate.setVisible(true);
		
		endDate = new DatePicker(group, SWT.NONE, DatePicker.LABEL_CHOOSE, false, 0);
		endDate.setVisible(true);
	}

	protected void doRefreshControls() {
		// TODO update any severity/etc fields
	}

	protected boolean hasRepositoryConfiguration() {
		return true;
	}

	protected boolean restoreState(IRepositoryQuery query) {
		setQueryTitle(query.getSummary());

		String closed = query.getAttribute(QueryAttribute.CLOSED);
		if (closed == null)
			allCases.setSelection(true);
		else if (closed.equals("false")) {
			openCases.setSelection(true);
		} else {
			StatusHandler.log(new Status(IStatus.ERROR, WaigatahuCaseCorePlugin.CONNECTOR_KIND, "Unknown query 'closed' value"));
			return false;
		}
		
		String sdate = query.getAttribute(QueryAttribute.START_DATE);
		if (sdate != null) {
			Calendar scal = Calendar.getInstance();
			scal.setTime(QueryAttribute.Convert.toDate(sdate));
			startDate.setDate(scal);
		}
		
		String edate = query.getAttribute(QueryAttribute.END_DATE);
		if (edate != null) {
			Calendar ecal = Calendar.getInstance();
			ecal.setTime(QueryAttribute.Convert.toDate(edate));
			endDate.setDate(ecal);
		}
		
		/*query.getAttribute(QueryAttribute.CASE_GROUP);
		query.getAttribute(QueryAttribute.SEARCH_TERMS);
		query.getAttribute(QueryAttribute.QUERY_USE_TIME);*/
		
		return true;
	}

	public void applyTo(IRepositoryQuery query) {
		//TODO: query.setUrl(getQueryUrl(getTaskRepository().getRepositoryUrl()));
		query.setSummary(getQueryTitle());
		
		if (openCases.getSelection()) {
			query.setAttribute(QueryAttribute.CLOSED, "false");
		} else if (allCases.getSelection()) {
			query.setAttribute(QueryAttribute.CLOSED, null);
		} else {
			// fail!
		}
		
		Calendar scal = startDate.getDate();
		query.setAttribute(QueryAttribute.START_DATE, (scal != null) ? QueryAttribute.Convert.fromDate(scal.getTime()) : null);
		
		Calendar ecal = endDate.getDate();
		query.setAttribute(QueryAttribute.END_DATE, (ecal != null) ? QueryAttribute.Convert.fromDate(ecal.getTime()) : null);

		/*
		query.setAttribute(QueryAttribute.CASE_GROUP, cg);
		query.setAttribute(QueryAttribute.SEARCH_TERMS, st);
		query.setAttribute(QueryAttribute.QUERY_USE_TIME, null);
		*/
	}
}
