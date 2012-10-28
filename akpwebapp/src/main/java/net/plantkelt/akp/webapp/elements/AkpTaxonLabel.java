package net.plantkelt.akp.webapp.elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.plantkelt.akp.domain.AkpAuthor;
import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.pages.AkpAuthorPage;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.google.inject.Inject;

public class AkpTaxonLabel extends Panel {

	private static final long serialVersionUID = 1L;

	private static class NameElement {
		public String taxonElement;
		public String authorElement;
		public AkpAuthor author;
	}

	@Inject
	private AkpTaxonService akpTaxonService;

	public AkpTaxonLabel(String id, final IModel<AkpTaxon> taxonModel,
			final IModel<Map<String, AkpAuthor>> authorsModel) {
		super(id);

		IModel<List<NameElement>> elemListModel = new LoadableDetachableModel<List<NameElement>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<NameElement> load() {
				String[] elems = taxonModel.getObject().getNameElements();
				Set<String> authorIds = new HashSet<String>(
						elems.length / 2 + 1);
				for (int i = 1; i < elems.length; i += 2) {
					authorIds.add(elems[i]);
				}
				Map<String, AkpAuthor> authors = authorsModel != null ? authorsModel
						.getObject() : akpTaxonService.getAuthors(authorIds);
				List<NameElement> retval = new ArrayList<NameElement>(
						elems.length / 2 + 1);
				for (int i = 0; i < elems.length; i += 2) {
					NameElement nameElement = new NameElement();
					nameElement.taxonElement = elems[i];
					String authorXid = i + 1 < elems.length ? elems[i + 1]
							: null;
					nameElement.authorElement = authorXid;
					if (authorXid != null)
						nameElement.author = authors
								.get(nameElement.authorElement);
					retval.add(nameElement);
				}
				return retval;
			}
		};
		ListView<NameElement> elemList = new ListView<AkpTaxonLabel.NameElement>(
				"elemList", elemListModel) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<NameElement> item) {
				NameElement nameElement = item.getModelObject();
				Label elemTaxon = new Label("elemTaxon",
						nameElement.taxonElement);
				elemTaxon.setEscapeModelStrings(false);
				item.add(elemTaxon);
				WebMarkupContainer elemAuthorLink = nameElement.author == null ? new WebMarkupContainer(
						"elemAuthorLink") : AkpAuthorPage.link(
						"elemAuthorLink", nameElement.author.getXid());
				item.add(elemAuthorLink);
				Label elemAuthorId = new Label("elemAuthorId",
						nameElement.authorElement == null ? ""
								: nameElement.authorElement);
				elemAuthorId.setVisible(nameElement.authorElement != null);
				if (nameElement.author == null)
					elemAuthorId.add(new AttributeAppender("class",
							new Model<String>("wrong-author"), " "));
				elemAuthorLink.add(elemAuthorId);
				Label authorId = new Label("authorId",
						nameElement.author == null ? nameElement.authorElement
								: nameElement.author.getXid());
				elemAuthorLink.add(authorId);
				Label authorName = new Label("authorName",
						nameElement.author == null ? "?"
								: nameElement.author.getName());
				authorName.setEscapeModelStrings(false);
				elemAuthorLink.add(authorName);
				Label authorDates = new Label("authorDates",
						nameElement.author == null ? "?"
								: nameElement.author.getDates());
				elemAuthorLink.add(authorDates);
			}
		};
		add(elemList);
	}
}
