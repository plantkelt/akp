package net.plantkelt.akp.domain;

import java.io.Serializable;

public class AkpTagValue implements Serializable {

	private static final long serialVersionUID = 1L;

	private int type;
	private int value;
	private String name;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("[AkpTagValue %d %d %s]", getType(), getValue(),
				getName());
	}
}
