package net.plantkelt.akp.webapp.components;

import java.util.ArrayList;
import java.util.List;

import net.plantkelt.akp.domain.AkpClass;
import net.plantkelt.akp.webapp.pages.AkpClassPage;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

public class AkpParentClassPathLabel extends Panel {

	private static final long serialVersionUID = 1L;

	public AkpParentClassPathLabel(String id, AkpClass akpClass) {
		super(id);
		RepeatingView classPathRepeat = new RepeatingView("classPathRepeat");
		List<AkpClass> parentList = new ArrayList<AkpClass>();
		while (akpClass != null) {
			parentList.add(0, akpClass);
			akpClass = akpClass.getParent();
		}
		AkpClass rootClass = new AkpClass();
		rootClass.setName("...");
		parentList.add(0, rootClass);
		for (AkpClass cls : parentList) {
			WebMarkupContainer item = new WebMarkupContainer(
					classPathRepeat.newChildId());
			classPathRepeat.add(item);
			Link<AkpClassPage> link = AkpClassPage.link("classLink",
					cls.getXid());
			item.add(link);
			Label classNameLabel = new Label("className", cls.getHtmlName());
			classNameLabel.setEscapeModelStrings(false);
			link.add(classNameLabel);
		}
		add(classPathRepeat);
	}
}
