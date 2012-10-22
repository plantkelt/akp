package net.plantkelt.akp.webapp.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.service.AkpTaxonService;

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

public class AkpPlantRefAdderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private WebMarkupContainer openSection;
	private Form<Void> form;
	private IModel<String> plantRefEntryModel;

	public AkpPlantRefAdderPanel(String id,
			final AkpPlantRefAdderListener adderModel) {
		super(id);

		plantRefEntryModel = new Model<String>("");

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
				target.add(form, openSection);
			}
		};
		openSection.add(newLink);

		// Add form
		form = new Form<Void>("form");
		add(form);
		form.setOutputMarkupId(true);
		form.setOutputMarkupPlaceholderTag(true);
		AutoCompleteTextField<String> plantRefSelect = new AutoCompleteTextField<String>(
				"plantRefSelect", plantRefEntryModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected Iterator<String> getChoices(String fill) {
				if (fill.length() <= 4)
					return new ArrayList<String>(0).iterator();
				List<AkpPlant> plants = akpTaxonService
						.searchPlantFromName(fill);
				List<String> retval = new ArrayList<String>(plants.size());
				for (AkpPlant plant : plants)
					retval.add(plant.getXid() + " - "
							+ plant.getMainName().getTextName());
				Collections.sort(retval);
				return retval.iterator();
			}
		};
		form.add(plantRefSelect);
		form.add(new AjaxSubmitLink("addButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String plantName = plantRefEntryModel.getObject();
				if (plantName != null && plantName.matches("^[0-9]+ - .*")) {
					String xidStr = plantName.substring(0,
							plantName.indexOf(" - "));
					int xid = Integer.parseInt(xidStr);
					AkpPlant targetPlant = akpTaxonService.getPlant(xid);
					if (targetPlant != null) {
						adderModel.onPlantRefAdded(target, targetPlant);
					}
				}
				plantRefEntryModel.setObject(null);
			}
		});
		form.add(new AjaxSubmitLink("cancelButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				plantRefEntryModel.setObject(null);
				openSection.setVisible(true);
				form.setVisible(false);
				target.add(form, openSection);
			}
		});
		form.setVisible(false);

	}
}
