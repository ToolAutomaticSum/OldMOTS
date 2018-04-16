package main.java.liasd.asadera.textModeling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Summary implements List<SentenceModel> {

	private List<SentenceModel> summary;
	private double score;

	public Summary() {
		this.summary = new ArrayList<SentenceModel>();
	}

	public Summary(Summary summary) {
		this.summary = new ArrayList<SentenceModel>(summary);
		this.score = summary.getScore();
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public int size(String mode) {
		int size = 0;
		switch (mode) {
		case "char":
			for (SentenceModel sen : summary)
				size += sen.toString().length();
			break;
		case "word":
			for (SentenceModel sen : summary)
				size += sen.getNbMot();
			break;
		case "sen":
			return size();
		default:
			return size();
		}
		return size;
	}

	@Override
	public boolean add(SentenceModel e) {
		if (summary.contains(e))
			return false;
		score = 0;
		return summary.add(e);
	}

	@Override
	public void add(int index, SentenceModel element) {
		if (!summary.contains(element)) {
			score = 0;
			summary.add(index, element);
		}
	}

	@Override
	public boolean addAll(Collection<? extends SentenceModel> c) {
		score = 0;
		boolean bool = true;
		for (SentenceModel sen : c)
			bool = bool || summary.add(sen);
		return bool;
	}

	@Override
	public boolean addAll(int index, Collection<? extends SentenceModel> c) {
		score = 0;
		boolean bool = true;
		for (SentenceModel sen : c)
			bool = bool || summary.add(sen);
		return bool;
	}

	@Override
	public void clear() {
		score = 0;
		summary.clear();
	}

	@Override
	public boolean contains(Object o) {
		return summary.contains(summary);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return summary.containsAll(c);
	}

	@Override
	public SentenceModel get(int index) {
		return summary.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return summary.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return summary.isEmpty();
	}

	@Override
	public Iterator<SentenceModel> iterator() {
		return summary.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return summary.lastIndexOf(o);
	}

	@Override
	public ListIterator<SentenceModel> listIterator() {
		return summary.listIterator();
	}

	@Override
	public ListIterator<SentenceModel> listIterator(int index) {
		return summary.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		score = 0;
		return summary.remove(o);
	}

	@Override
	public SentenceModel remove(int index) {
		score = 0;
		return summary.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		score = 0;
		return summary.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		score = 0;
		return summary.retainAll(c);
	}

	@Override
	public SentenceModel set(int index, SentenceModel element) {
		return summary.set(index, element);
	}

	@Override
	public int size() {
		return summary.size();
	}

	@Override
	public List<SentenceModel> subList(int fromIndex, int toIndex) {
		return summary.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return summary.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return summary.toArray(a);
	}

	@Override
	public String toString() {
		if (size() == 0)
			return "";
		String str = "";
		for (SentenceModel sen : summary)
			str += sen.toString() + "\n";
		return "Score : " + score + "\n" + size("word") + "\n" + str.substring(0, str.length() - 2);
	}
}
