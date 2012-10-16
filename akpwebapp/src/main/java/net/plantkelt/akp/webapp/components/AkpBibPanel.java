package net.plantkelt.akp.webapp.components;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.google.inject.Inject;

public class AkpBibPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	public AkpBibPanel(String id, final IModel<AkpBib> bibModel,
			final IModel<AkpVernacularName> vernaNameModel,
			final Component refreshComponent) {
		super(id);

		boolean isAdmin = AkpWicketSession.get().isAdmin();

		Label bibLabel = new Label("bibId", new PropertyModel<String>(bibModel,
				"xid"));
		add(bibLabel);

		AjaxConfirmLink<Void> deleteLink = new AjaxConfirmLink<Void>(
				"deleteLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				AkpVernacularName vernaName = vernaNameModel.getObject();
				vernaName.getBibs().remove(bibModel.getObject());
				akpTaxonService.updateVernacularName(vernaName);
				target.add(refreshComponent);
			}
		};
		deleteLink.setVisible(isAdmin);
		add(deleteLink);
	}
}
