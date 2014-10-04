package net.plantkelt.akp.webapp.elements;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.AjaxConfirmLink;
import net.plantkelt.akp.webapp.pages.AkpBibPage;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
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

		// Link
		Link<AkpBibPage> bibLink = AkpBibPage.link("bibLink", bibModel
				.getObject().getXid());
		add(bibLink);

		// Label: displayed
		Label bibLabel = new Label("bibId", new PropertyModel<String>(bibModel,
				"xid"));
		bibLink.add(bibLabel);

		// Popup
		WebMarkupContainer bibPopup = new WebMarkupContainer("bibPopup");
		// Do not display popup for administrators
		bibPopup.setVisible(!isAdmin);
		bibLink.add(bibPopup);
		Label bibTitleLabel = new Label("bibTitle", new PropertyModel<String>(
				bibModel, "title"));
		bibPopup.add(bibTitleLabel);
		Label bibAuthorLabel = new Label("bibAuthor",
				new PropertyModel<String>(bibModel, "author"));
		bibPopup.add(bibAuthorLabel);
		Label bibISBNLabel = new Label("bibISBN", new PropertyModel<String>(
				bibModel, "isbn"));
		bibPopup.add(bibISBNLabel);

		// Delete link (admin section)
		AjaxConfirmLink<Void> deleteLink = new AjaxConfirmLink<Void>(
				"deleteLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				AkpVernacularName vernaName = vernaNameModel.getObject();
				akpTaxonService.removeBibFromVernacularName(
						bibModel.getObject(), vernaName);
				target.add(refreshComponent);
			}
		};
		deleteLink.setVisible(isAdmin);
		add(deleteLink);
	}
}
