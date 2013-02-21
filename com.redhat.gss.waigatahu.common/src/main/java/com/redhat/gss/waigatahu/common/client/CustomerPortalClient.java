package com.redhat.gss.waigatahu.common.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebUtil;

import com.redhat.gss.strata.model.Values.Value;
import com.redhat.gss.strata.model.Values;
import com.redhat.gss.waigatahu.common.Function1;

public class CustomerPortalClient {
	private static final String USER_AGENT = "Waigatahu 0.0.1";

	public static final Header ACCEPT_XML_HEADER = new Header("Accept", "application/xml");
	public static final Header CONTENT_TYPE_XML_HEADER = new Header("Content-Type", "application/xml");
	public static final Header CONTENT_TYPE_BINARY_HEADER = new Header("Content-Type", "application/octet-stream");
	public static final Header CONTENT_TYPE_TEXT_HEADER = new Header("Content-Type", "text/plain");
	

	public static Function1<Object, List<String>> VALUES_EXTRACTOR = new Function1<Object, List<String>>() {
		public List<String> apply(Object o) {
		    List<String> vs = new ArrayList<String>();
		    for (Value v: ((Values)o).getValue()) {
		    	vs.add(v.getName());
		    }
		    return vs;
		}
	};

	

	protected HttpClient createHttpClient() {
		HttpClient httpClient = new HttpClient();
		httpClient.setHttpConnectionManager(WebUtil.getConnectionManager());
		httpClient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
		WebUtil.configureHttpClient(httpClient, USER_AGENT);
		httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT, USER_AGENT);
		return httpClient;
	}
	
	protected void setupClientAuthentication(HttpClient httpClient, AbstractWebLocation location,
			IProgressMonitor monitor) {
		String repositoryUrl = location.getUrl();

		AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
		AuthScope authScope = new AuthScope(WebUtil.getHost(repositoryUrl), WebUtil.getPort(repositoryUrl),
				null, AuthScope.ANY_SCHEME);
		Credentials httpCredentials = WebUtil.getHttpClientCredentials(credentials,
				WebUtil.getHost(repositoryUrl));
		httpClient.getState().setCredentials(authScope, httpCredentials);
	}

	public GetMethod runGetRequest(String url, AbstractWebLocation location, IProgressMonitor monitor) {
		HttpClient httpClient = createHttpClient();
		setupClientAuthentication(httpClient, location, monitor);

		HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);
		GetMethod method = new GetMethod(WebUtil.getRequestPath(url));
		method.addRequestHeader(ACCEPT_XML_HEADER);
		boolean okay = false;
		try {
			int status = WebUtil.execute(httpClient, hostConfiguration, method, monitor);

			switch (status) {
			case HttpURLConnection.HTTP_UNAUTHORIZED:
			case HttpURLConnection.HTTP_FORBIDDEN:
				throw new LoginException();
			default:
				okay = true;
				return method;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (!okay) {
				WebUtil.releaseConnection(method, monitor);
			}
		}
	}
	
	public PutMethod runPutRequest(String url, AbstractWebLocation location, IProgressMonitor monitor, RequestEntity re) {
		if (url == null)
			throw new IllegalArgumentException("url is null");
		HttpClient httpClient = createHttpClient();
		setupClientAuthentication(httpClient, location, monitor);

		HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);
		PutMethod method = new PutMethod(WebUtil.getRequestPath(url));
		method.addRequestHeader(ACCEPT_XML_HEADER);
		method.addRequestHeader(CONTENT_TYPE_XML_HEADER);
		method.setRequestEntity(re);
		boolean okay = false;
		try {
			int status = WebUtil.execute(httpClient, hostConfiguration, method, monitor);

			switch (status) {
			case HttpURLConnection.HTTP_UNAUTHORIZED:
			case HttpURLConnection.HTTP_FORBIDDEN:
				throw new LoginException();
			default:
				okay = true;
				return method;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (!okay) {
				WebUtil.releaseConnection(method, monitor);
			}
		}
	}


	public PostMethod runPostRequest(String url, AbstractWebLocation location, IProgressMonitor monitor, RequestEntity re, boolean contentTypeXml) {
		Header[] headers = contentTypeXml ? new Header[]{CONTENT_TYPE_XML_HEADER} : new Header[0];
		return runPostRequest(url, location, monitor, re, headers);
	}
	public PostMethod runPostRequest(String url, AbstractWebLocation location, IProgressMonitor monitor, RequestEntity re, Header[] headers) {
		HttpClient httpClient = createHttpClient();
		setupClientAuthentication(httpClient, location, monitor);

		HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);
		PostMethod method = new PostMethod(WebUtil.getRequestPath(url));
		method.addRequestHeader(ACCEPT_XML_HEADER);
		for (int i = 0; i < headers.length; i++)
			method.addRequestHeader(headers[i]);
		method.setRequestEntity(re);
		boolean okay = false;
		try {
			int status = WebUtil.execute(httpClient, hostConfiguration, method, monitor);

			switch (status) {
			case HttpURLConnection.HTTP_UNAUTHORIZED:
			case HttpURLConnection.HTTP_FORBIDDEN:
				throw new LoginException();
			default:
				okay = true;
				return method;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (!okay) {
				WebUtil.releaseConnection(method, monitor);
			}
		}
	}
}
