package net.plantkelt.akp.webapp.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

public class CollapsibleButton extends Panel {

	private static final long serialVersionUID = 1L;

	private Component showHideComponent;
	private boolean visible = false;

	public CollapsibleButton(String id, Component showHide, boolean defaultOpen) {
		super(id);
		this.visible = defaultOpen;
		this.showHideComponent = showHide;

		showHideComponent.setOutputMarkupId(true);
		showHideComponent.setOutputMarkupPlaceholderTag(true);

		AjaxLink<Void> showHideLink = new AjaxLink<Void>("showHideLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				visible = !visible;
				showHideComponent.setVisible(visible);
				target.add(showHideComponent);
				target.add(this);
			}
		};
		AttributeModifier openClosedAttributeModifier = new AttributeModifier(
				"class", new AbstractReadOnlyModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return visible ? "collapsible-opened" : "collapsible-closed";
					}
				});
		showHideLink.add(openClosedAttributeModifier);
		showHideLink.setOutputMarkupId(true);
		showHideComponent.setVisible(defaultOpen);
		add(showHideLink);
	}

	public void hide() {
		visible = false;
	}

	public void show() {
		visible = true;
	}
}