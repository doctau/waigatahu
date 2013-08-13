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

package com.redhat.gss.waigatahu.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractTaskHyperlinkDetector;
import org.eclipse.mylyn.tasks.ui.TaskHyperlink;

import com.redhat.gss.waigatahu.cases.core.WaigatahuCaseCorePlugin;

public class CaseHyperlinkDetector extends AbstractTaskHyperlinkDetector {
	private static final Pattern CASE_PATTERN = Pattern.compile("case *([0-9]+)", Pattern.CASE_INSENSITIVE); 
	private static final Pattern BUGZILLA_PATTERN = Pattern.compile("(bug|bz) *([0-9]+)", Pattern.CASE_INSENSITIVE); 
	private static final Pattern JIRA_PATTERN = Pattern.compile("([A-Z]+-[0-9]*+)"); 
	
	protected List<IHyperlink> detectHyperlinks(ITextViewer textViewer,
			String content, int index, int contentOffset) {
		TaskRepository repository = getTaskRepository(textViewer);
		List<IHyperlink> links = new ArrayList<IHyperlink>();

		collectCaseLinks(content, index, contentOffset, repository, links);
		TaskRepository bugzillaRepository = TasksUiPlugin.getRepositoryManager().getRepository("bugzilla", "https://bugzilla.redhat.com");
		collectBugzillaLinks(content, index, contentOffset, bugzillaRepository, links);
		TaskRepository jiraRepository = TasksUiPlugin.getRepositoryManager().getRepository("jira", "https://issues.jboss.org");
		collectJiraLinks(content, index, contentOffset, jiraRepository, links);

		return links;
	}


	private void collectCaseLinks(String content, int index, int contentOffset,
			TaskRepository repository, List<IHyperlink> links) {
		Matcher m = CASE_PATTERN.matcher(content);
		while (m.find()) {
			if (isInRegion(index, m)) {
				String id = m.group(1);
				String url = "rhcp:" + repository.getUrl() + "/cases/" + id;
				links.add(new TaskHyperlink(determineRegion(contentOffset, m), repository, url));
			}
		}
	}

	private void collectBugzillaLinks(String content, int index,
			int contentOffset, TaskRepository repository, List<IHyperlink> links) {
		Matcher m = BUGZILLA_PATTERN.matcher(content);
		while (m.find()) {
			if (isInRegion(index, m)) {
				String id = m.group(2);
				if (repository != null) {
					links.add(new TaskHyperlink(determineRegion(contentOffset, m), repository, id));
				} else {
					links.add(new URLHyperlink(determineRegion(contentOffset, m), "https://bugzilla.redhat.com/show_bug.cgi?id="  + id));
				}
			}
		}
	}


	private void collectJiraLinks(String content, int index,
			int contentOffset, TaskRepository repository, List<IHyperlink> links) {
		Matcher m = JIRA_PATTERN.matcher(content);
		while (m.find()) {
			if (isInRegion(index, m)) {
				String id = m.group(1);
				if (repository != null) {
					links.add(new TaskHyperlink(determineRegion(contentOffset, m), repository, id));
				} else {
					links.add(new URLHyperlink(determineRegion(contentOffset, m), "https://issues.jboss.org/browse/" + id));
				}
			}
		}
	}


	private static boolean isInRegion(int offsetInText, Matcher m) {
		return (offsetInText == -1) || (offsetInText >= m.start() && offsetInText <= m.end());
	}

	private static IRegion determineRegion(int textOffset, Matcher m) {
		return new Region(textOffset + m.start(), m.end() - m.start());
	}
}
