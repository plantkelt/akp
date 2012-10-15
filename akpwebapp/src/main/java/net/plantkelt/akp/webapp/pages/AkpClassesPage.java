package net.plantkelt.akp.webapp.pages;

import java.util.List;

import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.AkpParentClassPathLabel;
import net.plantkelt.akp.webapp.components.EditorModel;
import net.plantkelt.akp.webapp.components.InPlaceEditor;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

public class AkpClassesPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private Integer classId;
	private IModel<AkpClass> akpClassModel;

	public AkpClassesPage(PageParameters parameters) {

		// Load data
		classId = parameters.get("xid").toOptionalInteger();
		akpClassModel = new LoadableDetachableModel<AkpClass>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpClass load() {
				return akpTaxonService.getClass(classId);
			}
		};
		AkpClass akpClass = akpClassModel.getObject();
		AkpUser user = AkpWicketSession.get().getAkpUser();
		boolean isFake = akpClass.getXid() == null;
		boolean isAdmin = user != null && user.isAdmin();

		// Parent classes
		AkpParentClassPathLabel parentPathLabel = new AkpParentClassPathLabel(
				"parentPath", akpClass.getParent());
		add(parentPathLabel);

		// Class name
		InPlaceEditor classNameEditor = new InPlaceEditor("classNameEditor",
				new EditorModel<String>() {
					@Override
					public String getObject() {
						return akpClassModel.getObject().getName();
					}

					@Override
					public void saveObject(String name) {
						AkpClass akpClass = akpClassModel.getObject();
						akpClass.setName(name);
						akpTaxonService.updateClass(akpClass);
					}
				});
		add(classNameEditor);
		classNameEditor.setReadOnly(!isAdmin || isFake);
		Label classNameLabel = new Label("className",
				new PropertyModel<String>(akpClassModel, "htmlName"));
		classNameLabel.setEscapeModelStrings(false);
		classNameEditor.add(classNameLabel);

		// Synonyms
		InPlaceEditor synonymsEditor = new InPlaceEditor("classSynonymsEditor",
				new EditorModel<String>() {
					@Override
					public String getObject() {
						return akpClassModel.getObject().getSynonyms();
					}

					@Override
					public void saveObject(String synonyms) {
						AkpClass akpClass = akpClassModel.getObject();
						akpClass.setSynonyms(synonyms);
						akpTaxonService.updateClass(akpClass);
					}
				});
		add(synonymsEditor);
		synonymsEditor.setReadOnly(!isAdmin || isFake);
		Label classSynonyms = new Label("classSynonyms",
				new PropertyModel<String>(akpClassModel, "synonyms"));
		classSynonyms.setEscapeModelStrings(false);
		synonymsEditor.add(classSynonyms);

		// Sub-classes
		RepeatingView subClassesRepeat = new RepeatingView("subClasses");
		add(subClassesRepeat);
		List<AkpClass> subClasses = akpClass.getChildren();
		int i = 0;
		for (AkpClass subClass : subClasses) {
			final int index = i;
			WebMarkupContainer item = new WebMarkupContainer(
					subClassesRepeat.newChildId());
			subClassesRepeat.add(item);
			WebMarkupContainer adminSection = new WebMarkupContainer(
					"adminSection");
			item.add(adminSection);
			adminSection.setVisible(isAdmin);
			Link<Void> downLink = new Link<Void>("downLink") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick() {
					akpTaxonService.moveDownChildClass(
							akpClassModel.getObject(), index);
					refreshPage();
				}
			};
			adminSection.add(downLink);
			Link<AkpClassesPage> subClassLink = AkpClassesPage.link(
					"subClassLink", subClass.getXid());
			Label subClassNameLabel = new Label("subClassName",
					subClass.getName());
			subClassNameLabel.setEscapeModelStrings(false);
			subClassLink.add(subClassNameLabel);
			item.add(subClassLink);
			i++;
		}

		// Add a sub-class button
		Form<Void> form = new Form<Void>("form");
		add(form);
		Button addSubClassButton = new Button("addSubClassButton") {
			private static final long serialVersionUID = 1L;

			public void onSubmit() {
				akpTaxonService.createNewClass(akpClassModel.getObject());
				refreshPage();
			}
		};
		// We need to be admin to add, we can't add a new root class
		addSubClassButton.setVisible(isAdmin && akpClass.getXid() != null);
		form.add(addSubClassButton);

		// Remove class button
		Button removeClassButton = new Button("removeClassButton") {
			private static final long serialVersionUID = 1L;

			public void onSubmit() {
				AkpClass akpClass = akpClassModel.getObject();
				AkpClass parentClass = akpClass.getParent();
				boolean ok = akpTaxonService.deleteClass(akpClass);
				if (ok) {
					if (parentClass == null)
						setResponsePage(AkpClassesPage.class);
					else
						setResponsePage(
								AkpClassesPage.class,
								new PageParameters().add("xid",
										parentClass.getXid()));
				}
			}
		};
		// We need to be admin to remove, we can't remove a non-empty class
		removeClassButton.setVisible(isAdmin
				&& akpTaxonService.canDeleteClass(akpClass));
		form.add(removeClassButton);
		// We can't add a new root class
		addSubClassButton.setVisible(isAdmin && akpClass.getXid() != null);

		// Owned-plants
		RepeatingView ownedPlantsRepeat = new RepeatingView("ownedPlants");
		add(ownedPlantsRepeat);
		for (AkpPlant plant : akpClass.getPlants()) {
			WebMarkupContainer item = new WebMarkupContainer(
					ownedPlantsRepeat.newChildId());
			ownedPlantsRepeat.add(item);
			Link<AkpPlantPage> ownedPlantLink = AkpPlantPage.link(
					"ownedPlantLink", plant.getXid());
			Label ownedPlantNameLabel = new Label("ownedPlantName", plant
					.getMainName().getHtmlName());
			ownedPlantNameLabel.setEscapeModelStrings(false);
			ownedPlantLink.add(ownedPlantNameLabel);
			item.add(ownedPlantLink);
		}
	}

	private void refreshPage() {
		setResponsePage(AkpClassesPage.class,
				new PageParameters().add("xid", classId));
	}

	public static Link<AkpClassesPage> link(String id, Integer xid) {
		if (xid == null)
			return link(id);
		return new BookmarkablePageLink<AkpClassesPage>(id,
				AkpClassesPage.class, new PageParameters().add("xid", xid));
	}

	public static Link<AkpClassesPage> link(String id) {
		return new BookmarkablePageLink<AkpClassesPage>(id,
				AkpClassesPage.class);
	}

}
