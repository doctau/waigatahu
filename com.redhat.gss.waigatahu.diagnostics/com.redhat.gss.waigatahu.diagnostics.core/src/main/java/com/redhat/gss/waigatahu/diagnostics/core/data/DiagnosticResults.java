package com.redhat.gss.waigatahu.diagnostics.core.data;

import com.redhat.gss.strata.model.Problems;

public class DiagnosticResults {
	private final Problems problems;

	public DiagnosticResults(Problems problems) {
		this.problems = problems;
	}

	public Problems getProblems() {
		return problems;
	}
}
