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

package com.redhat.gss.waigatahu.common;

public class Utils {
	public static String padStringLeft(int n, char c, String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n - s.length(); i++) {
			sb.append(c);
		}
		sb.append(s);
		return sb.toString();
	}

	public static String padStringRight(int n, char c, String s) {
		StringBuilder sb = new StringBuilder();
		sb.append(s);
		for (int i = 0; i < n - s.length(); i++) {
			sb.append(c);
		}
		return sb.toString();
	}
}
