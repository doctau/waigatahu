package com.redhat.gss.waigatahu.diagnostics.core.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.WebUtil;

import com.redhat.gss.strata.model.Problems;
import com.redhat.gss.waigatahu.common.client.CustomerPortalClient;

public class DiagnosticsClientImpl implements DiagnosticsClient {
	private final JAXBContext jaxbContext;
	private final CustomerPortalClient portal;
	private final AbstractWebLocation baseLocation;

	private static final Class<?>[] XML_CLASSES = new Class[] {
		Problems.class
	};

	public DiagnosticsClientImpl(AbstractWebLocation baseLocation) {
		this.portal = new CustomerPortalClient();
		this.baseLocation = baseLocation;

		try {
			this.jaxbContext = JAXBContext.newInstance(XML_CLASSES);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public PostMethod runPostRequestPath(String restPath, IProgressMonitor monitor, RequestEntity re,  Header[] headers) {
		return portal.runPostRequest(baseLocation.getUrl() + restPath, baseLocation, monitor, re, headers);
	}


	public void shutdown() {
		//FIXME: do anything here?
	}

	public Problems diagnose(byte[] bs, IProgressMonitor monitor) {
		return diagnoseInternal(new ByteArrayRequestEntity(bs), monitor);
	}

	public Problems diagnose(InputStream is, IProgressMonitor monitor) {
		return diagnoseInternal(new InputStreamRequestEntity(is), monitor);
	}
	
	protected Problems diagnoseInternal(RequestEntity re, IProgressMonitor monitor) {
		PostMethod method = null;
		try {
			Header[] headers = new Header[] {CustomerPortalClient.CONTENT_TYPE_BINARY_HEADER};
			method = runPostRequestPath("/problems", monitor, re, headers);

			switch (method.getStatusCode()) {
			case HttpURLConnection.HTTP_OK:
				try {
					InputStream ris = WebUtil.getResponseBodyAsStream(method, monitor);
				    Unmarshaller um = jaxbContext.createUnmarshaller();
				    return ((Problems) um.unmarshal(ris));
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (JAXBException e) {
					throw new RuntimeException(e);
				}
			default:
				throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
			}
		} finally {
			if (method != null)
				WebUtil.releaseConnection(method, monitor);
		}
	}

}
