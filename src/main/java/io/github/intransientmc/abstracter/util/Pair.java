package io.github.intransientmc.abstracter.util;

import java.util.Objects;

public class Pair<A, B> {
	private final A a;
	private final B b;

	public Pair(A a, B b) {
		this.a = a;
		this.b = b;
	}

	public static <A, B> Pair<A, B> of(A a, B b) {
		return new Pair<>(a, b);
	}

	public A getA() {
		return this.a;
	}

	public B getB() {
		return this.b;
	}

	@Override public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Pair)) {
			return false;
		}

		Pair<?, ?> pair = (Pair<?, ?>) o;

		if (!Objects.equals(this.a, pair.a)) {
			return false;
		}
		return Objects.equals(this.b, pair.b);
	}

	@Override public int hashCode() {
		int result = this.a != null ? this.a.hashCode() : 0;
		result = 31 * result + (this.b != null ? this.b.hashCode() : 0);
		return result;
	}
}
