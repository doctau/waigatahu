package com.redhat.gss.waigatahu.cases.recommendation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.tasks.core.AbstractDuplicateDetector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import com.redhat.gss.waigatahu.cases.core.CaseRepositoryConnector;
import com.redhat.gss.waigatahu.cases.core.WaigatahuCaseCorePlugin;
import com.redhat.gss.waigatahu.cases.core.client.RhcpClient;

public class DuplicateDetector extends AbstractDuplicateDetector {
	public DuplicateDetector() {
		
	}

	private CaseRepositoryConnector getConnector() {
		//FIXME: how to get this?
		return WaigatahuCaseCorePlugin.getConnector();
	}

	public IRepositoryQuery getDuplicatesQuery(TaskRepository repository,
			TaskData taskData) throws CoreException {
		RhcpClient client = getConnector().getClient(repository);
		//FIXME: implement this
		return null;
	}
}
