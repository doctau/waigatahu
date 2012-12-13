package com.redhat.gss.waigatahu.cases.core.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

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
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

import com.redhat.gss.waigatahu.cases.util.Utils;
import com.redhat.gss.waigatahu.cases.util.WebDownloadInputStream;
import com.redhat.gss.strata.model.Attachment;
import com.redhat.gss.strata.model.Attachments;
import com.redhat.gss.strata.model.Case;
import com.redhat.gss.strata.model.Cases;
import com.redhat.gss.strata.model.Product;

public class RhcpClientImpl implements RhcpClient {
	private static final String USER_AGENT = "Mylyn RHCP connector client 0.0.1";
	private static final String VALIDATE_PATH = "/accounts/defaultAccount";
	private static final String ALL_OPEN_CASES_PATH = "/cases/";
	private static final String CASE_PREFIX = "/cases/";
	private static final String ALL_CASE_ATTACHMENTS = "/attachments";
	
	private static final Header ACCEPT_XML_HEADER = new Header("Accept", "application/xml");

	private final TaskRepository repository;
	private final RhcpClientFactory factory;
	private final JAXBContext jaxbContext;
	
	// cached values
	Future<List<Product>> products = null;
	ConcurrentMap<String, Future<List<String>>> versions = new ConcurrentHashMap<String, Future<List<String>>>();
	Future<List<String>> types = null;
	Future<List<String>> severities = null;
	Future<List<String>> statuses = null;

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

	public GetMethod runGetRequestPath(String restPath, IProgressMonitor monitor) {
		AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);
		return runGetRequestInternal(location.getUrl() + restPath, location, monitor);
	}
	public GetMethod runGetRequestUrl(String url, IProgressMonitor monitor) {
		AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);
		return runGetRequestInternal(url, location, monitor);
	}
	private GetMethod runGetRequestInternal(String url, AbstractWebLocation location, IProgressMonitor monitor) {
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
		GetMethod method = runGetRequestPath(VALIDATE_PATH, monitor);
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
		GetMethod method = runGetRequestPath(ALL_OPEN_CASES_PATH, monitor);
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
		GetMethod method = runGetRequestPath(CASE_PREFIX + caseId, monitor);
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

	public List<Attachment> getCaseAttachments(String caseId, IProgressMonitor monitor) {
		GetMethod method = runGetRequestPath(CASE_PREFIX + caseId + ALL_CASE_ATTACHMENTS, monitor);
		try {
			switch (method.getStatusCode()) {
			case HttpURLConnection.HTTP_OK:
				try {
					InputStream is = WebUtil.getResponseBodyAsStream(method, monitor);
	
				    Unmarshaller um = jaxbContext.createUnmarshaller();
				    return ((Attachments) um.unmarshal(is)).getAttachment();
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

	public InputStream streamAttachment(String caseId, String attachmentId,
			String url, IProgressMonitor monitor) {
		GetMethod method = runGetRequestUrl(url, monitor);
		switch (method.getStatusCode()) {
		case HttpURLConnection.HTTP_OK:
			try {
				return new WebDownloadInputStream(method, monitor);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		default:
			WebUtil.releaseConnection(method, monitor);
			throw new RuntimeException("unexpected result code: " + method.getStatusCode());
		}
	}

	public void postAttachment(String caseId, String comment,
			TaskAttribute attribute, AbstractTaskAttachmentSource source,
			IProgressMonitor monitor) {
		throw new IllegalArgumentException();
	}

	public List<Product> getProducts() {
		synchronized (this) {
			if (products == null) {
				RunnableFuture<List<Product>> rf =  new FutureTask<List<Product>>(new Callable<List<Product>>() {
					public List<Product> call() throws Exception {
						List<Product> ps = new ArrayList<Product>();
						
						//FIXME: talk to the API
						Product p = new Product();
						p.setCode("FIXME");
						p.setName("FIXME");
						ps.add(p);
						
						return ps;
					}
				});
				products = rf;

				rf.run();
			}
		}
		try {
			return products.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getVersions(final String product) {
		RunnableFuture<List<String>> nf = new FutureTask<List<String>>(new Callable<List<String>>() {
			public List<String> call() throws Exception {
				List<String> ps = new ArrayList<String>();
				
				//FIXME: talk to the API
				ps.add("FIXME");

				return ps;
			}
		});
		Future<List<String>> f = versions.putIfAbsent(product, nf);
		if (f == null) {
			f = nf;
			nf.run();
		}

		try {
			return f.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getTypes() {
		synchronized (this) {
			if (types == null) {
				RunnableFuture<List<String>> rf =  new FutureTask<List<String>>(new Callable<List<String>>() {
					public List<String> call() throws Exception {
						List<String> ps = new ArrayList<String>();
						
						//FIXME: talk to the API?
						ps.add("Bug");
						ps.add("Feature");
						ps.add("Info");
						ps.add("Other");
						
						return ps;
					}
				});
				types = rf;

				rf.run();
			}
		}
		try {
			return types.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getSeverities() {
		synchronized (this) {
			if (severities == null) {
				RunnableFuture<List<String>> rf =  new FutureTask<List<String>>(new Callable<List<String>>() {
					public List<String> call() throws Exception {
						List<String> ps = new ArrayList<String>();

						//FIXME: talk to the API?
						ps.add("4 (Low)");
						ps.add("3 (Normal)");
						ps.add("2 (High)");
						ps.add("1 (Urgent)");

						return ps;
					}
				});
				severities = rf;

				rf.run();
			}
		}
		try {
			return severities.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getStatuses() {
		synchronized (this) {
			if (statuses == null) {
				RunnableFuture<List<String>> rf =  new FutureTask<List<String>>(new Callable<List<String>>() {
					public List<String> call() throws Exception {
						List<String> ps = new ArrayList<String>();
						
						//FIXME: talk to the API?
						ps.add("Waiting on Red Hat");
						ps.add("Waiting on Customer");
						ps.add("Closed");
						
						return ps;
					}
				});
				statuses = rf;

				rf.run();
			}
		}
		try {
			return statuses.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}
