package net.plantkelt.akp.webapp.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

public class InPlaceEditor extends Border {

	private static final long serialVersionUID = 1L;

	private WebMarkupContainer viewPanel;
	private Form<Void> editForm;
	private TextField<String> textField;
	private AjaxLink<Void> editLink;
	private EditorModel<String> editorModel;

	public InPlaceEditor(String id, EditorModel<String> model) {
		super(id);
		editorModel = model;
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
		IModel<String> stringModel = new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				return editorModel.getObject();
			}
		};

		editForm = new Form<Void>("editForm");
		addToBorder(editForm);
		editForm.setVisible(false);
		editForm.setOutputMarkupPlaceholderTag(true);
		editForm.setOutputMarkupId(true);
		textField = new TextField<String>("textInput", stringModel);
		editForm.add(textField);
		editForm.add(new AjaxSubmitLink("saveButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				String value = textField.getModelObject();
				editorModel.saveObject(target, value);
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

	public InPlaceEditor setReadOnly(boolean readonly) {
		editLink.setVisible(!readonly);
		return this;
	}
}
