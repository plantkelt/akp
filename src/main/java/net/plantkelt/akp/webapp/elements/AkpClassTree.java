package net.plantkelt.akp.webapp.elements;

import java.util.List;

import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.service.AkpTaxonService;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;

import com.google.inject.Inject;

public class AkpClassTree extends Panel {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private IModel<AkpClass> akpClassModel;

	public AkpClassTree(String id, IModel<AkpClass> classModel) {
		super(id);
		akpClassModel = classModel;

		Label className = new Label("className",
				new PropertyModel<String>(akpClassModel, "htmlName"));
		className.setEscapeModelStrings(false);
		// Do not display root class name
		className.setVisible(classModel.getObject().getXid() != null);
		add(className);

		IModel<String> synonymsModel = new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				String retval = akpClassModel.getObject().getSynonyms();
				if (retval == null || retval.length() == 0)
					return "";
				return "(" + retval + ")";
			}
		};
		Label classSynonyms = new Label("classSynonyms", synonymsModel);
		classSynonyms.setEscapeModelStrings(false);
		// Do not display root class synonyms
		classSynonyms.setVisible(classModel.getObject().getXid() != null);
		add(classSynonyms);

		IModel<List<AkpClass>> subClassesModel = new LoadableDetachableModel<List<AkpClass>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpClass> load() {
				return akpClassModel.getObject().getChildren();
			}
		};
		ListView<AkpClass> subClassesListView = new ListView<AkpClass>(
				"subClasses", subClassesModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpClass> item) {
				AkpClassTree subClassTree = new AkpClassTree("subClassesTree",
						item.getModel());
				item.add(subClassTree);
				@SuppressWarnings("unchecked")
				ListView<AkpClass> parent = (ListView<AkpClass>) item
						.getParent();
				boolean last = (item
						.getIndex() == parent.getModelObject().size() - 1);
				if (last) {
					item.add(new AttributeAppender("class", "last"));
				}
			}
		};
		subClassesListView.setVisible(subClassesModel.getObject().size() > 0);
		add(subClassesListView);
	}
}
