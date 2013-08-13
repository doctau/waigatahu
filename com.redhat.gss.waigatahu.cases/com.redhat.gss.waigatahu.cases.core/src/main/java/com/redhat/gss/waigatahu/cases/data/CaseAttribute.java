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

package com.redhat.gss.waigatahu.cases.data;

public interface CaseAttribute {
	// case attributes
	String CLOSED = "task.com.redhat.gss.case.closed";
	String ATTACHMENT_PRIVATE = "task.com.redhat.gss.case.private";
	String CASE_URI = "task.com.redhat.gss.case.uri";
	String CASE_TYPE = "task.com.redhat.gss.case.type";
	String ALTERNATE_ID = "task.com.redhat.gss.case.alternateid";
	String FOLDER = "task.com.redhat.gss.case.folder";
	String USER_CONTACT = "task.com.redhat.gss.case.contact.user";
	String USER_LAST_MODIFIER = "task.com.redhat.gss.case.lastmodified.user";
	String ACCOUNT_NUMBER = "task.com.redhat.gss.case.account.number";
	String WEB_URL = "task.com.redhat.gss.case.weburl"; // URL of the customer portal version
	
	// attribute not shown in UI.
	String KIND_HIDDEN = null;
}
