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

package com.redhat.gss.waigatahu.cases.core.client;

import java.util.Date;

public class CaseQuery {
	private Boolean closed;
	private String caseGroup;
	private Date startDate;
	private Date endDate;
	private String searchTerms;
	/** Should we use the hour/minute/second for queries too? */
	private boolean queryUseTime;
	

	public Boolean getClosed() {
		return closed;
	}

	public void setClosed(Boolean closed) {
		this.closed = closed;
	}

	public void setClosed(String closed) {
		setClosed(toBoolean(closed));
	}

	public String getCaseGroup() {
		return caseGroup;
	}

	public void setCaseGroup(String caseGroup) {
		this.caseGroup = caseGroup;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getSearchTerms() {
		return searchTerms;
	}

	public void setSearchTerms(String searchTerms) {
		this.searchTerms = searchTerms;
	}

	public boolean isQueryUseTime() {
		return queryUseTime;
	}

	public void setQueryUseTime(boolean queryUseTime) {
		this.queryUseTime = queryUseTime;
	}

	private Boolean toBoolean(String s) {
		return (s == null) ? null : Boolean.valueOf(s);
	}

}
