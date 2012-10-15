package net.plantkelt.akp.webapp.components;

import org.apache.wicket.ajax.AjaxRequestTarget;

public abstract class EditorModel<T> {

	public abstract T getObject();

	public abstract void saveObject(AjaxRequestTarget target, T t);

	public void deleteObject(AjaxRequestTarget target) {
	}
}
