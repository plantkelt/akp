package net.plantkelt.akp.webapp.behaviors;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Add a confirmation dialog on any method of some component. Used mainly for
 * submit buttons such as delete buttons.
 * 
 * Usage example:
 * 
 * Button deleteButton = new Button("delete"); deleteButton.add(new
 * JavascriptConfirmationModifier("onClick", "Are you sure?"));
 * 
 */
public class JavascriptConfirmationModifier extends AttributeModifier {
	private static final long serialVersionUID = 1L;

	private boolean disappear;

	public JavascriptConfirmationModifier(String event, String msg) {
		this(event, new Model<String>(msg), false);
	}

	public JavascriptConfirmationModifier(String event, IModel<String> model) {
		this(event, model, false);
	}

	public JavascriptConfirmationModifier(String event, IModel<String> model,
			boolean disappear) {
		super(event, model);
		this.disappear = disappear;
	}

	@Override
	protected String newValue(final String currentValue,
			final String replacementValue) {
		String prefix = String
				.format("var conf = confirm('%s'); if (!conf) { return false; } else { %s return true; }",
						replacementValue,
						disappear ? "this.style.display='none';" : "");
		String result = prefix;
		if (currentValue != null) {
			result = prefix + currentValue;
		}
		return result;
	}
}
