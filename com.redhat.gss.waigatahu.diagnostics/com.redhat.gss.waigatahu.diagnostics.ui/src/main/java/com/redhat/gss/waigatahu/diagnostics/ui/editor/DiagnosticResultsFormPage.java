package com.redhat.gss.waigatahu.diagnostics.ui.editor;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.statushandlers.StatusManager;

import com.redhat.gss.strata.model.Link;
import com.redhat.gss.strata.model.Problem;
import com.redhat.gss.strata.model.Source;
import com.redhat.gss.waigatahu.diagnostics.core.WaigatahuDiagnosticsCorePlugin;
import com.redhat.gss.waigatahu.knowledge.ui.KnowledgeUIPlugin;

public class DiagnosticResultsFormPage extends FormPage {
	public static final String ID = "com.redhat.gss.waigatahu.diagnostics.ui.editor.DiagnosticResultsFormPage";

	public DiagnosticResultsFormPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	public DiagnosticResultsFormPage(String id, String title) {
		super(id, title);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);

		Composite body = managedForm.getForm().getBody();
		managedForm.getForm().setText("Diagnosis Results");
		GridLayout layout = new GridLayout();
		body.setLayout(layout);

		FormToolkit toolkit = managedForm.getToolkit();
		DiagnostisResultsEditorInput input = (DiagnostisResultsEditorInput)getEditorInput();
		
		for (Serializable s: input.getResults().getProblems().getSourceOrLinkOrProblem()) {
			if (s instanceof Problem) {
				Problem problem = (Problem)s;
				Section section = toolkit.createSection(body, SWT.WRAP);
				section.setText("Problem");
				GridLayout slayout = new GridLayout();
				section.setLayout(slayout);

				for (Serializable s2: problem.getSourceOrLink()) {
					if (s2 instanceof Source) {
						createSource(body, toolkit, (Source)s2);
					} else if (s2 instanceof Link) {
						createLink(body, toolkit, (Link)s2);
					} else {
						// unknown
					}
				}
			} else if (s instanceof Link) {
				createLink(body, toolkit, (Link)s);
			}  else if (s instanceof Source) {
				createSource(body, toolkit, (Source)s);
			} else {
				// unknown
			}
		}
	}

	private void createSource(Composite body, FormToolkit toolkit, Source source) {
		Text text = toolkit.createText(body, source.getName(), SWT.WRAP); //FIXME: should be section not body
		text.setToolTipText(source.getValue());
	}

	private void createLink(Composite body, FormToolkit toolkit, Link link) {
		Hyperlink hl = toolkit.createHyperlink(body, link.getValue(), SWT.WRAP); //FIXME: should be section not body
		hl.setHref(link.getUri());
		hl.setToolTipText(link.getExplanation());
		hl.addHyperlinkListener(HYPERLINK_RESPONDER);
	}

	private static HyperlinkAdapter HYPERLINK_RESPONDER = new HyperlinkAdapter() {
		public void linkActivated(HyperlinkEvent ev) {
			String uri = (String) ev.getHref();
			KnowledgeUIPlugin.getDefault().createKnowledgeEditor(uri, ev.getLabel());
		}
	};
}
