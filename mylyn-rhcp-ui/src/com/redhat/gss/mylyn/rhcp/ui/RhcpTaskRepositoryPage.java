package com.redhat.gss.mylyn.rhcp.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.widgets.Composite;

import com.redhat.gss.mylyn.rhcp.core.RhcpCorePlugin;

public class RhcpTaskRepositoryPage extends AbstractRepositorySettingsPage {
	public RhcpTaskRepositoryPage(String title, String description,
			TaskRepository taskRepository) {
		super(title, description, taskRepository);
	}

	public String getConnectorKind() {
		return RhcpCorePlugin.CONNECTOR_KIND;
	}

	protected void createAdditionalControls(Composite parent) {
		// TODO Auto-generated method stub
		
	}

	public void applyTo(TaskRepository repository) {
		super.applyTo(repository);
		// TODO: store settings
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
			//TODO: validate settings
		}
	}
}
