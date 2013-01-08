package com.redhat.gss.waigatahu.cases.data;

import java.util.Date;


public interface QueryAttribute {
	String CLOSED = "rhcp.case.query.closed";
	String CASE_GROUP = "rhcp.case.query.group";
	String SEARCH_TERMS = "rhcp.case.query.terms";
	String START_DATE = "rhcp.case.query.date.start";
	String END_DATE = "rhcp.case.query.date.end";
	String QUERY_USE_TIME = "rhcp.case.query.date.usetime";
	
	public static class Convert {
		public static Date toDate(String s) {
			if (s == null)
				return null;
			else
				return new Date(Long.parseLong(s));
		}

		public static String fromDate(Date d) {
			if (d == null)
				return null;
			else
				return Long.toString(d.getTime());
		}
	}
}
