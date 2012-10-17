package net.plantkelt.akp.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AkpPlantTag implements Comparable<AkpPlantTag>, Serializable {

	private static final long serialVersionUID = 1L;

	private static final List<Integer> TYPES;
	private static final Map<Integer, String> TYPE_7_VALUES;
	private static final Map<Integer, String> TYPE_8_VALUES;

	static {
		TYPES = new ArrayList<Integer>();
		TYPES.add(7);
		TYPES.add(8);
		TYPES.add(9);
		TYPES.add(10);
		TYPES.add(11);

		TYPE_7_VALUES = new HashMap<Integer, String>();
		TYPE_7_VALUES.put(1, "gouez / native / spontané");
		TYPE_7_VALUES.put(2, "nann gouez / not native / non spontané");

		TYPE_8_VALUES = new HashMap<Integer, String>();
		TYPE_8_VALUES.put(1, "gwez / tree / arbre");
		TYPE_8_VALUES.put(4, "gwez2 / tree2 / arbre2");
	}

	private int type;
	private String stringValue;
	private Integer intValue;
	private AkpPlant plant;

	public AkpPlantTag() {
	}

	public AkpPlantTag(AkpPlant plant, int tagType) {
		setType(tagType);
		setPlant(plant);
		if (isTypeString()) {
			setStringValue("Aaa");
			setIntValue(0);
		} else {
			setStringValue("");
			setIntValue(getAllIntPossibleValues().iterator().next());
		}
	}

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

	public String getValue() {
		String retval = getIntValueAsString(intValue);
		if (retval == null)
			retval = stringValue;
		return retval;
	}

	public String getIntValueAsString(int value) {
		switch (type) {
		case 7:
			return TYPE_7_VALUES.get(value);
		case 8:
			return TYPE_8_VALUES.get(value);
		default:
			return null;
		}
	}

	public Set<Integer> getAllIntPossibleValues() {
		switch (type) {
		case 7:
			return TYPE_7_VALUES.keySet();
		case 8:
			return TYPE_8_VALUES.keySet();
		default:
			throw new IllegalArgumentException("Invalid type");
		}
	}

	public static List<Integer> getAvailableTypes() {
		return TYPES;
	}

	public boolean isTypeString() {
		return !(type == 7 || type == 8);
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
