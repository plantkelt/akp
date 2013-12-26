package net.plantkelt.akp.webapp.elements;

import java.util.Collections;
import java.util.List;

import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.service.AkpTaxonService;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.inject.Inject;

public abstract class AkpClassSelectPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private String searchText;

	public AkpClassSelectPanel(String id) {
		super(id);

		Form<AkpClassSelectPanel> form = new Form<AkpClassSelectPanel>("form",
				new CompoundPropertyModel<AkpClassSelectPanel>(this)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
			}
		};
		add(form);
		form.add(new TextField<String>("searchText"));

		// Result model
		IModel<List<AkpClass>> resultModel = new LoadableDetachableModel<List<AkpClass>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<AkpClass> load() {
				if (searchText == null || searchText.length() < 3) {
					return Collections.emptyList();
				}
				return akpTaxonService.searchClass(searchText);
			}
		};

		// Result list
		ListView<AkpClass> resultList = new ListView<AkpClass>("resultList",
				resultModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AkpClass> item) {
				AkpClass clazz = item.getModelObject();
				Link<AkpClass> link = new Link<AkpClass>("link",
						item.getModel()) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						onClassSelected(getModelObject());
					}
				};
				item.add(link);
				Label classNameLabel = new Label("className",
						clazz.getHtmlName());
				classNameLabel.setEscapeModelStrings(false);
				link.add(classNameLabel);
			}
		};
		add(resultList);
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	protected abstract void onClassSelected(AkpClass clazz);

}
