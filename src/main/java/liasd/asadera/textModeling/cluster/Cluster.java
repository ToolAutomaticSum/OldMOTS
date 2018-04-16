package main.java.liasd.asadera.textModeling.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import main.java.liasd.asadera.textModeling.SentenceModel;

public class Cluster implements List<SentenceModel> {

	protected int id;
	protected List<SentenceModel> listSentenceTopic = new ArrayList<SentenceModel>();

	public Cluster(int id) {
		super();
		this.id = id;
	}

	public Cluster(int id, SentenceModel sen) {
		super();
		this.id = id;
		listSentenceTopic.add(sen);
	}

	public Cluster(int id, List<SentenceModel> listSentenceTopic) {
		this.id = id;
		this.listSentenceTopic = listSentenceTopic;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public boolean add(SentenceModel e) {
		return listSentenceTopic.add(e);
	}

	@Override
	public void add(int index, SentenceModel element) {
		listSentenceTopic.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends SentenceModel> c) {
		return listSentenceTopic.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends SentenceModel> c) {
		return listSentenceTopic.addAll(index, c);
	}

	@Override
	public void clear() {
		listSentenceTopic.clear();
	}

	@Override
	public boolean contains(Object o) {
		return listSentenceTopic.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return listSentenceTopic.containsAll(c);
	}

	@Override
	public SentenceModel get(int index) {
		return listSentenceTopic.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return listSentenceTopic.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return listSentenceTopic.isEmpty();
	}

	@Override
	public Iterator<SentenceModel> iterator() {
		return listSentenceTopic.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return listSentenceTopic.lastIndexOf(o);
	}

	@Override
	public ListIterator<SentenceModel> listIterator() {
		return listSentenceTopic.listIterator();
	}

	@Override
	public ListIterator<SentenceModel> listIterator(int index) {
		return listSentenceTopic.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return listSentenceTopic.remove(o);
	}

	@Override
	public SentenceModel remove(int index) {
		return listSentenceTopic.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return listSentenceTopic.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return listSentenceTopic.retainAll(c);
	}

	@Override
	public SentenceModel set(int index, SentenceModel element) {
		return listSentenceTopic.set(index, element);
	}

	@Override
	public int size() {
		return listSentenceTopic.size();
	}

	@Override
	public List<SentenceModel> subList(int fromIndex, int toIndex) {
		return listSentenceTopic.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return listSentenceTopic.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return listSentenceTopic.toArray(a);
	}

	@Override
	public String toString() {
		String t = id + "\n";
		for (SentenceModel sen : listSentenceTopic)
			t += sen.toString() + "\n";
		return t;
	}
}
