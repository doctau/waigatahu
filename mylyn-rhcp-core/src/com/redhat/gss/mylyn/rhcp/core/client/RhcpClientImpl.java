package com.redhat.gss.mylyn.rhcp.core.client;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;

import com.redhat.gss.mylyn.rhcp.core.data.RhcpSupportCase;

public class RhcpClientImpl implements RhcpClient {
	private static final String USER_AGENT = "Mylyn RHCP connector client 0.0.1";
	private static final String VALIDATE_PATH = "/";

	private final TaskRepository repository;

	public RhcpClientImpl(TaskRepository repository) {
		this.repository = repository;
	}

	protected HttpClient createHttpClient() {
		HttpClient httpClient = new HttpClient();
		httpClient.setHttpConnectionManager(WebUtil.getConnectionManager());
		httpClient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
		WebUtil.configureHttpClient(httpClient, USER_AGENT);
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


	public void validateConnection(IProgressMonitor monitor) {
		AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);

		HttpClient httpClient = createHttpClient();
		setupClientAuthentication(httpClient, location, monitor);
		

		HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);
		GetMethod method = new GetMethod(WebUtil.getRequestPath(location.getUrl() + VALIDATE_PATH));
		int status;
		try {
			status = WebUtil.execute(httpClient, hostConfiguration, method, monitor);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			WebUtil.releaseConnection(method, monitor);
		}

		switch (status) {
		case HttpURLConnection.HTTP_OK:
			break;
		case HttpURLConnection.HTTP_UNAUTHORIZED:
		case HttpURLConnection.HTTP_FORBIDDEN:
			throw new RuntimeException("authorisation failed");
		default:
			throw new RuntimeException("unexpected result code");
		}
		
	}

	public RhcpSupportCase getCase(long caseId, IProgressMonitor monitor) {
		throw new IllegalArgumentException();
	}
}
