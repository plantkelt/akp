package net.plantkelt.akp.webapp.components;

import java.util.List;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.domain.AkpUser;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.google.inject.Inject;

public class AkpLexicalGroupPanel extends Panel {

	@Inject
	private AkpTaxonService akpTaxonService;

	private static final long serialVersionUID = 1L;

	public AkpLexicalGroupPanel(String id, final IModel<AkpLexicalGroup> lexicalGroupModel) {
		super(id);

		AkpUser user = AkpWicketSession.get().getAkpUser();
		final boolean isAdmin = user != null && user.isAdmin();

		AkpLexicalGroup lexicalGroup = lexicalGroupModel.getObject();
		
		String xxx = String.format("%d - %s - %s",
				lexicalGroup.getCorrect(), lexicalGroup.getLang().getXid(),
				lexicalGroup.getLang().getLangGroup().getName());
		Label label = new Label("lexicalGroupValue", xxx);
		add(label);
		// Names
		RepeatingView vernaNamesRepeat = new RepeatingView("vernaNames");
		add(vernaNamesRepeat);
		List<AkpVernacularName> vernaNames = lexicalGroup
				.getVernacularNames();
		for (AkpVernacularName vernaName : vernaNames) {
			WebMarkupContainer item2 = new WebMarkupContainer(
					vernaNamesRepeat.newChildId());
			vernaNamesRepeat.add(item2);
			StringBuffer bibsb = new StringBuffer();
			for (AkpBib bib : vernaName.getBibs()) {
				bibsb.append("[").append(bib.getXid()).append("]");
			}
			String xxx2 = String.format("%d -> %d : %s (%s) -> %s",
					vernaName.getXid(), vernaName.getParentId(),
					vernaName.getName(), vernaName.getComment(),
					bibsb.toString());
			Label label2 = new Label("vernaNameValue", xxx2);
			item2.add(label2);
		}
		
		setOutputMarkupId(true);
	}
}
