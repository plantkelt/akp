package net.plantkelt.akp.webapp.pages;

import java.util.Map;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.service.AkpTaxonService;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;

public class AkpFirstVisitStatisticsPage extends AkpFirstVisitPage {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	public AkpFirstVisitStatisticsPage() {
		// Object count
		Map<String, Long> countPerType = akpTaxonService.getObjectCount();
		RepeatingView countRepeat = new RepeatingView("countRepeat");
		add(countRepeat);
		boolean even = false;
		for (Map.Entry<String, Long> kv : countPerType.entrySet()) {
			WebMarkupContainer item = new WebMarkupContainer(
					countRepeat.newChildId());
			countRepeat.add(item);
			item.add(new Label("what", getString("class." + kv.getKey())));
			item.add(new Label("count", "" + kv.getValue()));
			if (even)
				item.add(new AttributeAppender("class", "even"));
			even = !even;
		}
		// Lang count
		Map<AkpLang, Long> countPerLang = akpTaxonService
				.getVernacularNameCountPerLanguage();
		RepeatingView langRepeat = new RepeatingView("langRepeat");
		add(langRepeat);
		even = false;
		for (Map.Entry<AkpLang, Long> kv : countPerLang.entrySet()) {
			WebMarkupContainer item = new WebMarkupContainer(
					langRepeat.newChildId());
			langRepeat.add(item);
			item.add(new Label("lang", kv.getKey().getName()));
			item.add(new Label("count", "" + kv.getValue()));
			if (even)
				item.add(new AttributeAppender("class", "even"));
			even = !even;
		}
	}
}
