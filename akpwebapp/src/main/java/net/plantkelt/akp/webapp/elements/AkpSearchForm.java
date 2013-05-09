package net.plantkelt.akp.webapp.elements;

import java.util.List;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.domain.AkpSearchData;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.AdminMarkupContainer;
import net.plantkelt.akp.webapp.components.LoggedInMarkupContainer;
import net.plantkelt.akp.webapp.models.BrEnFrStringModel;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

public class AkpSearchForm extends Panel {
	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private Form<AkpSearchData> form;
	private IModel<List<AkpClass>> familiesModel;
	private IModel<List<AkpBib>> bibsModel;

	public AkpSearchForm(String id, IModel<AkpSearchData> searchDataModel) {
		super(id);

		// Main form
		form = new Form<AkpSearchData>("form",
				new CompoundPropertyModel<AkpSearchData>(searchDataModel));
		add(form);

		// Admin/logged-in/public containers
		WebMarkupContainer taxonNameRow = new WebMarkupContainer("taxonNameRow");
		form.add(taxonNameRow);
		WebMarkupContainer includeSynonymsRow = new LoggedInMarkupContainer(
				"includeSynonymsRow");
		form.add(includeSynonymsRow);
		WebMarkupContainer plantOriginRow = new LoggedInMarkupContainer(
				"plantOriginRow");
		form.add(plantOriginRow);
		WebMarkupContainer plantCommentsRow = new AdminMarkupContainer(
				"plantCommentsRow");
		form.add(plantCommentsRow);
		WebMarkupContainer plantFamilyRow = new LoggedInMarkupContainer(
				"plantFamilyRow");
		form.add(plantFamilyRow);
		WebMarkupContainer vernacularNameRow = new LoggedInMarkupContainer(
				"vernacularNameRow");
		form.add(vernacularNameRow);
		WebMarkupContainer langSelectRow = new LoggedInMarkupContainer(
				"langSelectRow");
		form.add(langSelectRow);
		WebMarkupContainer vernacularNameBibRow = new LoggedInMarkupContainer(
				"vernacularNameBibRow");
		form.add(vernacularNameBibRow);
		WebMarkupContainer vernacularNameCommentsRow = new LoggedInMarkupContainer(
				"vernacularNameCommentsRow");
		form.add(vernacularNameCommentsRow);

		// Simple stuff
		taxonNameRow.add(new TextField<String>("taxonName"));
		includeSynonymsRow.add(new CheckBox("includeSynonyms"));
		plantOriginRow.add(new TextField<String>("plantOrigin"));
		plantCommentsRow.add(new TextField<String>("plantComments"));
		vernacularNameRow.add(new TextField<String>("vernacularName"));
		vernacularNameCommentsRow.add(new TextField<String>(
				"vernacularNameComments"));

		// Families
		familiesModel = new LoadableDetachableModel<List<AkpClass>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpClass> load() {
				return akpTaxonService.getFamilies();
			}
		};
		IModel<AkpClass> familyModel = new IModel<AkpClass>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void detach() {
			}

			@Override
			public AkpClass getObject() {
				Integer xid = form.getModelObject().getFamilyXid();
				return xid == null ? null : akpTaxonService.getClass(xid);
			}

			@Override
			public void setObject(AkpClass family) {
				if (family == null) {
					form.getModelObject().setFamilyXid(null);
				} else {
					if (family.getLevel() != AkpClass.LEVEL_FAMILY)
						throw new IllegalArgumentException(
								"Invalid level for a family: "
										+ family.getLevel());
					form.getModelObject().setFamilyXid(family.getXid());
				}
			}

		};
		DropDownChoice<AkpClass> familySelect = new DropDownChoice<AkpClass>(
				"plantFamily", familyModel, familiesModel,
				new IChoiceRenderer<AkpClass>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(AkpClass akpClass) {
						return akpClass.getTextName();
					}

					@Override
					public String getIdValue(AkpClass akpClass, int index) {
						return akpClass.getXid().toString();
					}
				});
		plantFamilyRow.add(familySelect);
		familySelect.setNullValid(true);

		// Vernacular languages
		RepeatingView langListRepeat = new RepeatingView("langList");
		List<AkpLang> langs = akpTaxonService.getLangList();
		for (AkpLang lang : langs) {
			WebMarkupContainer item = new WebMarkupContainer(
					langListRepeat.newChildId());
			langListRepeat.add(item);

			WebMarkupContainer langLink = new WebMarkupContainer("langNameLink");
			item.add(langLink);
			Label langCodeLabel = new Label("langCode", lang.getCode());
			langLink.add(langCodeLabel);
			Label langTitleLabel = new Label("langTitle",
					new BrEnFrStringModel(lang.getName()));
			langLink.add(langTitleLabel);
			Label langDescLabel = new Label("langDesc", new BrEnFrStringModel(
					lang.getDesc()));
			langDescLabel.setEscapeModelStrings(false);
			langLink.add(langDescLabel);

			final String langXid = lang.getXid();
			IModel<Boolean> langSelectModel = new IModel<Boolean>() {
				private static final long serialVersionUID = 1L;

				@Override
				public void detach() {
				}

				@Override
				public Boolean getObject() {
					return form.getModelObject().getLangXids()
							.contains(langXid);
				}

				@Override
				public void setObject(Boolean set) {
					if (set)
						form.getModelObject().getLangXids().add(langXid);
					else
						form.getModelObject().getLangXids().remove(langXid);
				}
			};
			CheckBox langCheckBox = new CheckBox("langCheckbox",
					langSelectModel);
			item.add(langCheckBox);
		}
		langSelectRow.add(langListRepeat);

		// Bib ref.
		bibsModel = new LoadableDetachableModel<List<AkpBib>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpBib> load() {
				return akpTaxonService.getBibs();
			}
		};
		IModel<AkpBib> bibModel = new IModel<AkpBib>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void detach() {
			}

			@Override
			public AkpBib getObject() {
				String xid = form.getModelObject().getBibRefXid();
				return xid == null ? null : akpTaxonService.getBib(xid);
			}

			@Override
			public void setObject(AkpBib bib) {
				form.getModelObject().setBibRefXid(bib.getXid());
			}

		};
		DropDownChoice<AkpBib> bibSelect = new DropDownChoice<AkpBib>(
				"vernacularNameBib", bibModel, bibsModel,
				new IChoiceRenderer<AkpBib>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(AkpBib bib) {
						return bib.getShortName();
					}

					@Override
					public String getIdValue(AkpBib bib, int index) {
						return bib.getXid();
					}
				});
		vernacularNameBibRow.add(bibSelect);
		bibSelect.setNullValid(true);

		// Buttons
		Button searchButton = new Button("searchButton");
		form.add(searchButton);
		Button luckyButton = new Button("luckyButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				// TODO
			}
		};
		form.add(luckyButton);
	}
}
