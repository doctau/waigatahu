package com.redhat.gss.waigatahu.diagnostics.core.client;

import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;

import com.redhat.gss.waigatahu.diagnostics.core.data.DiagnosticResults;

public interface DiagnosticsClient {
	void shutdown();

	DiagnosticResults diagnose(byte[] bs, IProgressMonitor monitor);
	DiagnosticResults diagnose(InputStream is, IProgressMonitor monitor);
}
