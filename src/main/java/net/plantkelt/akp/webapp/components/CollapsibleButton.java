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
	private boolean opened = false;

	public CollapsibleButton(String id, Component showHide,
			boolean defaultOpen) {
		super(id);
		this.opened = defaultOpen;
		this.showHideComponent = showHide;

		showHideComponent.setOutputMarkupId(true);
		showHideComponent.setOutputMarkupPlaceholderTag(true);

		AjaxLink<Void> showHideLink = new AjaxLink<Void>("showHideLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				opened = !opened;
				showHideComponent.setVisible(opened);
				target.add(showHideComponent);
				target.add(this);
				onOpenClose(opened);
			}
		};
		AttributeModifier openClosedAttributeModifier = new AttributeModifier(
				"class", new AbstractReadOnlyModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject() {
						return opened ? "collapsible-opened"
								: "collapsible-closed";
					}
				});
		showHideLink.add(openClosedAttributeModifier);
		showHideLink.setOutputMarkupId(true);
		showHideComponent.setVisible(defaultOpen);
		add(showHideLink);
	}

	public void hide() {
		opened = false;
	}

	public void show() {
		opened = true;
	}

	protected void onOpenClose(boolean opened) {
	}
}