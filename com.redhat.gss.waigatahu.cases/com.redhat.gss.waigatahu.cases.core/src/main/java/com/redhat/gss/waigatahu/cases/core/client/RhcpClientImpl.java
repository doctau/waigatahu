package com.redhat.gss.waigatahu.cases.core.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

import com.redhat.gss.strata.model.Account;
import com.redhat.gss.strata.model.Attachment;
import com.redhat.gss.strata.model.Attachments;
import com.redhat.gss.strata.model.Case;
import com.redhat.gss.strata.model.Cases;
import com.redhat.gss.strata.model.Comment;
import com.redhat.gss.strata.model.Entitlement;
import com.redhat.gss.strata.model.Group;
import com.redhat.gss.strata.model.Groups;
import com.redhat.gss.strata.model.Product;
import com.redhat.gss.strata.model.Products;
import com.redhat.gss.strata.model.User;
import com.redhat.gss.strata.model.Users;
import com.redhat.gss.strata.model.Values;
import com.redhat.gss.strata.model.Values.Value;
import com.redhat.gss.strata.model.Versions;
import com.redhat.gss.waigatahu.cases.core.CaseId;
import com.redhat.gss.waigatahu.cases.util.WebDownloadInputStream;

public class RhcpClientImpl implements RhcpClient {

	private static class AttachmentPartSource implements PartSource {
		private final AbstractTaskAttachmentSource source;
		private final IProgressMonitor monitor;

		private AttachmentPartSource(AbstractTaskAttachmentSource source,
				IProgressMonitor monitor) {
			this.source = source;
			this.monitor = monitor;
		}

		public long getLength() {
			return source.getLength();
		}

		public String getFileName() {
			return source.getName();
		}

		public InputStream createInputStream() throws IOException {
			try {
				return source.createInputStream(monitor);
			} catch (CoreException e) {
				throw new IOException("Could not create stream");
			}
		}
	}

	private static final String USER_AGENT = "Waigatahu 0.0.1";
	private static final String VALIDATE_PATH = "/accounts";
	private static final String ALL_OPEN_CASES_PATH = "/cases/";
	private static final String POST_CASE = "/cases";
	private static final String ALL_CASE_ATTACHMENTS = "/attachments";
	private static final String POST_CASE_ATTACHMENT = "/attachments";
	private static final String POST_CASE_COMMENT = "/comments";
	private static final String VALUES_PRODUCTS = "/products";
	private static final String VALUES_GROUPS = "/groups";
	private static final String VALUES_STATUSES = "/values/case/status";
	private static final String VALUES_SEVERITIES = "/values/case/severity";
	private static final String VALUES_CASE_TYPES = "/values/case/types";
	private static final String ACCOUNT_USERS_SUFFIX = "/users";
	private static final String ACCOUNT_USERS_PREFIX = "/account/";

	private static final Header ACCEPT_XML_HEADER = new Header("Accept", "application/xml");
	private static final Header CONTENT_TYPE_XML_HEADER = new Header("Content-Type", "application/xml");
	
	private static final Class<?>[] XML_CLASSES = new Class[] {
		Account.class,
		Case.class,
		Cases.class,
		Product.class,
		Products.class,
		Values.class,
		Versions.class
	};

	private final TaskRepository repository;
	private final JAXBContext jaxbContext;

	// cached values
	Future<String> accountNumber = null;
	Future<List<Product>> products = null;
	ConcurrentMap<String, Future<List<String>>> versions = new ConcurrentHashMap<String, Future<List<String>>>();
	Future<List<String>> types = null;
	Future<List<String>> severities = null;
	Future<List<String>> statuses = null;
	Future<List<Group>> groups = null;
	ConcurrentMap<String, Future<List<User>>> users = new ConcurrentHashMap<String, Future<List<User>>>();

