package net.plantkelt.akp.webapp.pages;

import java.util.List;

import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.AkpParentClassPathLabel;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

@AuthorizeInstantiation("ADMIN")
public class AkpPlantPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	public AkpPlantPage(PageParameters parameters) {
		Integer plantId = parameters.get("xid").toOptionalInteger();
		AkpPlant plant = akpTaxonService.getPlant(plantId);
		// Parent classes
		AkpParentClassPathLabel parentPathLabel = new AkpParentClassPathLabel(
				"parentPath", plant.getAkpClass());
		add(parentPathLabel);
		// Plant main name
		Label classNameLabel = new Label("plantName", plant.getMainName()
				.getHtmlName());
		classNameLabel.setEscapeModelStrings(false);
		add(classNameLabel);
		// Synonyms
		RepeatingView synonymsRepeat = new RepeatingView("synonyms");
		add(synonymsRepeat);
		List<AkpTaxon> taxons = plant.getSynonyms();
		for (AkpTaxon taxon : taxons) {
			WebMarkupContainer item = new WebMarkupContainer(
					synonymsRepeat.newChildId());
			synonymsRepeat.add(item);
			Label synonymNameLabel = new Label("synonymName",
					taxon.getHtmlName());
			synonymNameLabel.setEscapeModelStrings(false);
			item.add(synonymNameLabel);
		}
	}

	public static Link<AkpPlantPage> link(String id, Integer xid) {
		return new BookmarkablePageLink<AkpPlantPage>(id, AkpPlantPage.class,
				new PageParameters().add("xid", xid));
	}
}
