package net.plantkelt.akp.webapp.pages;

import java.util.List;
import java.util.SortedSet;

import net.plantkelt.akp.domain.AkpLexicalGroup;
import net.plantkelt.akp.domain.AkpPlant;
import net.plantkelt.akp.domain.AkpPlantTag;
import net.plantkelt.akp.domain.AkpVernacularName;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.components.AkpParentClassPathLabel;
import net.plantkelt.akp.webapp.components.AkpPlantSynonymsPanel;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

public class AkpPlantPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private Integer plantId;
	private IModel<AkpPlant> akpPlantModel;

	public AkpPlantPage(PageParameters parameters) {

		// Load data
		plantId = parameters.get("xid").toOptionalInteger();
		akpPlantModel = new LoadableDetachableModel<AkpPlant>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpPlant load() {
				return akpTaxonService.getPlant(plantId);
			}
		};
		AkpPlant plant = akpPlantModel.getObject();

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
		AkpPlantSynonymsPanel synonymsPanel = new AkpPlantSynonymsPanel(
				"synonymsPanel", akpPlantModel);
		add(synonymsPanel);

		// Tags
		RepeatingView tagsRepeat = new RepeatingView("tags");
		add(tagsRepeat);
		SortedSet<AkpPlantTag> tags = plant.getTags();
		for (AkpPlantTag tag : tags) {
			WebMarkupContainer item = new WebMarkupContainer(
					tagsRepeat.newChildId());
			tagsRepeat.add(item);
			Label label = new Label("tagValue", tag.getValue());
			label.add(new AttributeAppender("class", new Model<String>("tag_"
					+ tag.getType()), " "));
			item.add(label);
		}

		// Lexical groups
		RepeatingView lexicalGroupsRepeat = new RepeatingView("lexicalGroups");
		add(lexicalGroupsRepeat);
		List<AkpLexicalGroup> lexicalGroups = plant.getLexicalGroups();
		for (AkpLexicalGroup lexicalGroup : lexicalGroups) {
			WebMarkupContainer item = new WebMarkupContainer(
					lexicalGroupsRepeat.newChildId());
			lexicalGroupsRepeat.add(item);
			String xxx = String.format("%d - %s - %s",
					lexicalGroup.getCorrect(), lexicalGroup.getLang().getXid(),
					lexicalGroup.getLang().getLangGroup().getName());
			Label label = new Label("lexicalGroupValue", xxx);
			item.add(label);
			// Names
			RepeatingView vernaNamesRepeat = new RepeatingView("vernaNames");
			item.add(vernaNamesRepeat);
			List<AkpVernacularName> vernaNames = lexicalGroup
					.getVernacularNames();
			for (AkpVernacularName vernaName : vernaNames) {
				WebMarkupContainer item2 = new WebMarkupContainer(
						vernaNamesRepeat.newChildId());
				vernaNamesRepeat.add(item2);
				String xxx2 = String.format("%d -> %d : %s (%s)",
						vernaName.getXid(), vernaName.getParentId(),
						vernaName.getName(), vernaName.getComment());
				Label label2 = new Label("vernaNameValue", xxx2);
				item2.add(label2);
			}
		}
	}

	public static Link<AkpPlantPage> link(String id, Integer xid) {
		return new BookmarkablePageLink<AkpPlantPage>(id, AkpPlantPage.class,
				new PageParameters().add("xid", xid));
	}
}
