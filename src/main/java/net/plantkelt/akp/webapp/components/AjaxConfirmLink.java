package net.plantkelt.akp.webapp.components;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;

public abstract class AjaxConfirmLink<T> extends AjaxLink<T> {

	private static final long serialVersionUID = 1L;

	public AjaxConfirmLink(String id) {
		super(id);
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);

		IAjaxCallListener listener = new AjaxCallListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public CharSequence getPrecondition(Component c) {
				return String.format("return confirm('%s');",
						getString("confirm.action.message"));
			}
		};

		attributes.getAjaxCallListeners().add(listener);
	}
}