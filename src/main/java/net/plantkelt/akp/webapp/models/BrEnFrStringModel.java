package net.plantkelt.akp.webapp.models;

import java.util.Locale;

import net.plantkelt.akp.webapp.wicket.AkpWicketSession;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

/**
 * Model that display a "Br/En/Fr" string according to current locale.
 * 
 */
public class BrEnFrStringModel extends LoadableDetachableModel<String> {
	private static final long serialVersionUID = 1L;

	private static final String[] LANG_LIST = { "br", "en", "fr" };

	private IModel<String> model;

	public BrEnFrStringModel(IModel<String> model) {
		this.model = model;
	}

	public BrEnFrStringModel(String string) {
		this.model = new Model<String>(string);
	}

	@Override
	protected String load() {
		String str = model.getObject();
		String[] sstr = str.split("/");
		Locale locale = AkpWicketSession.get().getLocale();
		if (locale == null)
			locale = new Locale("en");
		String lang = locale.getLanguage();
		for (int i = 0; i < LANG_LIST.length && i < sstr.length; i++) {
			if (lang.equals(LANG_LIST[i]))
				return sstr[i].trim();
		}
		// In case something bad happened, return the whole string.
		return str;
	}

}
