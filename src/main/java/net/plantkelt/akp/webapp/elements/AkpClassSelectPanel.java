package net.plantkelt.akp.webapp.elements;

import java.util.Collections;
import java.util.List;

import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.behaviors.JavascriptConfirmationModifier;

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
import org.apache.wicket.model.StringResourceModel;

import com.google.inject.Inject;

public abstract class AkpClassSelectPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private boolean singleResultAutoselect = true;
	private boolean confirmClick = false;
	private String searchText;
	private IModel<List<AkpClass>> resultModel;

	public AkpClassSelectPanel(String id) {
		super(id);

		Form<AkpClassSelectPanel> form = new Form<AkpClassSelectPanel>("form",
				new CompoundPropertyModel<AkpClassSelectPanel>(this)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (singleResultAutoselect) {
					List<AkpClass> results = resultModel.getObject();
					if (results.size() == 1) {
						// 1 Result only: auto-select the only result
						onClassSelected(results.get(0));
					}
				}
			}
		};
		add(form);
		form.add(new TextField<String>("searchText"));

		// Result model
		resultModel = new LoadableDetachableModel<List<AkpClass>>() {
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
				if (confirmClick) {
					link.add(new JavascriptConfirmationModifier("onClick",
							new StringResourceModel("confirm.action.message",
									AkpClassSelectPanel.this, null)));
				}
			}
		};
		add(resultList);
	}

	public boolean hasResult() {
		return !resultModel.getObject().isEmpty();
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public void setSingleResultAutoselect(boolean singleResultAutoselect) {
		this.singleResultAutoselect = singleResultAutoselect;
	}

	public void setConfirmClick(boolean confirmClick) {
		this.confirmClick = confirmClick;
		if (confirmClick)
			singleResultAutoselect = false;
	}
	
	protected abstract void onClassSelected(AkpClass clazz);

}
