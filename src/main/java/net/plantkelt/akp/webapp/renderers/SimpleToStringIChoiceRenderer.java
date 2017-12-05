package net.plantkelt.akp.webapp.renderers;

import org.apache.wicket.markup.html.form.ChoiceRenderer;

public class SimpleToStringIChoiceRenderer<T> extends ChoiceRenderer<T> {
	private static final long serialVersionUID = 1L;

	public SimpleToStringIChoiceRenderer() {
	}

	@Override
	public Object getDisplayValue(T object) {
		return object.toString();
	}

	@Override
	public String getIdValue(T object, int index) {
		return object.toString();
	}

}
