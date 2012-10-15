package net.plantkelt.akp.webapp.pages;

import java.util.List;

import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.AkpParentClassPathLabel;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

public class AkpClassesPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private Integer classId;
	private IModel<AkpClass> akpClassModel;

	public AkpClassesPage(PageParameters parameters) {
		classId = parameters.get("xid").toOptionalInteger();
		akpClassModel = new LoadableDetachableModel<AkpClass>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpClass load() {
				return akpTaxonService.getClass(classId);
			}
		};
		AkpClass akpClass = akpClassModel.getObject();
		boolean isAdmin = AkpWicketSession.get().getAkpUser().isAdmin();
		// Parent classes
		AkpParentClassPathLabel parentPathLabel = new AkpParentClassPathLabel(
				"parentPath", akpClass.getParent());
		add(parentPathLabel);
		// Class name
		Label classNameLabel = new Label("className", akpClass.getHtmlName());
		classNameLabel.setEscapeModelStrings(false);
		add(classNameLabel);
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
		// Add a class form
		Form<Void> addClassForm = new Form<Void>("addClassForm") {
			private static final long serialVersionUID = 1L;

			public void onSubmit() {
				akpTaxonService.createNewClass(akpClassModel.getObject());
				refreshPage();
			}
		};
		// We can't add a new root class
		addClassForm.setVisible(isAdmin && akpClass.getXid() != null);
		add(addClassForm);
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
