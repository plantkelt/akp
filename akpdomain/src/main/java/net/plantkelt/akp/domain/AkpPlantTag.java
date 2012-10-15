package net.plantkelt.akp.domain;

import java.io.Serializable;

public class AkpPlantTag implements Comparable<AkpPlantTag>, Serializable {

	private static final long serialVersionUID = 1L;

	private int type;
	private String stringValue;
	private Integer intValue;
	private AkpPlant plant;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public Integer getIntValue() {
		return intValue;
	}

	public void setIntValue(Integer intValue) {
		this.intValue = intValue;
	}

	public AkpPlant getPlant() {
		return plant;
	}

	public void setPlant(AkpPlant plant) {
		this.plant = plant;
	}

	@Override
	public int hashCode() {
		return type;
	}

	@Override
	public boolean equals(Object another) {
		if (another instanceof AkpPlantTag)
			return false;
		AkpPlantTag anotherTag = (AkpPlantTag) another;
		return anotherTag.getType() == getType();
	}

	@Override
	public int compareTo(AkpPlantTag o) {
		return getType() < o.getType() ? -1
				: (getType() == o.getType() ? 0 : 1);
	}

	@Override
	public String toString() {
		return String.format("[AkpPlantTag %d %d %s]", type, intValue,
				stringValue);
	}

}
