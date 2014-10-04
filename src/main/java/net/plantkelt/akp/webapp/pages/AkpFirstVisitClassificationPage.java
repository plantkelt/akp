package net.plantkelt.akp.webapp.pages;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.elements.AkpClassTree;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

public class AkpFirstVisitClassificationPage extends AkpFirstVisitPage {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	public AkpFirstVisitClassificationPage() {

		IModel<AkpClass> rootClassModel = new LoadableDetachableModel<AkpClass>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected AkpClass load() {
				return akpTaxonService.getClass(null);
			}
		};
		AkpClassTree rootClassTree = new AkpClassTree("classTree",
				rootClassModel);
		add(rootClassTree);
	}
}
