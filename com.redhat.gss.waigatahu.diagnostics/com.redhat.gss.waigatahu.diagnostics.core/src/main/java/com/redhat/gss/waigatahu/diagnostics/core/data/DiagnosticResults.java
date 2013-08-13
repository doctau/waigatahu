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
