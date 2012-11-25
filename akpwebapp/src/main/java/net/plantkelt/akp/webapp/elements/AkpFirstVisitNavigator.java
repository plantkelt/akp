package net.plantkelt.akp.webapp.elements;

import net.plantkelt.akp.webapp.pages.AkpFirstVisitClassificationPage;
import net.plantkelt.akp.webapp.pages.AkpFirstVisitCorpusPage;
import net.plantkelt.akp.webapp.pages.AkpFirstVisitMainPage;
import net.plantkelt.akp.webapp.pages.AkpFirstVisitPage;
import net.plantkelt.akp.webapp.pages.AkpFirstVisitSourcesPage;
import net.plantkelt.akp.webapp.pages.AkpFirstVisitToolsPage;
import net.plantkelt.akp.webapp.pages.AkpFirstVisitWhoPage;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.StringResourceModel;

public class AkpFirstVisitNavigator extends Panel {

	private static final long serialVersionUID = 1L;

	private static final Class<?>[] PAGES = { AkpFirstVisitMainPage.class,
			AkpFirstVisitCorpusPage.class,
			AkpFirstVisitClassificationPage.class,
			AkpFirstVisitSourcesPage.class, AkpFirstVisitWhoPage.class,
			AkpFirstVisitToolsPage.class };

	public AkpFirstVisitNavigator(String id, AkpFirstVisitPage parent) {
		super(id);

		RepeatingView repeat = new RepeatingView("repeat");
		for (int i = 0; i < PAGES.length; i++) {
			@SuppressWarnings("unchecked")
			Class<? extends AkpFirstVisitPage> pageClass = (Class<? extends AkpFirstVisitPage>) PAGES[i];
			WebMarkupContainer item = new WebMarkupContainer(
					repeat.newChildId());
			repeat.add(item);
			BookmarkablePageLink<AkpFirstVisitPage> link = new BookmarkablePageLink<AkpFirstVisitPage>(
					"link", pageClass);
			item.add(link);
			Label pageLabel = new Label("label", new StringResourceModel(
					"first.visit." + i, null));
			link.add(pageLabel);
			if (parent.getClass().equals(pageClass)) {
				item.add(new AttributeModifier("class", "selected"));
			}
		}
		add(repeat);
	}
}
