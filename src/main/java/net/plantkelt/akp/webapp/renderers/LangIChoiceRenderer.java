package net.plantkelt.akp.webapp.renderers;

import org.apache.wicket.markup.html.form.ChoiceRenderer;

import net.plantkelt.akp.domain.AkpLang;

public class LangIChoiceRenderer extends ChoiceRenderer<AkpLang> {
	private static final long serialVersionUID = 1L;

	public LangIChoiceRenderer() {
	}

	@Override
	public Object getDisplayValue(AkpLang lang) {
		return lang.getCode();
	}

	@Override
	public String getIdValue(AkpLang lang, int index) {
		return lang.getXid();
	}

}
