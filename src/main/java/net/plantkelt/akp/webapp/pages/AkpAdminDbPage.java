package net.plantkelt.akp.webapp.pages;

import java.util.ArrayList;
import java.util.List;

import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.service.AkpTaxonService;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.inject.Inject;

@AuthorizeInstantiation("ADMIN")
public class AkpAdminDbPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	public AkpAdminDbPage() {

		add(new FeedbackPanel("feedback"));
		Form<Void> form = new Form<Void>("form");
		add(form);

		final IModel<String> lang1Model = new Model<String>();
		final IModel<String> lang2Model = new Model<String>();

		form.add(new LangDropDown("lang1", lang1Model));
		form.add(new LangDropDown("lang2", lang2Model));

		form.add(new Button("mergeLang") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				String lang1 = lang1Model.getObject();
				String lang2 = lang2Model.getObject();
				if (lang1 == null || lang2 == null) {
					warn("Please select 2 lang to process.");
				} else {
					akpTaxonService.mergeLang(lang1, lang2);
					info("Merge OK!");
				}
			}
		});
		
		form.add(new Button("addAuthNameAsSource") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onSubmit() {
				akpTaxonService.addAuthNameAsSource();
			}
		});
	}

	private class LangDropDown extends DropDownChoice<String> {
		private static final long serialVersionUID = 1L;

		public LangDropDown(String id, final IModel<String> langXidModel) {
			super(id, langXidModel, convertLangList(akpTaxonService
					.getLangList(AkpUser.PROFILE_ADMIN)), new IChoiceRenderer<String>() {
				private static final long serialVersionUID = 1L;

				@Override
				public Object getDisplayValue(String langXid) {
					return langXid;
				}

				@Override
				public String getIdValue(String langXid, int index) {
					return langXid;
				}
			});
		}

	}

	private static List<String> convertLangList(List<AkpLang> langList) {
		List<String> retval = new ArrayList<String>(langList.size());
		for (AkpLang lang : langList) {
			retval.add(lang.getXid());
		}
		return retval;
	}
}
