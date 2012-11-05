package net.plantkelt.akp.webapp.components;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface SelectorModel<T> extends Serializable {

	public abstract T getObject();

	public abstract void saveObject(AjaxRequestTarget target, T t);

	public abstract String getDisplayValue(T t);

	public abstract String getIdValue(T t);
	
	public abstract List<T> getValues();
	
}
