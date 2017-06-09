package textModeling;

public class SentenceQuery<T> {

	T query;

	public T getQuery() {
		return query;
	}

	public void setQuery(T query) {
		this.query = query;
	}
	
	public void clear() {
		query = null;
	}
}
