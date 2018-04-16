package main.java.liasd.asadera.tools;

public class Pair<S, T extends Comparable<T>> implements Comparable<Pair<S, T>> {

	private S key;
	private T value;

	public Pair(S key, T value) {
		super();
		this.key = key;
		this.value = value;
	}

	public S getKey() {
		return key;
	}

	public void setKey(S key) {
		this.key = key;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public int compareTo(Pair<S, T> o) {
		return o.getValue().compareTo(value);
	}
}
