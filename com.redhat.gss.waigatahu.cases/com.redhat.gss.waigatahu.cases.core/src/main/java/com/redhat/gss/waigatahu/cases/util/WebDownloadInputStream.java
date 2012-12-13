package com.redhat.gss.waigatahu.cases.util;

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
