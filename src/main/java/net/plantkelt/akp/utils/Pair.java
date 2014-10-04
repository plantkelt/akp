package net.plantkelt.akp.utils;

import java.io.Serializable;

public class Pair<E1, E2> implements Serializable {

	private static final long serialVersionUID = -8280230942468359316L;

	private final E1 a;

	private final E2 b;

	public Pair(E1 a, E2 b) {
		this.a = a;
		this.b = b;
	}

	public E1 getFirst() {
		return a;
	}

	public E2 getSecond() {
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Pair<?, ?> other = (Pair<?, ?>) obj;
		if (a == null) {
			if (other.a != null)
				return false;
		} else if (!a.equals(other.a))
			return false;
		if (b == null) {
			if (other.b != null)
				return false;
		} else if (!b.equals(other.b))
			return false;
		return true;
	}

	public String toString() {
		return "T2(" + a + ", " + b + ")";
	}
}
