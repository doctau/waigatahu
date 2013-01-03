package com.redhat.gss.waigatahu.cases.core;

public class CaseId {
	private String url;
	
	public CaseId(String url) {
		if (url == null)
			throw new IllegalArgumentException();
		this.url = url;
	}

	public String getUrl() {
		return url;
	}
}
