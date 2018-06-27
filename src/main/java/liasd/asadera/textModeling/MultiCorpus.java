package main.java.liasd.asadera.textModeling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import main.java.liasd.asadera.model.SummarizeModel;

public class MultiCorpus implements List<Corpus> {

	private List<Corpus> listCorpus = new ArrayList<Corpus>();
	private boolean hasAllModelSummary = true;
	protected int iD;
	protected SummarizeModel model;

	public MultiCorpus(int iD) {
		this.iD = iD;
	}

	public MultiCorpus(MultiCorpus m) {
		this.iD = m.getiD();
		for (Corpus c : m)
			listCorpus.add(new Corpus(c));
	}

	public SummarizeModel getModel() {
		return model;
	}

	public void setModel(SummarizeModel model) {
		this.model = model;
	}

	public int getNbDocument() {
		int nbDoc = 0;
		for (Corpus c : this)
			nbDoc += c.getNbDocument();
		return nbDoc;
	}

	public int getNbCorpus() {
		return listCorpus.size();
	}
	
	public int getiD() {
		return iD;
	}
	
	public boolean hasModelSummaries() {
		return hasAllModelSummary;
	}

	@Override
	public boolean add(Corpus e) {
		hasAllModelSummary = hasAllModelSummary && e.hasModelSummary();
		return listCorpus.add(e);
	}

	@Override
	public void add(int index, Corpus element) {
		hasAllModelSummary = hasAllModelSummary && element.hasModelSummary();
		listCorpus.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends Corpus> c) {
		for (Corpus corpus: c)
			hasAllModelSummary = hasAllModelSummary && corpus.hasModelSummary();
		return listCorpus.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Corpus> c) {
		for (Corpus corpus: c)
			hasAllModelSummary = hasAllModelSummary && corpus.hasModelSummary();
		return listCorpus.addAll(index, c);
	}

	@Override
	public void clear() {
		listCorpus.clear();
	}

	@Override
	public boolean contains(Object o) {
		return listCorpus.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return listCorpus.containsAll(c);
	}

	@Override
	public Corpus get(int index) {
		return listCorpus.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return listCorpus.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return listCorpus.isEmpty();
	}

	@Override
	public Iterator<Corpus> iterator() {
		return listCorpus.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return listCorpus.lastIndexOf(o);
	}

	@Override
	public ListIterator<Corpus> listIterator() {
		return listCorpus.listIterator();
	}

	@Override
	public ListIterator<Corpus> listIterator(int index) {
		return listCorpus.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return listCorpus.remove(o);
	}

	@Override
	public Corpus remove(int index) {
		return listCorpus.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return listCorpus.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return listCorpus.retainAll(c);
	}

	@Override
	public Corpus set(int index, Corpus element) {
		return listCorpus.set(index, element);
	}

	@Override
	public int size() {
		return listCorpus.size();
	}

	@Override
	public List<Corpus> subList(int fromIndex, int toIndex) {
		return listCorpus.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return listCorpus.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return listCorpus.toArray(a);
	}
}
