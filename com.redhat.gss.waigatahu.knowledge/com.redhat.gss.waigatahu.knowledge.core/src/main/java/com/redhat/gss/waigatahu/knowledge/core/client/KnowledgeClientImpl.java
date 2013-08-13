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

package com.redhat.gss.waigatahu.knowledge.core.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.WebUtil;

import com.redhat.gss.strata.model.Solution;
import com.redhat.gss.strata.model.Solutions;
import com.redhat.gss.waigatahu.common.client.CustomerPortalClient;

public class KnowledgeClientImpl implements KnowledgeClient {
	private final JAXBContext jaxbContext;
	private final CustomerPortalClient portal;
	private final AbstractWebLocation baseLocation;

	private static final Class<?>[] XML_CLASSES = new Class[] {
		Solutions.class
	};

	public KnowledgeClientImpl(AbstractWebLocation baseLocation, CustomerPortalClient portal) {
		this.portal = portal;
		this.baseLocation = baseLocation;

		try {
			this.jaxbContext = JAXBContext.newInstance(XML_CLASSES);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public void shutdown() {
		// TODO Auto-generated method stub

	}

	public Solution getSolution(URI uri, IProgressMonitor monitor) {
		GetMethod method = portal.runGetRequest(uri.toString(), baseLocation, monitor);
		try {
			switch (method.getStatusCode()) {
			case HttpURLConnection.HTTP_OK:
				try {
					InputStream is = WebUtil.getResponseBodyAsStream(method, monitor);
				    Unmarshaller um = jaxbContext.createUnmarshaller();
				    return ((Solution) um.unmarshal(is));
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
}
