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

import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;

import com.redhat.gss.waigatahu.cases.ui.query.CaseQueryPage;

public class CaseQueryWizard extends RepositoryQueryWizard {
	private final String QUERY_PAGE_DESCRIPTION = "QUERY_PAGE_DESCRIPTION";
	
	private IRepositoryQuery query;

	public CaseQueryWizard(TaskRepository repository, IRepositoryQuery query) {
		super(repository);
		this.query = query;
	}
    public void addPages() {
		addPage(new CaseQueryPage(QUERY_PAGE_DESCRIPTION, getTaskRepository(), query));
    }

}
