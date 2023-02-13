package net.plantkelt.akp.webapp.pages;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpUserRoles;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.EditorModel;
import net.plantkelt.akp.webapp.components.InPlaceEditor;
import net.plantkelt.akp.webapp.components.InPlaceSelector;
import net.plantkelt.akp.webapp.components.SelectorModel;
import net.plantkelt.akp.webapp.elements.AkpClassSelectPanel;
import net.plantkelt.akp.webapp.elements.AkpParentClassPathLabel;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

public class AkpClassPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private Integer classId;
	private IModel<AkpClass> akpClassModel;

	public AkpClassPage(PageParameters parameters) {

		// Class quickSearch
		final AkpClassSelectPanel quickSearch = new AkpClassSelectPanel(
				"quickSearch") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onClassSelected(AkpClass clazz) {
				setResponsePage(AkpClassPage.class,
						new PageParameters().add("xid", clazz.getXid()));
			}
		};
		add(quickSearch);

		// Main panel, hidden when result are present
		WebMarkupContainer mainPanel = new WebMarkupContainer("mainPanel") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !quickSearch.hasResult();
			}
		};
		add(mainPanel);

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
		boolean isFake = akpClass.getXid() == null;
		boolean isAdmin = AkpWicketSession.get()
				.hasRole(AkpUserRoles.ROLE_ADMIN);

		// Parent classes
		AkpParentClassPathLabel parentPathLabel = new AkpParentClassPathLabel(
				"parentPath",
				new PropertyModel<AkpClass>(akpClassModel, "parent"));
		mainPanel.add(parentPathLabel);

		// Class name
		InPlaceEditor classNameEditor = new InPlaceEditor("classNameEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return akpClassModel.getObject().getName();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String name) {
						AkpClass akpClass = akpClassModel.getObject();
						akpClass.setName(name);
						akpTaxonService.updateClass(akpClass);
					}
				}, 1, 60);
		mainPanel.add(classNameEditor);
		classNameEditor.setReadOnly(!isAdmin || isFake);
		Label classNameLabel = new Label("className",
				new PropertyModel<String>(akpClassModel, "htmlName"));
		classNameLabel.setEscapeModelStrings(false);
		classNameEditor.add(classNameLabel);

		// Level
		InPlaceSelector<Integer> levelEditor = new InPlaceSelector<Integer>(
				"levelEditor", new SelectorModel<Integer>() {
					private static final long serialVersionUID = 1L;

					public Integer getObject() {
						return akpClassModel.getObject().getLevel();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							Integer level) {
						AkpClass akpClass = akpClassModel.getObject();
						akpClass.setLevel(level);
						akpTaxonService.updateClass(akpClass);
					}

					@Override
					public String getDisplayValue(Integer level) {
						return getString("class.level." + level);
					}

					@Override
					public String getIdValue(Integer level) {
						return "" + level;
					}

					@Override
					public List<Integer> getValues() {
						return akpTaxonService.getClassLevels();
					}
				});
		mainPanel.add(levelEditor);
		levelEditor.setReadOnly(!isAdmin || isFake);
		Label levelLabel = new Label("levelName", new IModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				int level = akpClassModel.getObject().getLevel();
				return getString("class.level." + level);
			}
		});
		levelEditor.add(levelLabel);

		// Comments
		InPlaceEditor commentsEditor = new InPlaceEditor("classCommentsEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return akpClassModel.getObject().getComments();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String comments) {
						AkpClass akpClass = akpClassModel.getObject();
						akpClass.setComments(comments);
						akpTaxonService.updateClass(akpClass);
					}
				}, 1, 60);
		mainPanel.add(commentsEditor);
		commentsEditor.setReadOnly(!isAdmin || isFake);
		Label classComments = new Label("classComments",
				new PropertyModel<String>(akpClassModel, "comments"));
		classComments.setEscapeModelStrings(false);
		commentsEditor.add(classComments);

		// Synonyms
		InPlaceEditor synonymsEditor = new InPlaceEditor("classSynonymsEditor",
				new EditorModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return akpClassModel.getObject().getSynonyms();
					}

					@Override
					public void saveObject(AjaxRequestTarget target,
							String synonyms) {
						AkpClass akpClass = akpClassModel.getObject();
						akpClass.setSynonyms(synonyms);
						akpTaxonService.updateClass(akpClass);
					}
				}, 1, 60);
		mainPanel.add(synonymsEditor);
		synonymsEditor.setReadOnly(!isAdmin || isFake);
		Label classSynonyms = new Label("classSynonyms",
				new PropertyModel<String>(akpClassModel, "htmlSynonyms"));
		classSynonyms.setEscapeModelStrings(false);
		synonymsEditor.add(classSynonyms);

		// Generic form
		Form<Void> form = new Form<Void>("form");
		mainPanel.add(form);

		// Add a sub-class button
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
						setResponsePage(AkpClassPage.class);
					else
						setResponsePage(AkpClassPage.class, new PageParameters()
								.add("xid", parentClass.getXid()));
				}
			}
		};
		// We need to be admin to remove, we can't remove a non-empty class
		removeClassButton.setVisible(
				isAdmin && akpTaxonService.canDeleteClass(akpClass));
		form.add(removeClassButton);
		// We can't add a new root class
		addSubClassButton.setVisible(isAdmin && akpClass.getXid() != null);

		// Add a plant button
		Button addPlantButton = new Button("addPlantButton") {
			private static final long serialVersionUID = 1L;

			public void onSubmit() {
				AkpPlant plant = akpTaxonService
						.createNewPlant(akpClassModel.getObject());
				setResponsePage(AkpPlantPage.class,
						new PageParameters().add("xid", plant.getXid()));
			}
		};
		// We need to be admin to add, we can't add a plant in root class
		addPlantButton.setVisible(isAdmin && akpClass.getXid() != null);
		form.add(addPlantButton);

		// Sub-classes
		IModel<List<AkpClass>> subClassesModel = new LoadableDetachableModel<List<AkpClass>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpClass> load() {
				List<AkpClass> children = akpClassModel.getObject()
						.getChildren();
				Collections.sort(children);
				return children;
			}
		};
		ListView<AkpClass> subClassesListView = new ListView<AkpClass>(
				"subClasses", subClassesModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpClass> item) {
				AkpClass subClass = item.getModelObject();
				final int index = item.getIndex();
				WebMarkupContainer adminSection = new WebMarkupContainer(
						"adminSection");
				item.add(adminSection);
				boolean isAdmin = AkpWicketSession.get()
						.hasRole(AkpUserRoles.ROLE_ADMIN);
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
				Link<AkpClassPage> subClassLink = AkpClassPage
						.link("subClassLink", subClass.getXid());
				Label subClassNameLabel = new Label("subClassName",
						subClass.getTextName().isEmpty() ? "(empty)"
								: subClass.getName());
				subClassNameLabel.setEscapeModelStrings(false);
				subClassLink.add(subClassNameLabel);
				item.add(subClassLink);
			}

			@Override
			public boolean isVisible() {
				return !getModelObject().isEmpty();
			}
		};
		mainPanel.add(subClassesListView);

		// Owned-plants
		IModel<List<AkpPlant>> ownedPlantsModel = new PropertyModel<List<AkpPlant>>(
				akpClassModel, "plantsSorted");
		ListView<AkpPlant> ownedPlantsListView = new ListView<AkpPlant>(
				"ownedPlants", ownedPlantsModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpPlant> item) {
				AkpPlant plant = item.getModelObject();
				Link<AkpPlantPage> ownedPlantLink = AkpPlantPage
						.link("ownedPlantLink", plant.getXid());
				Label ownedPlantNameLabel = new Label("ownedPlantName",
						plant.getMainName().getHtmlName());
				ownedPlantNameLabel.setEscapeModelStrings(false);
				ownedPlantLink.add(ownedPlantNameLabel);
				item.add(ownedPlantLink);
			}

			@Override
			public boolean isVisible() {
				return !getModelObject().isEmpty();
			}
		};
		mainPanel.add(ownedPlantsListView);
	}

	private void refreshPage() {
		if (classId == null)
			setResponsePage(AkpClassPage.class);
		else
			setResponsePage(AkpClassPage.class,
					new PageParameters().add("xid", classId));
	}

	public static Link<AkpClassPage> link(String id, Integer xid) {
		if (xid == null)
			return link(id);
		return new BookmarkablePageLink<AkpClassPage>(id, AkpClassPage.class,
				new PageParameters().add("xid", xid));
	}

	public static Link<AkpClassPage> link(String id) {
		return new BookmarkablePageLink<AkpClassPage>(id, AkpClassPage.class);
	}

}
