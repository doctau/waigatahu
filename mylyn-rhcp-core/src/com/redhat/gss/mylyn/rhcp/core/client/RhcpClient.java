package com.redhat.gss.mylyn.rhcp.core.client;

import org.eclipse.core.runtime.IProgressMonitor;

import com.redhat.gss.mylyn.rhcp.core.data.RhcpSupportCase;

public interface RhcpClient {
	void validateConnection(IProgressMonitor monitor);
	RhcpSupportCase getCase(long caseId, IProgressMonitor monitor);
}