	public RhcpClientImpl(TaskRepository repository) {
		this.repository = repository;

		try {
			this.jaxbContext = JAXBContext.newInstance(XML_CLASSES);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public void shutdown() {
		//FIXME: do anything here?
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

	public PutMethod runPutRequestPath(String restPath, IProgressMonitor monitor, RequestEntity re) {
		AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);
		return runPutRequestInternal(location.getUrl() + restPath, location, monitor, re);
	}
	public PutMethod runPutRequestUrl(String url, IProgressMonitor monitor, RequestEntity re) {
		AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);
		return runPutRequestInternal(url, location, monitor, re);
	}
	private PutMethod runPutRequestInternal(String url, AbstractWebLocation location, IProgressMonitor monitor, RequestEntity re) {
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

	public PostMethod runPostRequestPath(String restPath, IProgressMonitor monitor, RequestEntity re, boolean contentTypeXml) {
		AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);
		return runPostRequestInternal(location.getUrl() + restPath, location, monitor, re, contentTypeXml);
	}
	public PostMethod runPostRequestUrl(String url, IProgressMonitor monitor, RequestEntity re, boolean contentTypeXml) {
		AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);
		return runPostRequestInternal(url, location, monitor, re, contentTypeXml);
	}
	private PostMethod runPostRequestInternal(String url, AbstractWebLocation location, IProgressMonitor monitor, RequestEntity re, boolean contentTypeXml) {
		HttpClient httpClient = createHttpClient();
		setupClientAuthentication(httpClient, location, monitor);

		HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);
		PostMethod method = new PostMethod(WebUtil.getRequestPath(url));
		method.addRequestHeader(ACCEPT_XML_HEADER);
		if (contentTypeXml)
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


	public void validateConnection(IProgressMonitor monitor) {
		synchronized (this) {
			if (accountNumber!= null && accountNumber.isDone())
				accountNumber = null;
		}
		getAccountNumber(monitor);
	}
	
