package main.java.liasd.asadera.textModeling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;
import main.java.liasd.asadera.tools.wordFilters.WordFilter;

public class SentenceModel implements List<WordIndex>, Comparable<SentenceModel> {

	private static Logger logger = LoggerFactory.getLogger(SentenceModel.class);

	private List<WordModel> listWordModel = new ArrayList<WordModel>();

	protected int n;
	protected Map<Integer, List<WordIndex>> mapNGram;

	protected String sentence;
	protected int nbMot;
	protected int iD;
	protected double score;
	protected TextModel text;

	public SentenceModel(String sen) {
		mapNGram = new HashMap<Integer, List<WordIndex>>();
		sentence = sen;
	}

	public SentenceModel(String sen, int iD, TextModel text) {
		mapNGram = new HashMap<Integer, List<WordIndex>>();
		sentence = sen;
		this.iD = iD;
		this.text = text;
	}

	public SentenceModel(SentenceModel s) {
		mapNGram = new HashMap<Integer, List<WordIndex>>();
		this.sentence = s.toString();
		this.iD = s.getiD();
		this.nbMot = s.getNbMot();
		for (WordModel w : s.getListWordModel()) {
			WordModel word = new WordModel(w);
			word.setSentence(this);
			listWordModel.add(word);
		}
	}

	public List<WordModel> getListWordModel() {
		return listWordModel;
	}

	/**
	 * 
	 * @return Sentence as a list of lemme with stopword notified with "%%" before
	 *         them
	 */
	public String getSentence() {
		String txt = "";
		for (WordModel w : listWordModel) {
			if (!w.isStopWord())
				txt += w.toString() + " ";
			else
				txt += "%%" + w.toString() + " ";
		}
		return txt;
	}

