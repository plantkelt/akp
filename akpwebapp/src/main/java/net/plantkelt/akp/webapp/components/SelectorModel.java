package net.plantkelt.akp.webapp.components;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;

public abstract class SelectorModel<T> {

	public abstract T getObject();

	public abstract void saveObject(AjaxRequestTarget target, T t);

	public abstract String getDisplayValue(T t);

	public abstract List<T> getValues();
}
