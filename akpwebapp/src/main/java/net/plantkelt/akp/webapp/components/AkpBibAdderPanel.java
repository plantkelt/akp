package net.plantkelt.akp.webapp.components;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpLoginService;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.google.inject.Inject;

public class AkpBibAdderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;
	@Inject
	private AkpLoginService akpLoginService;

	private WebMarkupContainer openSection;
	private Form<Void> form;
	private IModel<String> bibEntryModel;

	public AkpBibAdderPanel(String id,
			final IModel<AkpVernacularName> vernaNameModel,
			final Component refreshComponent) {
		super(id);

		bibEntryModel = new Model<String>("");
		
		// Open section
		openSection = new WebMarkupContainer("openSection");
		add(openSection);
		openSection.setOutputMarkupId(true);
		openSection.setOutputMarkupPlaceholderTag(true);
		AjaxLink<Void> newLink = new AjaxLink<Void>("newLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				openSection.setVisible(false);
				form.setVisible(true);
				AkpUser user = AkpWicketSession.get().getAkpUser();
				bibEntryModel.setObject(user == null ? "" : user.getLastbib());
				target.add(form, openSection);
			}
		};
		openSection.add(newLink);

		// Add form
		form = new Form<Void>("form");
		add(form);
		form.setOutputMarkupId(true);
		form.setOutputMarkupPlaceholderTag(true);
		AutoCompleteTextField<String> bibSelect = new AutoCompleteTextField<String>(
				"bibSelect", bibEntryModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected Iterator<String> getChoices(String fill) {
				if (fill.length() <= 2)
					return null;
				List<String> bibEntries = akpTaxonService
						.getBibIdsStartingWith(fill);
				Collections.sort(bibEntries);
				return bibEntries.iterator();
			}
		};
		form.add(bibSelect);
		form.add(new AjaxSubmitLink("addButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				AkpBib bib = akpTaxonService.getBib(bibEntryModel.getObject());
				AkpVernacularName vernaName = vernaNameModel.getObject();
				if (bib != null && !vernaName.getBibs().contains(bib)) {
					vernaName.getBibs().add(bib);
					akpTaxonService.updateVernacularName(vernaName);
					AkpUser user = AkpWicketSession.get().getAkpUser();
					if (user != null) {
						user.setLastbib(bib.getXid());
						akpLoginService.updateUser(user);
					}
				}
				bibEntryModel.setObject("");
				target.add(refreshComponent);
			}
		});
		form.add(new AjaxSubmitLink("cancelButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				bibEntryModel.setObject("");
				openSection.setVisible(true);
				form.setVisible(false);
				target.add(form, openSection);
			}
		});
		form.setVisible(false);

	}
}
