package net.plantkelt.akp.webapp.elements;

import java.io.Serializable;

import net.plantkelt.akp.domain.AkpPlant;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface AkpPlantRefListener extends Serializable {

	public abstract void onPlantRefRemoved(AjaxRequestTarget target,
			AkpPlant targetPlant);
}