	public String getAccountNumber(final IProgressMonitor monitor) {
		Future<String> an;
		synchronized (this) {
			if (accountNumber == null) {
				RunnableFuture<String> rf =  new FutureTask<String>(new Callable<String>() {
					public String call() throws Exception {
						GetMethod method = runGetRequestPath(VALIDATE_PATH, monitor);
						try {
							switch (method.getStatusCode()) {
							case HttpURLConnection.HTTP_OK:
								try {
									return method.getResponseBodyAsString();
									/*InputStream is = method.getResponseBodyAsStream();
								    Unmarshaller um = jaxbContext.createUnmarshaller();
								    Account account = (Account) um.unmarshal(is);
								    // FIXME: no data is set on it???
								    return account.getNumber();*/
								} catch (IOException e) {
									throw new RuntimeException(e);
								}/* catch (JAXBException e) {
									throw new RuntimeException(e);
								}*/
							default:
								throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
							}
						} finally {
							WebUtil.releaseConnection(method, monitor);
						}
					}
				});
				accountNumber = rf;
				rf.run();
			}
			an = accountNumber;
		}
		try {
			return an.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<Case> queryCases(CaseQuery query, IProgressMonitor monitor) {
		GetMethod gm = new GetMethod(ALL_OPEN_CASES_PATH);
		List<NameValuePair> queryParams = new ArrayList<NameValuePair>();

		// FIXME: not current supported according to DOC-43310
		if (query.getClosed() == null) {
			
		} else if (query.getClosed()) {
			throw new IllegalStateException("Query on open/closed is not supported yet");
		} else {
			throw new IllegalStateException("Query on open/closed is not supported yet");
		}
		
		if (query.getCaseGroup() != null) {
			queryParams.add(new NameValuePair("group", query.getCaseGroup()));
		}
		if (query.getSearchTerms() != null) {
			queryParams.add(new NameValuePair("query", query.getCaseGroup()));
		}

		//FIXME: support date and time?
		if (query.getStartDate() != null) {
			final DateFormat df;
			if (query.isQueryUseTime())
				df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'");
			else
				df = new SimpleDateFormat("yyyy-MM-dd");
			queryParams.add(new NameValuePair("startDate", df.format(query.getStartDate())));
		}
		
		if (query.getEndDate() != null) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			queryParams.add(new NameValuePair("startDate", df.format(query.getEndDate())));
		}
		
		gm.setQueryString((NameValuePair[]) queryParams.toArray(new NameValuePair[0]));
		GetMethod method = runGetRequestPath(gm.getPath(), monitor);
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
				throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
			}
		} finally {
			WebUtil.releaseConnection(method, monitor);
		}
	}

	public Case getCase(CaseId caseId, IProgressMonitor monitor) {
		GetMethod method = runGetRequestUrl(caseId.getUrl(), monitor);
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
				throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
			}
		} finally {
			WebUtil.releaseConnection(method, monitor);
		}
	}

	public boolean canCreateCases() {
		//TODO: check
		return true;
	}

	public List<Attachment> getCaseAttachments(CaseId caseId, IProgressMonitor monitor) {
		GetMethod method = runGetRequestUrl(caseId.getUrl() + ALL_CASE_ATTACHMENTS, monitor);
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
				throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
			}
		} finally {
			WebUtil.releaseConnection(method, monitor);
		}
	}

	public InputStream streamAttachment(CaseId caseId, String attachmentId,
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

	public void postAttachment(CaseId caseId, String comment, TaskAttribute attribute,
			final AbstractTaskAttachmentSource source, final IProgressMonitor monitor) {
		PostMethod method = null;
		InputStream is = null;
		try {
			//FIXME: add content-type, description etc
			HttpMethodParams params = new HttpMethodParams();

			Part part = new FilePart("file", new AttachmentPartSource(source, monitor), source.getContentType(), null);
			RequestEntity re = new MultipartRequestEntity(new Part[] { part }, params);
			method = runPostRequestUrl(caseId.getUrl() + POST_CASE_ATTACHMENT, monitor, re, false);

			switch (method.getStatusCode()) {
			case HttpURLConnection.HTTP_CREATED:
				// normal response
				String  location = method.getResponseHeader("Location").getValue();
				attribute.createAttribute(TaskAttribute.ATTACHMENT_URL).setValue(location);
				break;
			default:
				throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
			}
		} finally {
			if (method != null)
				WebUtil.releaseConnection(method, monitor);
			method = null;
			
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					//oh no!
				}
			}
		}
	}

	public void postComment(CaseId caseId, String text, TaskAttribute attribute, IProgressMonitor monitor) {
		PostMethod method = null;
		InputStream is = null;
		try {
			Comment comment = new Comment();
			comment.setText(text);
			comment.setPublic(true);
			comment.setDraft(false);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			jaxbContext.createMarshaller().marshal(comment, baos);
			method = runPostRequestUrl(caseId.getUrl() + POST_CASE_COMMENT, monitor, new ByteArrayRequestEntity(baos.toByteArray()), true);

			switch (method.getStatusCode()) {
			case HttpURLConnection.HTTP_CREATED:
				// normal response
				String  location = method.getResponseHeader("Location").getValue();
				attribute.createAttribute(TaskAttribute.COMMENT_URL).setValue(location);
				break;
			default:
				throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
			}
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		} finally {
			if (method != null)
				WebUtil.releaseConnection(method, monitor);
			method = null;
			
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					//oh no!
				}
			}
		}
	}

	public void updateCaseMetadata(CaseId caseId, Case supportCase, IProgressMonitor monitor) {
		PutMethod method = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			jaxbContext.createMarshaller().marshal(supportCase, baos);
			method = runPutRequestUrl(caseId.getUrl(), monitor, new ByteArrayRequestEntity(baos.toByteArray()));

			switch (method.getStatusCode()) {
			case HttpURLConnection.HTTP_ACCEPTED:
				// normal response
				break;
			default:
				throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
			}
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		} finally {
			if (method != null)
				WebUtil.releaseConnection(method, monitor);
		}
	}

	public Case createCase(Case supportCase, IProgressMonitor monitor) {
		PostMethod method = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			jaxbContext.createMarshaller().marshal(supportCase, baos);
			method = runPostRequestPath(POST_CASE, monitor, new ByteArrayRequestEntity(baos.toByteArray()), true);

			switch (method.getStatusCode()) {
			case HttpURLConnection.HTTP_CREATED:
				// normal response
				String  location = method.getResponseHeader("Location").getValue();
				//FIXME: don't parse and un-parse the number from the URL
				return getCase(new CaseId(location), monitor);
			case HttpURLConnection.HTTP_NOT_ACCEPTABLE:
				String err1 = "unknown";
				try {
					err1 = method.getResponseBodyAsString();
				} catch (IOException e) {}
				throw new RuntimeException("Case not accepted by server: " + err1);
			default:
				String err2;
				try {
					Writer writer = new StringWriter();
					jaxbContext.createMarshaller().marshal(supportCase, writer);
					err2 = method.getResponseBodyAsString();
				} catch (IOException e) {}
				throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
			}
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		} finally {
			if (method != null)
				WebUtil.releaseConnection(method, monitor);
			method = null;
		}
	}

	
	/* reference data */
	public void updateData() {
		synchronized (this) {
			products = null;
			types = null;
			severities = null;
			statuses = null;
			groups = null;
			versions.clear();
			users.clear();
		}
	}
	
	public List<Product> getProducts() {
		synchronized (this) {
			if (products == null) {
				RunnableFuture<List<Product>> rf = new FutureTask<List<Product>>(new Callable<List<Product>>() {
					public List<Product> call() throws Exception {
						IProgressMonitor monitor = new NullProgressMonitor();
						GetMethod method = runGetRequestPath(VALUES_PRODUCTS, monitor);
						try {
							switch (method.getStatusCode()) {
							case HttpURLConnection.HTTP_OK:
								try {
									InputStream is = method.getResponseBodyAsStream();
								    Unmarshaller um = jaxbContext.createUnmarshaller();
								    Products products = (Products) um.unmarshal(is);
								    return products.getProduct();
								} catch (IOException e) {
									throw new RuntimeException(e);
								} catch (JAXBException e) {
									throw new RuntimeException(e);
								}
							default:
								throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
							}
						} finally {
							WebUtil.releaseConnection(method, monitor);
						}
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

	public List<Group> getGroups() {
		synchronized (this) {
			if (groups == null) {
				RunnableFuture<List<Group>> rf = new FutureTask<List<Group>>(new Callable<List<Group>>() {
					public List<Group> call() throws Exception {
						IProgressMonitor monitor = new NullProgressMonitor();
						GetMethod method = runGetRequestPath(VALUES_GROUPS, monitor);
						try {
							switch (method.getStatusCode()) {
							case HttpURLConnection.HTTP_OK:
								try {
									InputStream is = method.getResponseBodyAsStream();
								    Unmarshaller um = jaxbContext.createUnmarshaller();
								    Groups products = (Groups) um.unmarshal(is);
								    return products.getGroup();
								} catch (IOException e) {
									throw new RuntimeException(e);
								} catch (JAXBException e) {
									throw new RuntimeException(e);
								}
							default:
								throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
							}
						} finally {
							WebUtil.releaseConnection(method, monitor);
						}
					}
				});
				groups = rf;

				rf.run();
			}
		}
		try {
			return groups.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getVersions(final String product) {
		RunnableFuture<List<String>> nf = new FutureTask<List<String>>(new Callable<List<String>>() {
			public List<String> call() throws Exception {
				IProgressMonitor monitor = new NullProgressMonitor();
				String escProduct = product.replace(" ", "%20"); // FIXME this sucks, but URLEncoder turns spaces into +s
				GetMethod method = runGetRequestPath("/products/" + escProduct + "/versions", monitor);
				try {
					switch (method.getStatusCode()) {
					case HttpURLConnection.HTTP_OK:
						try {
							InputStream is = method.getResponseBodyAsStream();
						    Unmarshaller um = jaxbContext.createUnmarshaller();
						    Versions versions = (Versions) um.unmarshal(is);
						    return versions.getVersion();
						} catch (IOException e) {
							throw new RuntimeException(e);
						} catch (JAXBException e) {
							throw new RuntimeException(e);
						}
					default:
						throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
					}
				} finally {
					WebUtil.releaseConnection(method, monitor);
				}
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
						IProgressMonitor monitor = new NullProgressMonitor();
						GetMethod method = runGetRequestPath(VALUES_CASE_TYPES, monitor);
						try {
							switch (method.getStatusCode()) {
							case HttpURLConnection.HTTP_OK:
								try {
									InputStream is = method.getResponseBodyAsStream();
								    Unmarshaller um = jaxbContext.createUnmarshaller();
								    Values values = (Values) um.unmarshal(is);
								    
								    List<String> vs = new ArrayList<String>();
								    for (Value v: values.getValue()) {
								    	vs.add(v.getName());
								    }
								    // field is optional
								    vs.add("");
								    return vs;
								} catch (IOException e) {
									throw new RuntimeException(e);
								} catch (JAXBException e) {
									throw new RuntimeException(e);
								}
							default:
								throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
							}
						} finally {
							WebUtil.releaseConnection(method, monitor);
						}
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
						IProgressMonitor monitor = new NullProgressMonitor();
						GetMethod method = runGetRequestPath(VALUES_SEVERITIES, monitor);
						try {
							switch (method.getStatusCode()) {
							case HttpURLConnection.HTTP_OK:
								try {
									InputStream is = method.getResponseBodyAsStream();
								    Unmarshaller um = jaxbContext.createUnmarshaller();
								    Values values = (Values) um.unmarshal(is);
								    
								    List<String> vs = new ArrayList<String>();
								    for (Value v: values.getValue()) {
								    	vs.add(v.getName());
								    }
								    return vs;
								} catch (IOException e) {
									throw new RuntimeException(e);
								} catch (JAXBException e) {
									throw new RuntimeException(e);
								}
							default:
								throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
							}
						} finally {
							WebUtil.releaseConnection(method, monitor);
						}
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
						IProgressMonitor monitor = new NullProgressMonitor();
						GetMethod method = runGetRequestPath(VALUES_STATUSES, monitor);
						try {
							switch (method.getStatusCode()) {
							case HttpURLConnection.HTTP_OK:
								try {
									InputStream is = method.getResponseBodyAsStream();
								    Unmarshaller um = jaxbContext.createUnmarshaller();
								    Values values = (Values) um.unmarshal(is);
								    
								    List<String> vs = new ArrayList<String>();
								    for (Value v: values.getValue()) {
								    	vs.add(v.getName());
								    }
								    return vs;
								} catch (IOException e) {
									throw new RuntimeException(e);
								} catch (JAXBException e) {
									throw new RuntimeException(e);
								}
							default:
								throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
							}
						} finally {
							WebUtil.releaseConnection(method, monitor);
						}
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
	
	public List<User> getUsers(final String accountNumber) {
		RunnableFuture<List<User>> nf = new FutureTask<List<User>>(new Callable<List<User>>() {
			public List<User> call() throws Exception {
				IProgressMonitor monitor = new NullProgressMonitor();
				GetMethod method = runGetRequestPath(ACCOUNT_USERS_PREFIX + accountNumber + ACCOUNT_USERS_SUFFIX, monitor);
				try {
					switch (method.getStatusCode()) {
					case HttpURLConnection.HTTP_OK:
						try {
							InputStream is = method.getResponseBodyAsStream();
						    Unmarshaller um = jaxbContext.createUnmarshaller();
						    Users users = (Users) um.unmarshal(is);
						    return users.getUser();
						} catch (IOException e) {
							throw new RuntimeException(e);
						} catch (JAXBException e) {
							throw new RuntimeException(e);
						}
					default:
						throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
					}
				} finally {
					WebUtil.releaseConnection(method, monitor);
				}
			}
		});
		Future<List<User>> f = users.putIfAbsent(accountNumber, nf);
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
}
