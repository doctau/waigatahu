package com.redhat.gss.mylyn.rhcp.core.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "case", namespace="http://www.redhat.com/gss/strata")
@XmlAccessorType(XmlAccessType.FIELD)
public class RhcpSupportCase {
	@XmlAttribute(name="caseNumber")
	private long caseNumber;

	public RhcpSupportCase() {
		this.caseNumber = -1;
	}

	public RhcpSupportCase(final long caseNumber) {
		this.caseNumber = caseNumber;
	}

	public long getCaseNumber() {
		return caseNumber;
	}
	
	public void setCaseNumber(long caseNumber) {
		this.caseNumber = caseNumber;
	}
}
