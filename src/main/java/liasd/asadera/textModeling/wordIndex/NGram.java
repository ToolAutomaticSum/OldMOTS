package main.java.liasd.asadera.textModeling.wordIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author valnyz
 *
 */
public class NGram extends WordIndex implements List<WordIndex> {

	private static Logger logger = LoggerFactory.getLogger(NGram.class);

	private List<WordIndex> listWord = new ArrayList<WordIndex>();

	public NGram() {
		super();
		word = "";
	}

	public NGram(String ngram) {
		super(ngram);
	}

	/**
	 * @param ng
	 */
	public NGram(NGram ng) {
		super(ng.getWord());
		listWord.addAll(ng);
	}

	@Override
	public int compareTo(WordIndex o) {
		if (o.getClass() != this.getClass())
			return super.compareTo(o);
		NGram ngram = (NGram) o;
		if (ngram.size() < this.size())
			return 1;
		if (this.size() < ngram.size())
			return -1;
		for (int i = 0; i < this.size(); i++) {
			if (!this.get(i).getiD().equals(ngram.get(i).getiD()))
				return this.get(i).compareTo(ngram.get(i));
		}
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (o.getClass() != this.getClass())
			return false;
		NGram ngram = (NGram) o;
		if (ngram.size() != this.size())
			return false;
		for (int i = 0; i < this.size(); i++) {
			if (!this.get(i).equals(ngram.get(i)))
				return false;
		}
		return true;
	}

	public void printNGram() {
		for (WordIndex i : listWord) {
			logger.debug(" | " + i);
		}
	}

	public void removeLastGram() {
		if (this.size() == 0)
			return;
		this.remove(this.size() - 1);
	}

	public void removeFirstGram() {
		if (this.size() == 0)
			return;
		this.remove(0);
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public int hashCode() {
		String word = "";
		for (WordIndex w : this) {
			word += w.getWord() + "_";
		}
		return word.hashCode();
	}

	@Override
	public boolean add(WordIndex e) {
		setWord(getWord() + " | " + e.getWord());
		return listWord.add(e);
	}

	@Override
	public void add(int index, WordIndex element) {
		setWord(getWord() + " | " + element.getWord());
		listWord.add(element);
	}

	@Override
	public boolean addAll(Collection<? extends WordIndex> c) {
		String s = "";
		for (WordIndex w : c)
			s += " | " + w.getWord();
		setWord(getWord() + s);
		return listWord.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends WordIndex> c) {
		return listWord.addAll(c);
	}

	@Override
	public void clear() {
		listWord.clear();
	}

	@Override
	public boolean contains(Object o) {
		return listWord.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return containsAll(c);
	}

	@Override
	public WordIndex get(int index) {
		return listWord.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return listWord.isEmpty();
	}

	@Override
	public Iterator<WordIndex> iterator() {
		return listWord.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return listWord.lastIndexOf(o);
	}

	@Override
	public ListIterator<WordIndex> listIterator() {
		return listWord.listIterator();
	}

	@Override
	public ListIterator<WordIndex> listIterator(int index) {
		return listWord.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return listWord.remove(o);
	}

	@Override
	public WordIndex remove(int index) {
		return listWord.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return retainAll(c);
	};

	@Override
	public WordIndex set(int index, WordIndex element) {
		return set(index, element);
	}

	@Override
	public int size() {
		return listWord.size();
	}

	@Override
	public List<WordIndex> subList(int fromIndex, int toIndex) {
		return listWord.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return listWord.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return listWord.toArray(a);
	}

}
