package net.plantkelt.akp.webapp.elements;

import org.apache.wicket.ajax.AjaxRequestTarget;

import net.plantkelt.akp.domain.AkpPlant;

public interface AkpPlantRefAdderListener {

	public abstract void onPlantRefAdded(AjaxRequestTarget target,
			AkpPlant targetPlant);
}
