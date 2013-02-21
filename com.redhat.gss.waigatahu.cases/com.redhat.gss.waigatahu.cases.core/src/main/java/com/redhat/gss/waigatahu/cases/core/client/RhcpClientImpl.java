package com.redhat.gss.waigatahu.cases.core.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
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
import com.redhat.gss.strata.model.Versions;
import com.redhat.gss.waigatahu.cases.core.CaseId;
import com.redhat.gss.waigatahu.common.Function1;
import com.redhat.gss.waigatahu.common.Id;
import com.redhat.gss.waigatahu.common.WebDownloadInputStream;
import com.redhat.gss.waigatahu.common.client.CustomerPortalClient;

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
	private static final String GET_ACCOUNT_NUMBER = "/accounts";
	private static final String ALL_OPEN_CASES_PATH = "/cases";
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

	private static final Class<?>[] XML_CLASSES = new Class[] {
		Account.class,
		Attachment.class,
		Attachments.class,
		Case.class,
		Cases.class,
		Comment.class,
		Entitlement.class,
		Group.class,
		Groups.class,
		Product.class,
		Products.class,
		Users.class,
		User.class,
		Values.class,
		Versions.class
	};

	private final TaskRepository repository;
	private final JAXBContext jaxbContext;
	private final CustomerPortalClient portal;

	// cached values
	private Id<Future<String>> accountNumber = new Id<Future<String>>();
	private Id<Future<List<Product>>> products = new Id<Future<List<Product>>>();
	private ConcurrentMap<String, Id<Future<List<String>>>> versions = new ConcurrentHashMap<String, Id<Future<List<String>>>>();
	private Id<Future<List<String>>> severities = new Id<Future<List<String>>>();
	private Id<Future<List<String>>> types = new Id<Future<List<String>>>();
	private Id<Future<List<String>>> statuses = new Id<Future<List<String>>>();
	private Id<Future<List<Group>>> groups = new Id<Future<List<Group>>>();
	private ConcurrentMap<String, Id<Future<List<User>>>> users = new ConcurrentHashMap<String, Id<Future<List<User>>>>();

	public RhcpClientImpl(TaskRepository repository) {
		this.repository = repository;
		this.portal = new CustomerPortalClient();

		try {
			this.jaxbContext = JAXBContext.newInstance(XML_CLASSES);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public void shutdown() {
		//FIXME: do anything here?
	}

	public GetMethod runGetRequestPath(String restPath, IProgressMonitor monitor) {
		AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);
		return portal.runGetRequest(location.getUrl() + restPath, location, monitor);
	}
	public GetMethod runGetRequestUrl(String url, IProgressMonitor monitor) {
		AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);
		return portal.runGetRequest(url, location, monitor);
	}

	public PutMethod runPutRequestPath(String restPath, IProgressMonitor monitor, RequestEntity re) {
		AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);
		return portal.runPutRequest(location.getUrl() + restPath, location, monitor, re);
	}
	public PutMethod runPutRequestUrl(String url, IProgressMonitor monitor, RequestEntity re) {
		AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);
		return portal.runPutRequest(url, location, monitor, re);
	}

	public PostMethod runPostRequestPath(String restPath, IProgressMonitor monitor, RequestEntity re, boolean contentTypeXml) {
		AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);
		return portal.runPostRequest(location.getUrl() + restPath, location, monitor, re, contentTypeXml);
	}
	public PostMethod runPostRequestUrl(String url, IProgressMonitor monitor, RequestEntity re, boolean contentTypeXml) {
		AbstractWebLocation location = new TaskRepositoryLocationFactory().createWebLocation(repository);
		return portal.runPostRequest(url, location, monitor, re, contentTypeXml);
	}


	public void validateConnection(IProgressMonitor monitor) {
		synchronized (accountNumber) {
			if (accountNumber.value != null && accountNumber.value.isDone())
				accountNumber.value = null;
		}
		getAccountNumber(monitor);
	}
	


	public String getAccountNumber(IProgressMonitor monitor) {
		return getData(this.accountNumber, GET_ACCOUNT_NUMBER, monitor, new Function1<HttpMethod, String>() {
			public String apply(HttpMethod m) {
				try {
					return m.getResponseBodyAsString();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	public String getContactName(IProgressMonitor monitor) {
		AuthenticationCredentials creds = repository.getCredentials(AuthenticationType.REPOSITORY);
		return creds.getUserName();
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
			products.value = null;
			types.value = null;
			severities.value = null;
			statuses.value = null;
			groups.value = null;
			versions.clear();
			users.clear();
		}
	}

	public List<Product> getProducts() {
		return getXmlData(this.products, VALUES_PRODUCTS, new NullProgressMonitor(), new Function1<Object, List<Product>>() {
			public List<Product> apply(Object o) {
				return ((Products)o).getProduct();
			}
		});
	}
	
	public List<Group> getGroups() {
		return getXmlData(this.groups, VALUES_GROUPS, new NullProgressMonitor(), new Function1<Object, List<Group>>() {
			public List<Group> apply(Object o) {
				return ((Groups)o).getGroup();
			}
		});
	}
	
	public List<String> getVersions(final String product) {
		String escProduct = product.replace(" ", "%20"); // FIXME this sucks, but URLEncoder turns spaces into +s

		Id<Future<List<String>>> id = new Id<Future<List<String>>>();
		Id<Future<List<String>>> v = versions.putIfAbsent(escProduct, id);
		if (v == null)
			v = id;

		return getXmlData(v, "/products/" + escProduct + "/versions", new NullProgressMonitor(), new Function1<Object, List<String>>() {
			public List<String> apply(Object o) {
				return ((Versions)o).getVersion();
			}
		});
	}

	public List<String> getTypes() {
		return getXmlData(this.types, VALUES_CASE_TYPES, new NullProgressMonitor(), CustomerPortalClient.VALUES_EXTRACTOR);
	}
	
	public List<String> getSeverities() {
		return getXmlData(this.severities, VALUES_SEVERITIES, new NullProgressMonitor(), CustomerPortalClient.VALUES_EXTRACTOR);
	}
	
	public List<String> getStatuses() {
		return getXmlData(this.statuses, VALUES_STATUSES, new NullProgressMonitor(), CustomerPortalClient.VALUES_EXTRACTOR);
	}

	
	public List<User> getUsers(final String accountNumber) {
		Id<Future<List<User>>> id = new Id<Future<List<User>>>();
		Id<Future<List<User>>> v = users.putIfAbsent(accountNumber, id);
		if (v == null)
			v = id;
		return getXmlData(v, ACCOUNT_USERS_PREFIX + accountNumber + ACCOUNT_USERS_SUFFIX,
				new NullProgressMonitor(), new Function1<Object, List<User>>() {
			public List<User> apply(Object o) {
				return ((Users)o).getUser();
			}
		});
	}

	protected <A> A getXmlData(Id<Future<A>> holder, final String path,
			final IProgressMonitor monitor, final Function1<Object, A> mapper) {
		return getData(holder, path, monitor, new Function1<HttpMethod, A>() {
			public A apply(HttpMethod method) {
				try {
					InputStream is = method.getResponseBodyAsStream();
				    Unmarshaller um = jaxbContext.createUnmarshaller();
				    return mapper.apply(um.unmarshal(is));
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (JAXBException e) {
					String ss = null;
					try {
						ss = method.getResponseBodyAsString();
					} catch (IOException ex) {
						ss = ss;
					}
					throw new RuntimeException(e);
				}
			}
		});
	}

	protected <A> A getData(Id<Future<A>> holder, final String path,
			final IProgressMonitor monitor, final Function1<HttpMethod, A> mapper) {
		synchronized (holder) {
			if (holder.value == null) {
				RunnableFuture<A> rf =  new FutureTask<A>(new Callable<A>() {
					public A call() throws Exception {
						GetMethod method = runGetRequestPath(path, monitor);
						try {
							switch (method.getStatusCode()) {
							case HttpURLConnection.HTTP_OK:
								return mapper.apply(method);
							default:
								throw new RuntimeException("unexpected result code: " + method.getStatusCode() + " from " + method.getPath());
							}
						} finally {
							WebUtil.releaseConnection(method, monitor);
						}
					}
				});
				holder.value = rf;
				rf.run();
			}
		}
		try {
			return holder.value.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean isUserRedHat(String userId) {
		//FIXME: don't hard-code this
		// for the relevant fields, this is their full name not their username
		if (userId.startsWith("rhn-"))
			return true;
		else
			return false;
	}
	
	public URL getAccountManagementUrl() {
		try {
			//FIXME: don't hard-code
			return new URL("https://www.redhat.com/wapps/ugc/protected/usermgt/userList.html");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public URL getUserProfileUrl() {
		try {
			//FIXME: don't hard-code
			return new URL("https://www.redhat.com/wapps/ugc/protected/personalInfo.html");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
