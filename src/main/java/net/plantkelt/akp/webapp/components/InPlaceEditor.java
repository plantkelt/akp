package net.plantkelt.akp.webapp.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import net.plantkelt.akp.webapp.behaviors.InputMacros;

public class InPlaceEditor extends Border {

	private static final long serialVersionUID = 1L;

	private WebMarkupContainer viewPanel;
	private Form<Void> editForm;
	private TextField<String> textField;
	private TextArea<String> textArea;
	private AjaxLink<Void> editLink;
	private EditorModel<String> editorModel;

	public InPlaceEditor(String id, EditorModel<String> model, final int nRows,
			final int nCols) {
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
		textField.add(new InputMacros());
		textField.setVisible(nRows == 1);
		textField.add(
				new AttributeAppender("size", new Model<String>("" + nCols)));
		textArea = new TextArea<String>("textArea", stringModel);
		textArea.setVisible(nRows > 1);
		textArea.add(
				new AttributeAppender("rows", new Model<String>("" + nRows)));
		textArea.add(
				new AttributeAppender("cols", new Model<String>("" + nCols)));
		editForm.add(textField);
		editForm.add(textArea);
		AjaxSubmitLink submitBtn = new AjaxSubmitLink("saveButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				String value = nRows > 1 ? textArea.getModelObject()
						: textField.getModelObject();
				editorModel.saveObject(target, value);
				viewPanel.setVisible(true);
				editForm.setVisible(false);
				target.add(editForm, viewPanel);
			}
		};
		submitBtn.setOutputMarkupId(true);
		textArea.add(new InputMacros(submitBtn.getMarkupId()));
		editForm.add(submitBtn);
		editForm.add(new AjaxSubmitLink("cancelButton") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				editorModel.cancelObject(target);
				viewPanel.setVisible(true);
				editForm.setVisible(false);
				target.add(editForm, viewPanel);
			}
		});
	}

	public void open() {
		viewPanel.setVisible(false);
		editForm.setVisible(true);
	}

	public InPlaceEditor setReadOnly(boolean readonly) {
		editLink.setVisible(!readonly);
		return this;
	}
}
