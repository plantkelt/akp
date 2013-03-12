package net.plantkelt.akp.webapp.components;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public abstract class EditorModel<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	public abstract T getObject();

	public abstract void saveObject(AjaxRequestTarget target, T t);

	public void cancelObject(AjaxRequestTarget target) {
	}

}
