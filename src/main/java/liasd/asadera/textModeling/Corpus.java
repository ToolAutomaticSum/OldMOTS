package main.java.liasd.asadera.textModeling;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import main.java.liasd.asadera.model.AbstractModel;
import main.java.liasd.asadera.tools.wordFilters.WordFilter;

public class Corpus implements List<TextModel> {

	private List<TextModel> listTextModel = new ArrayList<TextModel>();
	private List<SentenceModel> listSentence;

	protected int iD;
	protected String corpusName;
	protected String inputPath;
	protected String summaryPath;
	protected AbstractModel model;
	protected List<String> docNames;
	protected List<String> summaryNames;

	public Corpus(int iD) {
		this.iD = iD;
		this.summaryNames = new ArrayList<String>();
	}

	public Corpus(Corpus c) {
		this.iD = c.getiD();
		this.corpusName = c.getCorpusName();
		this.inputPath = c.getInputPath();
		if (c.getSummaryPath() != null)
			this.summaryPath = c.getSummaryPath();
		this.model = c.getModel();
		this.docNames = new ArrayList<String>(c.getDocNames());
		if (c.getSummaryNames() != null)
			this.summaryNames = new ArrayList<String>(c.getSummaryNames());
		else
			this.summaryNames = new ArrayList<String>();

		for (TextModel t : c) {
			TextModel text = new TextModel(t);
			text.setParentCorpus(this);
			this.listTextModel.add(text);
		}
	}

	public void loadDocumentModels() {
		corpusName = new File(inputPath).getName(); //.split(File.separator)[inputPath.split(File.separator).length - 1];
		Iterator<String> it = docNames.iterator();
		while (it.hasNext()) {
			String docName = it.next();
			if (new File(inputPath + File.separator + docName).isFile())
				listTextModel.add(new TextModel(this, inputPath + File.separator + docName));
		}
	}

	public SentenceModel getSentenceByID(int id) {
		int current = id;
		boolean notFind = true;
		SentenceModel sen = null;
		Iterator<TextModel> textIt = this.iterator();
		while (notFind && textIt.hasNext()) {
			TextModel text = textIt.next();
			if (text.getNbSentence() <= current)
				current -= text.getNbSentence();
			else {
				sen = text.getSentenceByID(id);
				notFind = false;
			}
		}
		return sen;
	}

	public int getNbWord(WordFilter filter) {
		int nbWord = 0;
		for (TextModel t : this)
			nbWord += t.getNbWord(filter);
		return nbWord;
	}

	public int getNbSentence() {
		int nbSentence = 0;
		for (TextModel t : this)
			nbSentence += t.getNbSentence();
		return nbSentence;
	}

	public AbstractModel getModel() {
		return model;
	}

	public void setModel(AbstractModel model) {
		this.model = model;
	}

	public List<String> getDocNames() {
		return docNames;
	}

	public void setDocNames(List<String> docNames) {
		this.docNames = docNames;
	}

	public String getInputPath() {
		return inputPath;
	}

	public void setInputPath(String inputPath) {
		this.inputPath = inputPath;
	}

	public int getiD() {
		return iD;
	}

	public String getSummaryPath() {
		return summaryPath;
	}

	public void setSummaryPath(String summaryPath) {
		this.summaryPath = summaryPath;
	}

	public List<String> getSummaryNames() {
		return summaryNames;
	}

	public void setSummaryNames(List<String> summaryNames) {
		this.summaryNames = summaryNames;
	}
	
	public boolean hasModelSummary() {
		return summaryNames == null || summaryNames.isEmpty();
	}

	@Override
	public String toString() {
		int current = 0;
		for (TextModel text : this) {
			current += text.getNbSentence();
		}
		String str = "Corpus " + iD + " \n" + current + "\n";
		return str;
	}

	public List<String> getAllStringSentence() {
		List<String> allSentenceList = new ArrayList<String>();
		for (TextModel text : this) {
			allSentenceList.addAll(text.getStringSentence());
		}
		return allSentenceList;
	}

	public List<SentenceModel> getAllSentence() {
		if (listSentence == null) {
			listSentence = new ArrayList<SentenceModel>();
			for (TextModel text : this) {
				listSentence.addAll(text.getSentence());
			}
		}
		return listSentence;
	}

	public String getCorpusName() {
		return corpusName;
	}

	public int getNbDocument() {
		return docNames.size();
	}

	@Override
	public boolean add(TextModel e) {
		return listTextModel.add(e);
	}

	@Override
	public void add(int index, TextModel element) {
		listTextModel.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends TextModel> c) {
		return listTextModel.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends TextModel> c) {
		return listTextModel.addAll(index, c);
	}

	@Override
	public void clear() {
		listTextModel.clear();
		listSentence = null;
	}

	@Override
	public boolean contains(Object o) {
		return listTextModel.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return listTextModel.containsAll(c);
	}

	@Override
	public TextModel get(int index) {
		return listTextModel.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return listTextModel.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return listTextModel.isEmpty();
	}

	@Override
	public Iterator<TextModel> iterator() {
		return listTextModel.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return listTextModel.lastIndexOf(o);
	}

	@Override
	public ListIterator<TextModel> listIterator() {
		return listTextModel.listIterator();
	}

	@Override
	public ListIterator<TextModel> listIterator(int index) {
		return listTextModel.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return listTextModel.remove(o);
	}

	@Override
	public TextModel remove(int index) {
		return listTextModel.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return listTextModel.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return listTextModel.retainAll(c);
	}

	@Override
	public TextModel set(int index, TextModel element) {
		return listTextModel.set(index, element);
	}

	@Override
	public int size() {
		return listTextModel.size();
	}

	@Override
	public List<TextModel> subList(int fromIndex, int toIndex) {
		return listTextModel.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return listTextModel.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return listTextModel.toArray(a);
	}
}
