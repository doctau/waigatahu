package com.redhat.gss.waigatahu.diagnostics.ui.editor;

import java.io.Serializable;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;

import com.redhat.gss.strata.model.Link;
import com.redhat.gss.strata.model.Problem;
import com.redhat.gss.strata.model.Source;

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

		Composite editorComposite = managedForm.getForm().getBody();
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 0;
		editorComposite.setLayout(layout);
		

		DiagnostisResultsEditorInput input = (DiagnostisResultsEditorInput)getEditorInput();
		
		for (Serializable s: input.getProblems().getSourceOrLinkOrProblem()) {
			if (s instanceof Problem) {
				Problem problem = (Problem)s;
				for (Serializable s2: problem.getSourceOrLink()) {
					if (s2 instanceof Source) {
						Source source = (Source)s2;
					} else if (s2 instanceof Link) {
						Link link = (Link)s2;
					} else {
						// unknown
					}
				}
			} else if (s instanceof Link) {
				Link link = (Link)s;
				
				
				SectionPart part = new SectionPart(editorComposite, managedForm.getToolkit(), 0);
				Section section = part.getSection();
				Hyperlink hl = managedForm.getToolkit().createHyperlink(section, "example", 0);
				hl.addHyperlinkListener(new HyperlinkAdapter() {
					public void linkActivated(HyperlinkEvent e) {
						new Object();
					}
				});
				managedForm.addPart(part);
				
			}  else if (s instanceof Source) {
				Source source = (Source)s;
				
			} else {
				// unknown
			}
		}
		
	}
}
