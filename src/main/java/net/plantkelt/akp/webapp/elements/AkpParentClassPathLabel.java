package net.plantkelt.akp.webapp.elements;

import java.util.ArrayList;
import java.util.List;

import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.webapp.pages.AkpClassPage;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

public class AkpParentClassPathLabel extends Panel {

	private static final long serialVersionUID = 1L;

	public AkpParentClassPathLabel(String id,
			final IModel<AkpClass> akpClassModel) {
		super(id);

		IModel<List<AkpClass>> parentClassesModel = new LoadableDetachableModel<List<AkpClass>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpClass> load() {
				List<AkpClass> parentList = new ArrayList<AkpClass>();
				AkpClass akpClass = akpClassModel.getObject();
				while (akpClass != null) {
					parentList.add(0, akpClass);
					akpClass = akpClass.getParent();
				}
				AkpClass rootClass = new AkpClass();
				rootClass.setName("●");
				parentList.add(0, rootClass);
				return parentList;
			}
		};
		ListView<AkpClass> classPathListView = new ListView<AkpClass>(
				"classPathRepeat", parentClassesModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpClass> item) {
				AkpClass akpClass = item.getModelObject();
				Label classSeparatorLabel = new Label("separator",
						item.getIndex() == 0 ? "" : " → ");
				item.add(classSeparatorLabel);
				Link<AkpClassPage> link = AkpClassPage.link("classLink",
						akpClass.getXid());
				item.add(link);
				Label classNameLabel = new Label("className",
						akpClass.getHtmlName());
				classNameLabel.setEscapeModelStrings(false);
				link.add(classNameLabel);
			}
		};
		add(classPathListView);
	}
}
