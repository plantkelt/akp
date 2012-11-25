package net.plantkelt.akp.webapp.pages;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpBib;
import net.plantkelt.akp.service.AkpTaxonService;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.RepeatingView;

public class AkpFirstVisitSourcesPage extends AkpFirstVisitPage {

	private static final long serialVersionUID = 1L;

	/**
	 * 0. Website name; 1. List of usages: NA=Author's names, NL=Linnean's
	 * names...; 2. URL
	 */
	private static final String[][] WEBSITES = {
			{ "IPNI - International Plant Names Index", "NA,NL",
					"http://www.ipni.org/index.html" },
			{ "HUH - Harvard University Herbaria", "NA,NC",
					"http://asaweb.huh.harvard.edu:8080/databases/botanist_index.html" },
			{ "VASCULAR PLANT FAMILIES & GENERA", "NL,SYN",
					"http://www.kew.org/data/genlist.html" },
			{ "ARBRES", "NL,SYN,RM", "http://jeanlouis.helardot.free.fr/" },
			{ "INDEX SYNONYMIQUE DE LA FLORE FRANÇAISE", "NL,SYN",
					"http://www.inra.fr/flore-france/index.htm" },
			{ "FLORA ITALIANA", "NL,SYN",
					"http://www.homolaicus.com/scienza/erbario/utility/floraitalica/flora/" },
			{ "FLORA IBERICA", "NL,SYN,RL",
					"http://www.rjb.csic.es/floraiberica/" },
			{ "FLORA OF CHINA", "NL,SYN,RM", "http://www.fna.org/china/" },
			{ "FLORA OF PAKISTAN", "NL,SYN,RM",
					"http://www.efloras.org/flora_page.aspx?flora_id=5" },
			{ "FLORA OF NEPAL", "NL,SYN,RM",
					"http://ti.um.u-tokyo.ac.jp/default.htm" },
			{ "FLORA OF NORTH AMERICA", "NL,SYN,RL",
					"http://hua.huh.harvard.edu/FNA/" },
			{ "FLORA OF CHILE", "NL,SYN,RL",
					"http://www.efloras.org/flora_page.aspx?flora_id=60" },
			{ "FLORA OF SOUTH AUSTRALIA", "NL,SYN,RL",
					"http://www.flora.sa.gov.au/" },
			{ "FLORA OF TAIWAN", "NL,SYN,RM",
					"http://tai2.ntu.edu.tw/fotdv/fotmain.htm" },
			{ "FLOREALPES", "NL,SYN,RM", "http://www.florealpes.com/" },
			{ "GRASS BASE", "NL,SYN", "http://www.kew.org/data/grasses-db.html" },
			{ "RESSOURCES NATURELLES CANADA,Service Canadien des Forêts",
					"NL,SYN", "http://www.nrcan-rncan.gc.ca/" },
			{ "GRIN Taxonomy. Germplasm Resources Information Network",
					"NL,SYN", "http://www.ars-grin.gov/" },
			{ "USDA - United States Department of Agriculture", "NL,SYN,RL",
					"http://www.plants.usda.gov/" },
			{ "PLANTS FOR A FUTURE", "NL,SYN,RM", "http://www.pfaf.org/" },
			{ "ALUKA", "NL,SYN,RL", "http://aluka.org/" },
			{ "TROPICOS", "NL,", "http://www.tropicos.org/" },
			{ "ENCYCLOPEDIA OF LIFE", "NL,SYN", "http://www.eol.org/index" }, };

	private static final String[] BOOKS = { "2000paer", "1990gfap", "1992ecbb",
			"1857dgdk", "1984mgbl", "2001afav", "1990apfd", "1911gfcb",
			"2001gflb", "2001gptb", "2005arph", "1995pnph", "1971fvma" };

	@Inject
	private AkpTaxonService akpTaxonService;

	public AkpFirstVisitSourcesPage() {

		// Websites
		RepeatingView websiteList = new RepeatingView("websiteList");
		add(websiteList);
		for (String[] website : WEBSITES) {
			WebMarkupContainer item = new WebMarkupContainer(
					websiteList.newChildId());
			websiteList.add(item);
			String[] whatList = website[1].split(",");
			item.add(new Label("name", website[0]));
			ExternalLink url = new ExternalLink("url", website[2]);
			item.add(url);
			url.add(new Label("urlName", website[2]));
			StringBuffer whatStr = new StringBuffer();
			for (String what : whatList) {
				whatStr.append(getString("what." + what)).append(", ");
			}
			if (whatStr.length() > 2)
				whatStr.setLength(whatStr.length() - 2);
			item.add(new Label("what", whatStr.toString()));
		}

		// Books
		RepeatingView bookList = new RepeatingView("bookList");
		add(bookList);
		for (String bookRef : BOOKS) {
			AkpBib book = akpTaxonService.getBib(bookRef);
			if (book == null)
				continue; // We never know...
			WebMarkupContainer item = new WebMarkupContainer(
					bookList.newChildId());
			bookList.add(item);
			item.add(new Label("title", book.getTitle()));
			item.add(new Label("author", book.getAuthor()));
			item.add(new Label("publisher", book.getEditor()));
			item.add(new Label("comments", book.getComments()));
			item.add(new Label("isbn", book.getIsbn()));
		}

	}
}
