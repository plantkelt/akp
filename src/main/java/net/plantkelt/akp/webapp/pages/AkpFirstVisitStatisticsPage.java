package net.plantkelt.akp.webapp.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpLang;
import net.plantkelt.akp.service.AkpLogService;
import net.plantkelt.akp.service.AkpTaxonService;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.resource.ByteArrayResource;

public class AkpFirstVisitStatisticsPage extends AkpFirstVisitPage {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;
	@Inject
	private AkpLogService akpLogService;

	public AkpFirstVisitStatisticsPage() {
		// Activity graph
		add(new Image("activityPerWeek", new ByteArrayResource("image/png") {
			private static final long serialVersionUID = 1L;

			@Override
			protected byte[] getData(final Attributes attributes) {
				return akpLogService.getActivityGraph(600, 400);
			}

		}));

		// Object count
		List<Map.Entry<String, Long>> countPerTypeList = new ArrayList<Map.Entry<String, Long>>(
				akpTaxonService.getObjectCount().entrySet());
		Collections.sort(countPerTypeList,
				new Comparator<Map.Entry<String, Long>>() {
					@Override
					public int compare(Entry<String, Long> kv1,
							Entry<String, Long> kv2) {
						return kv2.getValue().compareTo(kv1.getValue());
					}
				});
		RepeatingView countRepeat = new RepeatingView("countRepeat");
		add(countRepeat);
		boolean even = false;
		for (Map.Entry<String, Long> kv : countPerTypeList) {
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
		List<Map.Entry<AkpLang, Long>> countPerLang = new ArrayList<Map.Entry<AkpLang, Long>>(
				akpTaxonService.getVernacularNameCountPerLanguage().entrySet());
		Collections.sort(countPerLang,
				new Comparator<Map.Entry<AkpLang, Long>>() {
					@Override
					public int compare(Entry<AkpLang, Long> kv1,
							Entry<AkpLang, Long> kv2) {
						return kv2.getValue().compareTo(kv1.getValue());
					}
				});
		RepeatingView langRepeat = new RepeatingView("langRepeat");
		add(langRepeat);
		// TODO Sort by count
		even = false;
		for (Map.Entry<AkpLang, Long> kv : countPerLang) {
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
