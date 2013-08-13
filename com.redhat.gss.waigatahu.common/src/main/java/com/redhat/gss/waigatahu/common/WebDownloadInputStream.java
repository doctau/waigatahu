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

package com.redhat.gss.waigatahu.common;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpMethodBase;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.WebUtil;

public class WebDownloadInputStream extends FilterInputStream {
	private final HttpMethodBase method;
	private final IProgressMonitor monitor;

	public WebDownloadInputStream(HttpMethodBase method, IProgressMonitor monitor) throws IOException {
		super(open(method, monitor));
		this.method = method;
		this.monitor = monitor;
	}

	public void close() throws IOException {
		try {
			super.close();
		} finally {
			WebUtil.releaseConnection(method, monitor);
		}
	}

	private static InputStream open(HttpMethodBase method, IProgressMonitor monitor) throws IOException {
		try {
			return WebUtil.getResponseBodyAsStream(method, monitor);
		} catch (IOException e) {
			WebUtil.releaseConnection(method, monitor);
			throw e;
		}
	}

}
