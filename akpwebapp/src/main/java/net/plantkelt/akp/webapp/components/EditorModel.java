package net.plantkelt.akp.webapp.components;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface EditorModel<T> extends Serializable {

	public abstract T getObject();

	public abstract void saveObject(AjaxRequestTarget target, T t);

}
