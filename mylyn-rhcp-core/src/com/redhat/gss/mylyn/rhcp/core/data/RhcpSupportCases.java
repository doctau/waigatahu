package com.redhat.gss.mylyn.rhcp.core.data;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement(name="cases", namespace="http://www.redhat.com/gss/strata")
@XmlSeeAlso(RhcpSupportCase.class)
@XmlAccessorType(XmlAccessType.FIELD)
public class RhcpSupportCases {
	@XmlElementRef(name="case", type=RhcpSupportCase.class)
	private List<RhcpSupportCase> cases;

	public List<RhcpSupportCase> getCases() {
		return cases;
	}
}
