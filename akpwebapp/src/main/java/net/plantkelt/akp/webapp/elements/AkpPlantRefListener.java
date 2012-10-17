package net.plantkelt.akp.webapp.elements;

import net.plantkelt.akp.domain.AkpPlant;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface AkpPlantRefListener {

	public abstract void onPlantRefRemoved(AjaxRequestTarget target, AkpPlant targetPlant);
}
