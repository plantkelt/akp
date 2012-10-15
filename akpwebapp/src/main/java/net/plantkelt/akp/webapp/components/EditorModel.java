package net.plantkelt.akp.webapp.components;

public interface EditorModel<T> {

	public T getObject();

	public void saveObject(T t);
}