	public String getRawSentence() {
		String txt = "";
		for (WordModel w : listWordModel)
			txt += w.toString() + " ";
		return txt;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public int getiD() {
		return iD;
	}

	public void setiD(int iD) {
		this.iD = iD;
	}

	/**
	 * @return full sentence
	 */
	@Override
	public String toString() {
		return sentence;
	}

	public static String listSentenceModelToString(List<SentenceModel> list, boolean verbose) {
		String str = "";
		if (verbose) {
			int nbMot = 0;
			for (SentenceModel sen : list)
				nbMot += sen.getNbMot();
			str += nbMot + "\n";
		}
		for (SentenceModel sen : list) {
			str += ((verbose) ? sen.getNbMot() + "\t" + sen.getScore() + "\t" : "\t") + sen.toString() + "\n";
		}
		return str;
	}

	@Override
	public int hashCode() {
		return (text.getParentCorpus().getiD() + "_" + iD).hashCode();
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public void setN(int n) {
		this.n = n;
	}

	public int getN() {
		return n;
	}

	@SuppressWarnings("unlikely-arg-type")
	public static void getUnigram(SentenceModel sen, Index<WordIndex> index, WordFilter filter) {
		List<WordIndex> ngrams_list = new ArrayList<WordIndex>();
		for (WordModel u1 : sen.getListWordModel())
			if (filter.passFilter(u1) && index.containsKey(u1.getmLemma()))
				ngrams_list.add(index.get(u1.getmLemma()));
		sen.setListWordIndex(1, ngrams_list);
	}

	@SuppressWarnings("unlikely-arg-type")
	public static void getListNGrams(SentenceModel sen, int n, Index<WordIndex> index, Index<NGram> indexNG,
			WordFilter filter) {
		if (n == 1)
			getUnigram(sen, index, filter);
		else {
			List<WordIndex> ngrams_list = new ArrayList<WordIndex>();
			WordModel u;
			for (int i = 0; i < sen.size() - n + 1; i++) {
				boolean cond = false;
				boolean filtered = false;
				NGram ng = new NGram();

				for (int j = i; j < i + n; j++) {
					u = sen.getListWordModel().get(j);

					if (!filtered || (filtered && !filter.passFilter(u))) {
						cond = true;
						WordIndex w = index.get(u.getmLemma());
						if (w != null)
							ng.add(w);
						else {
							logger.error("BREAK!!! " + u.getmLemma());
							cond = false;
							break;
						}
						if (u.isStopWord())
							filtered = true;
					} else
						cond = false;
				}
				if (cond) {
					if (indexNG != null && indexNG.values().contains(ng))
						ng = indexNG.get(ng.getWord());
					else if (indexNG != null) {
						indexNG.put(0, ng);
						logger.error("BREAK!!! " + ng);
					}
					ngrams_list.add(ng);
				}
			}
			sen.setListWordIndex(n, ngrams_list);
		}
	}

	public List<WordIndex> getListWordIndex(int n) {
		if (n <= 0)
			return mapNGram.get(1);
		else
			return mapNGram.get(n);
	}

	public void setListWordIndex(int n, Collection<? extends WordIndex> listWordIndex) {
		this.mapNGram.put(n, new ArrayList<WordIndex>(listWordIndex));
	}

	public double getPosScore() {
		if (text.size() > 1)
			return (double) (text.size() - 1 - text.indexOf(this)) / (double) (text.size() - 1);
		else
			return 1;
	}

	public int getPosition() {
		return (text.indexOf(this) + 1);
	}

	public TextModel getText() {
		return text;
	}

	public void setText(TextModel text) {
		this.text = text;
	}

	/**
	 * Iterate words in sentence and if the word pass the filter, add it to the
	 * length
	 * 
	 * @return int, number of words of the sentence passing through the filter
	 */
	public int getLength(WordFilter filter) {
		int n = 0;
		for (WordModel w : listWordModel) {
			if (filter.passFilter(w))
				n++;
		}
		return n;
	}

	/**
	 * Return the length of the sentence via the preprocessing step (with stop
	 * words)
	 * 
	 * @return int, length of the sentence with stop words
	 */
	public int getNbMot() {
		return nbMot;
	}

	public void setNbMot(int nbMot) {
		this.nbMot = nbMot;
	}

	public List<String> getLabels() {
		return text.getLabels();
	}

	@Override
	public boolean add(WordIndex e) {
		return mapNGram.get(n).add(e);
	}

	@Override
	public void add(int index, WordIndex element) {
		mapNGram.get(n).add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends WordIndex> c) {
		return mapNGram.get(n).addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends WordIndex> c) {
		return mapNGram.get(n).addAll(index, c);
	}

	@Override
	public void clear() {
		listWordModel.clear();
		mapNGram.clear();
	}

	@Override
	public boolean contains(Object o) {
		return mapNGram.get(n).contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return mapNGram.get(n).containsAll(c);
	}

	@Override
	public WordIndex get(int index) {
		return mapNGram.get(n).get(index);
	}

	@Override
	public int indexOf(Object o) {
		return listWordModel.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return mapNGram.get(n).isEmpty();
	}

	@Override
	public Iterator<WordIndex> iterator() {
		return mapNGram.get(n).iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return mapNGram.get(n).lastIndexOf(o);
	}

	@Override
	public ListIterator<WordIndex> listIterator() {
		return mapNGram.get(n).listIterator();
	}

	@Override
	public ListIterator<WordIndex> listIterator(int index) {
		return mapNGram.get(n).listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return mapNGram.get(n).remove(o);
	}

	@Override
	public WordIndex remove(int index) {
		return mapNGram.get(n).remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return mapNGram.get(n).removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return mapNGram.get(n).retainAll(c);
	}

	@Override
	public WordIndex set(int index, WordIndex element) {
		return mapNGram.get(n).set(index, element);
	}

	@Override
	public int size() {
		if (mapNGram.isEmpty())
			return listWordModel.size();
		else
			return mapNGram.get(n).size();
	}

	@Override
	public List<WordIndex> subList(int fromIndex, int toIndex) {
		return mapNGram.get(n).subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return mapNGram.get(n).toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return mapNGram.get(n).toArray(a);
	}

	@Override
	public int compareTo(SentenceModel o) {
		if (o.getiD() > this.getiD())
			return -1;
		else if (o.getiD() < this.getiD())
			return 1;
		else
			return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SentenceModel other = (SentenceModel) obj;
		if (sentence == null) {
			if (other.sentence != null)
				return false;
		} else if (iD == other.iD)
			return true;
		else if (!sentence.equals(other.sentence))
			return false;
		return true;
	}
}
