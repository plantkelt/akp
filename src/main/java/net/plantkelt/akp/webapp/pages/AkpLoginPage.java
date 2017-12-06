package net.plantkelt.akp.webapp.pages;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.authroles.authentication.panel.SignInPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;

import net.plantkelt.akp.service.AkpTaxonService;
import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

public class AkpLoginPage extends AkpPageTemplate {

	private static final long serialVersionUID = 1L;

	@Inject
	private AkpTaxonService akpTaxonService;

	private int fortuneCount = (int) (System.currentTimeMillis() % 9L);

	public AkpLoginPage() {
		this(null);
	}

	public AkpLoginPage(final PageParameters parameters) {

		SignInPanel signInPanel = new SignInPanel("signInPanel", false);
		signInPanel.setRememberMe(false);
		add(signInPanel);

		// Misc info (last update)
		Date lastUpdate = akpTaxonService.getLastUpdate();
		Label lastUpdateLabel = new Label("lastUpdate",
				getString("last.update") + " "
						+ SimpleDateFormat
								.getDateInstance(DateFormat.LONG,
										AkpWicketSession.get().getLocale())
								.format(lastUpdate));
		add(lastUpdateLabel);

		// Fortune
		IModel<String> fortuneModel = new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				fortuneCount++;
				if (fortuneCount > 8)
					fortuneCount = 0;
				return getString("fortune." + fortuneCount);
			}
		};
		Label fortuneLabel = new Label("fortune", fortuneModel);
		fortuneLabel.setEscapeModelStrings(false);
		add(fortuneLabel);

		// Subscribe link
		add(AkpSubscribePage.link("subscribeLink"));
	}

}
