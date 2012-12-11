package com.redhat.gss.mylyn.rhcp.core.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
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

import com.redhat.gss.mylyn.rhcp.util.Utils;
import com.redhat.gss.strata.model.Case;
import com.redhat.gss.strata.model.Cases;

public class RhcpClientImpl implements RhcpClient {
	private static final String USER_AGENT = "Mylyn RHCP connector client 0.0.1";
	private static final String VALIDATE_PATH = "/accounts/defaultAccount";
	private static final String ALL_OPEN_CASES_PATH = "/cases/";
	private static final String CASE_PREFIX = "/cases/";
	
	private static final Header ACCEPT_XML_HEADER = new Header("Accept", "application/xml");

	private final TaskRepository repository;
	private final RhcpClientFactory factory;
	private final JAXBContext jaxbContext;

	public RhcpClientImpl(TaskRepository repository, RhcpClientFactory factory) {
		this.repository = repository;
		this.factory = factory;

		try {
			this.jaxbContext = JAXBContext.newInstance(Cases.class, Case.class);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public String getRepositoryUrlFromCaseUrl(String taskUrl) {
		return factory.getRepositoryUrlFromCaseUrl(taskUrl);
	}

	public long getCaseNumberFromCaseUrl(String taskUrl) {
		return factory.getCaseNumberFromCaseUrl(taskUrl);
	}

	public String getCaseUrl(String repositoryUrl, long caseNumber) {
		return factory.getCaseUrl(repositoryUrl, caseNumber);
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
	
	public GetMethod runGetRequest(String restPath, IProgressMonitor monitor) {
		AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);
		HttpClient httpClient = createHttpClient();
		setupClientAuthentication(httpClient, location, monitor);

		HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);
		GetMethod method = new GetMethod(WebUtil.getRequestPath(location.getUrl() + restPath));
		method.addRequestHeader(ACCEPT_XML_HEADER);
		boolean okay = false;
		try {
			int status = WebUtil.execute(httpClient, hostConfiguration, method, monitor);

			switch (status) {
			case HttpURLConnection.HTTP_UNAUTHORIZED:
			case HttpURLConnection.HTTP_FORBIDDEN:
				throw new RuntimeException("authorisation failed");
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


	public void validateConnection(IProgressMonitor monitor) {
		GetMethod method = runGetRequest(VALIDATE_PATH, monitor);
		try {
			switch (method.getStatusCode()) {
			case HttpURLConnection.HTTP_OK:
				try {
					InputStream is = method.getResponseBodyAsStream();
					// FIXME: verify data
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				break;
			default:
				throw new RuntimeException("unexpected result code");
			}
		} finally {
			WebUtil.releaseConnection(method, monitor);
		}
	}

	public Collection<Case> getAllOpenCases(RhcpClient client,
			IProgressMonitor monitor) {
		GetMethod method = runGetRequest(ALL_OPEN_CASES_PATH, monitor);
		try {
			switch (method.getStatusCode()) {
			case HttpURLConnection.HTTP_OK:
				try {
					InputStream is = WebUtil.getResponseBodyAsStream(method, monitor);
				    Unmarshaller um = jaxbContext.createUnmarshaller();
				    Cases cases = (Cases) um.unmarshal(is);
					return cases.getCase();
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (JAXBException e) {
					throw new RuntimeException(e);
				}
			default:
				throw new RuntimeException("unexpected result code");
			}
		} finally {
			WebUtil.releaseConnection(method, monitor);
		}
	}

	public Case getCase(long caseNumber, IProgressMonitor monitor) {
		String caseId = Utils.padStringLeft(8, '0', Long.toString(caseNumber));
		GetMethod method = runGetRequest(CASE_PREFIX + caseId, monitor);
		try {
			switch (method.getStatusCode()) {
			case HttpURLConnection.HTTP_OK:
				try {
					InputStream is = WebUtil.getResponseBodyAsStream(method, monitor);
	
				    Unmarshaller um = jaxbContext.createUnmarshaller();
				    return (Case) um.unmarshal(is);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (JAXBException e) {
					throw new RuntimeException(e);
				}
			default:
				throw new RuntimeException("unexpected result code: " + method.getStatusCode());
			}
		} finally {
			WebUtil.releaseConnection(method, monitor);
		}
	}

	public boolean canCreateCases() {
		//TODO: check
		return true;
	}
}
