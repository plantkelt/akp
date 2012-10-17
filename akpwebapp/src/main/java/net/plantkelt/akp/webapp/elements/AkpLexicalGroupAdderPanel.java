package net.plantkelt.akp.webapp.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.inject.Inject;

public class AkpLexicalGroupAdderPanel extends Panel {

	@Inject
	private AkpTaxonService akpTaxonService;

	private static final long serialVersionUID = 1L;

	private IModel<String> addLangModel;
	private IModel<Integer> addCorrectModel;

	public AkpLexicalGroupAdderPanel(String id,
			final Component refreshComponent, final IModel<AkpPlant> plantModel) {
		super(id);

		Form<Void> form = new Form<Void>("form");
		add(form);

		// Lang select
		List<AkpLang> langs = akpTaxonService.getLangList();
		List<String> langXids = new ArrayList<String>();
		for (AkpLang lang : langs)
			langXids.add(lang.getXid());
		Collections.sort(langXids);
		addLangModel = new Model<String>(AkpWicketSession.get()
				.getSessionData().getDefaultLangXid());
		DropDownChoice<String> langSelect = new DropDownChoice<String>(
				"langSelect", addLangModel, langXids);
		form.add(langSelect);

		// Correct select
		List<Integer> corrects = new ArrayList<Integer>();
		for (int correct = 0; correct <= AkpLexicalGroup.MAX_CORRECT; correct++)
			corrects.add(correct);
		addCorrectModel = new Model<Integer>(0);
		DropDownChoice<Integer> correctSelect = new DropDownChoice<Integer>(
				"correctSelect", addCorrectModel, corrects,
				new IChoiceRenderer<Integer>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(Integer correct) {
						return AkpLexicalGroup.getCorrectDisplayCode(correct);
					}

					@Override
					public String getIdValue(Integer correct, int index) {
						return correct.toString();
					}
				});
		form.add(correctSelect);

		// Add button
		form.add(new AjaxSubmitLink("addLexicalGroupButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				AkpPlant plant = plantModel.getObject();
				Integer correct = addCorrectModel.getObject();
				String langXid = addLangModel.getObject();
				if (correct != null && langXid != null) {
					AkpLang lang = akpTaxonService.getLang(langXid);
					if (lang != null) {
						akpTaxonService.createNewLexicalGroup(plant, lang,
								correct);
						AkpWicketSession.get().getSessionData()
								.setDefautLangXid(lang.getXid());
						target.add(refreshComponent);
					}
				}
			}
		});

	}
}
