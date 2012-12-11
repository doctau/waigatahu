package com.redhat.gss.mylyn.rhcp.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.widgets.Composite;

import com.redhat.gss.mylyn.rhcp.core.RhcpCorePlugin;
import com.redhat.gss.mylyn.rhcp.core.client.RhcpClient;
import com.redhat.gss.mylyn.rhcp.core.client.RhcpClientFactory;

public class RhcpTaskRepositoryPage extends AbstractRepositorySettingsPage {
	private final RhcpClientFactory clientFactory;
	
	public RhcpTaskRepositoryPage(String title, String description,
			TaskRepository taskRepository, RhcpClientFactory clientFactory) {
		super(title, description, taskRepository);
		this.clientFactory = clientFactory;
	}

	public String getConnectorKind() {
		return RhcpCorePlugin.CONNECTOR_KIND;
	}

	protected void createAdditionalControls(Composite parent) {
		// TODO: add any custom controls
	}

	public void applyTo(TaskRepository repository) {
		super.applyTo(repository);
		// TODO: store any custom settings
	}

	protected Validator getValidator(TaskRepository repository) {
		return new RhcpSettingsValidator(repository);
	}

	class RhcpSettingsValidator extends Validator {
		private final TaskRepository repository;

		public RhcpSettingsValidator(TaskRepository repository) {
			this.repository = repository;
		}

		public void run(IProgressMonitor monitor) throws CoreException {
			monitor.beginTask("Verifying credentials", IProgressMonitor.UNKNOWN);
			try {
				validate(monitor);
			} finally {
				monitor.done();
			}
		}

		private void validate(IProgressMonitor monitor) throws CoreException {
			try {
				RhcpClient client = clientFactory.getClient(repository);
				client.validateConnection(monitor);
				setStatus(RepositoryStatus.OK_STATUS);
			} catch (Exception e) {
				setStatus(RepositoryStatus.createLoginError(repository.getUrl(), RhcpCorePlugin.CONNECTOR_KIND));
			}
		}
	}
}
