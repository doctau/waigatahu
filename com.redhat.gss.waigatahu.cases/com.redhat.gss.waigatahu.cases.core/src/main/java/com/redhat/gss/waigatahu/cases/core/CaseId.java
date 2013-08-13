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

package com.redhat.gss.waigatahu.cases.core;

public class CaseId {
	private String url;
	
	public CaseId(String url) {
		if (url == null)
			throw new IllegalArgumentException();
		this.url = url;
	}

	public String getUrl() {
		return url;
	}
}
