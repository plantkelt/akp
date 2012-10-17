package net.plantkelt.akp.webapp.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

public class InPlaceSelector<T> extends Border {

	private static final long serialVersionUID = 1L;

	private WebMarkupContainer viewPanel;
	private Form<Void> editForm;
	private DropDownChoice<T> valueSelect;
	private AjaxLink<Void> editLink;
	private SelectorModel<T> selectorModel;

	public InPlaceSelector(String id, SelectorModel<T> model) {
		super(id);
		selectorModel = model;
		viewPanel = new WebMarkupContainer("viewPanel");
		addToBorder(viewPanel);
		viewPanel.setOutputMarkupPlaceholderTag(true);
		viewPanel.setOutputMarkupId(true);
		editLink = new AjaxLink<Void>("editLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				viewPanel.setVisible(false);
				editForm.setVisible(true);
				target.add(editForm, viewPanel);
			}
		};
		viewPanel.add(editLink);
		viewPanel.add(getBodyContainer());
		IModel<T> valueModel = new LoadableDetachableModel<T>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected T load() {
				return selectorModel.getObject();
			}
		};

		editForm = new Form<Void>("editForm");
		addToBorder(editForm);
		editForm.setVisible(false);
		editForm.setOutputMarkupPlaceholderTag(true);
		editForm.setOutputMarkupId(true);
		valueSelect = new DropDownChoice<T>("valueSelect", valueModel,
				selectorModel.getValues(), new IChoiceRenderer<T>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(T object) {
						return selectorModel.getDisplayValue(object);
					}

					@Override
					public String getIdValue(T object, int index) {
						return "" + index;
					}
				});
		editForm.add(valueSelect);
		editForm.add(new AjaxSubmitLink("saveButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				T value = valueSelect.getModelObject();
				selectorModel.saveObject(target, value);
				viewPanel.setVisible(true);
				editForm.setVisible(false);
				target.add(editForm, viewPanel);
			}
		});
		editForm.add(new AjaxSubmitLink("cancelButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				viewPanel.setVisible(true);
				editForm.setVisible(false);
				target.add(editForm, viewPanel);
			}
		});
	}

	public InPlaceSelector<T> setReadOnly(boolean readonly) {
		editLink.setVisible(!readonly);
		return this;
	}
}
