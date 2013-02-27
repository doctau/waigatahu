package com.redhat.gss.waigatahu.knowledge.core.client;

import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;

import com.redhat.gss.strata.model.Solution;

public interface KnowledgeClient {
	void shutdown();
	
	Solution getSolution(URI uri, IProgressMonitor monitor);
}
