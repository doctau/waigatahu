package com.redhat.gss.waigatahu.diagnostics.core.client;

import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;

import com.redhat.gss.strata.model.Problems;

public interface DiagnosticsClient {
	void shutdown();

	Problems diagnose(byte[] bs, IProgressMonitor monitor);
	Problems diagnose(InputStream is, IProgressMonitor monitor);
}
