package net.plantkelt.app;

import javax.inject.Inject;

import net.plantkelt.akp.domain.AkpTaxon;
import net.plantkelt.akp.service.AkpTaxonService;

import org.hibernate.LockMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.google.inject.Provider;
import com.google.inject.persist.UnitOfWork;

public class AkpApplicationService {

	@Inject
	AkpTaxonService akpTaxonService;
	@Inject
	private Provider<Session> sessionProvider;
	@Inject
	private UnitOfWork unitOfWork;

	public void taxonRegexp(String search, String replace, boolean commit) {
		unitOfWork.begin();

		System.out.println(String.format("Taxon regexp '%s' -> '%s'.", search,
				replace));
		if (commit)
			System.out.println("Commit mode.");
		ScrollableResults taxons = sessionProvider.get()
				.createCriteria(AkpTaxon.class).setFetchSize(1000)
				.setReadOnly(true).setLockMode(LockMode.NONE).scroll();
		int n = 0;
		while (taxons.next()) {
			AkpTaxon taxon = (AkpTaxon) taxons.get(0);
			String name2 = taxon.getName().replaceAll(search, replace);
			if (!taxon.getName().equals(name2)) {
				System.out.println("- " + taxon.getName());
				System.out.println("+ " + name2);
				System.out.println("------------------");
				if (commit) {
					AkpTaxon taxon2 = akpTaxonService.getTaxon(taxon.getXid());
					akpTaxonService.updateTaxonName(taxon2, name2);
				}
				n++;
			}
		}
		System.out.println(n + " results.");

		unitOfWork.end();
	}
}
