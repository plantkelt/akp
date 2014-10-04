package net.plantkelt.akp.webapp.behaviors;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Add macros to text input.
 * 
 * Usage example:
 * 
 * TextField input = new TextField("input"); input.add(new InputMacros());
 */
public class InputMacros extends AttributeModifier {
	private static final long serialVersionUID = 1L;

	private ResourceReference JS_MACRO = new JavaScriptResourceReference(
			InputMacros.class, "InputMacros.js");

	public InputMacros() {
		super("onkeyup", new Model<String>("akp_handleMacro(this, event, null)"));
	}

	public InputMacros(String submitButtonId) {
		super("onkeyup", new Model<String>(String.format(
				"akp_handleMacro(this, event, '%s')", submitButtonId)));
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(JS_MACRO));
	}

}
