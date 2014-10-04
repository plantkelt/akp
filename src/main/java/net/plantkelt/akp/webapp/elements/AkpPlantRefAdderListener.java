package net.plantkelt.akp.webapp.elements;

import java.io.Serializable;

import net.plantkelt.akp.domain.AkpPlant;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface AkpPlantRefAdderListener extends Serializable {

	public abstract void onPlantRefAdded(AjaxRequestTarget target,
			AkpPlant targetPlant);
}
