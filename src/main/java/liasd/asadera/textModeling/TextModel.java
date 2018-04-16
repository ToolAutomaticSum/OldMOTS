package main.java.liasd.asadera.textModeling;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import main.java.liasd.asadera.tools.wordFilters.WordFilter;

public class TextModel implements List<SentenceModel> {

	private List<SentenceModel> listSentenceModel = new ArrayList<SentenceModel>();

	private static int iD = 0;
	private int textID = iD;
	private Corpus parentCorpus;
	protected String documentFilePath;
	protected String textName;
	protected List<String> labels = new ArrayList<String>();

	protected int textSize = 0;
	protected String text = "";

	protected int nbSentence;

	public TextModel(Corpus parentCorpus, String filePath) {
		this.parentCorpus = parentCorpus;
		iD++;
		documentFilePath = filePath;
		textName = documentFilePath.split(File.separator)[documentFilePath.split(File.separator).length - 1];
	}

	public TextModel(TextModel t) {
		this.textID = t.getiD();
		this.documentFilePath = t.getDocumentFilePath();
		this.textName = t.getTextName();
		this.textSize = t.getTextSize();
		this.text = t.getText();

		for (SentenceModel s : t) {
			SentenceModel sen = new SentenceModel(s);
			sen.setText(this);
			listSentenceModel.add(sen);
		}
	}

	public String getDocumentFilePath() {
		return documentFilePath;
	}

	public void setDocumentFilePath(String documentFilePath) {
		this.documentFilePath = documentFilePath;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public SentenceModel getSentenceByID(int id) {
		boolean notFind = true;
		SentenceModel sen = null;
		Iterator<SentenceModel> senIt = this.iterator();
		while (notFind && senIt.hasNext()) {
			SentenceModel sent = senIt.next();
			if (sent.getiD() == id) {
				sen = sent;
				notFind = false;
			}
		}
		return sen;
	}

	public List<SentenceModel> getSentence() {
		List<SentenceModel> listSentence = new ArrayList<SentenceModel>();
		for (SentenceModel s : this) {
			if (!s.getSentence().equals(""))
				listSentence.add(s);
		}
		return listSentence;
	}

	public List<String> getStringSentence() {
		List<String> listSentence = new ArrayList<String>();
		for (SentenceModel s : this) {
			if (!s.getSentence().equals(""))
				listSentence.add(s.getSentence());
		}
		return listSentence;
	}

	public int getNbSentence() {
		return nbSentence;
	}

	public void setNbSentence(int nbSentence) {
		this.nbSentence = nbSentence;
	}

	public int getTextSize() {
		return textSize;
	}

	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}

	@Override
	public String toString() {
		String str = "";
		for (int i = 0; i < size(); i++) {
			str += get(i).toString() + "\n";
		}
		return str;
	}

	public int getiD() {
		return textID;
	}

	public Corpus getParentCorpus() {
		return parentCorpus;
	}

	public void setParentCorpus(Corpus parentCorpus) {
		this.parentCorpus = parentCorpus;
	}

	public int getNbWord(WordFilter filter) {
		int nbWord = 0;
		for (SentenceModel p : this)
			nbWord += p.getLength(filter);
		return nbWord;
	}

	public List<String> getLabels() {
		return labels;
	}

	public String getTextName() {
		return textName;
	}

	@Override
	public boolean add(SentenceModel e) {
		return listSentenceModel.add(e);
	}

	@Override
	public void add(int index, SentenceModel element) {
		listSentenceModel.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends SentenceModel> c) {
		return listSentenceModel.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends SentenceModel> c) {
		return listSentenceModel.addAll(index, c);
	}

	@Override
	public void clear() {
		listSentenceModel.clear();
	}

	@Override
	public boolean contains(Object o) {
		return listSentenceModel.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return listSentenceModel.containsAll(c);
	}

	@Override
	public SentenceModel get(int index) {
		return listSentenceModel.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return listSentenceModel.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return listSentenceModel.isEmpty();
	}

	@Override
	public Iterator<SentenceModel> iterator() {
		return listSentenceModel.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return listSentenceModel.lastIndexOf(o);
	}

	@Override
	public ListIterator<SentenceModel> listIterator() {
		return listSentenceModel.listIterator();
	}

	@Override
	public ListIterator<SentenceModel> listIterator(int index) {
		return listSentenceModel.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return listSentenceModel.remove(o);
	}

	@Override
	public SentenceModel remove(int index) {
		return listSentenceModel.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return listSentenceModel.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return listSentenceModel.retainAll(c);
	}

	@Override
	public SentenceModel set(int index, SentenceModel element) {
		return listSentenceModel.set(index, element);
	}

	@Override
	public int size() {
		return listSentenceModel.size();
	}

	@Override
	public List<SentenceModel> subList(int fromIndex, int toIndex) {
		return listSentenceModel.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return listSentenceModel.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return listSentenceModel.toArray(a);
	}
}
