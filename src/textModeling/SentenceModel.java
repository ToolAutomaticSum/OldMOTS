package textModeling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

import textModeling.wordIndex.Index;
import textModeling.wordIndex.NGram;
import textModeling.wordIndex.WordIndex;

public class SentenceModel implements List<WordModel>, Comparable<SentenceModel> {

	private List<WordModel> listWordModel = new ArrayList<WordModel>();
	
	protected String sentence;
	protected int nbMot;
	protected Set<NGram> listNGram;
	protected int iD;
	protected double score;
	protected TextModel text;
	
	public SentenceModel(String sen) {
		sentence = sen;
	}
	
	public SentenceModel(String sen, int iD, TextModel text) {
		sentence = sen;
		this.iD = iD;
		this.text = text;
	}
	
	public SentenceModel(SentenceModel s) {
		this.sentence = s.toString();
		this.iD = s.getiD();
		this.nbMot = s.getNbMot();
		for (WordModel w : s) {
			WordModel word = new WordModel(w);
			word.setSentence(this);
			listWordModel.add(word);
		}
	}

	/**
	 * 
	 * @return Sentence as a list of lemme with  stopword notified with "%%" before them
	 */
	public String getSentence() {
		String txt = "";
		for (WordModel w : this) {
			if (!w.isStopWord())
				txt += w.toString() + " ";
			else
				txt += "%%" + w.toString() + " ";
		}
		return txt;
	}
	
	public String getRawSentence() {
		String txt = "";
		for (WordModel w : this)
			txt += w.toString() + " ";
		return txt;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	/*public ArrayList<WordModel> getListWord() {
		return listWord;
	}

	public void setListWord(ArrayList<WordModel> listWord) {
		this.listWord = listWord;
	}*/
	
	public int getiD() {
		return iD;
	}

	public void setiD(int iD) {
		this.iD = iD;
	}

	/*public Caracteristic getCaracteristic() {
		return caracteristic;
	}

	public void setCaracteristic(Caracteristic caracteristic) {
		this.caracteristic = caracteristic;
	}*/

	/**
	 * @return full sentence
	 */
	@Override
	public String toString() {
		return sentence;
	}
	
	public static String listSentenceModelToString (List<SentenceModel> list) {
		String str = "";
		Iterator<SentenceModel> it = list.iterator();
		while (it.hasNext())
			str += it.next().toString() + "\n";
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
	
	private void getListNGrams(int n, Index<WordIndex> indexWord, Index<NGram> index) {
		Set<NGram> ngrams_list = new TreeSet<NGram>() ;
		WordModel u;
		
		if (n == 1) {
			for (WordModel u1 : this) {
				NGram ng = new NGram(index);
				if (!u1.isStopWord()) {
					WordIndex w = indexWord.get(u1.getmLemma());
					if (w != null) {
						ng.add(w);
						if (index != null && index.values().contains(ng))
							ng = index.get(ng.getWord());
						else if (index != null) {
							index.put(0, ng);
						}
						ngrams_list.add(ng);
					}
				}
			}
		}
		else {
			for (int i = 0; i < this.size() - n + 1; i++)
			{
				boolean cond = false;
				boolean stopWord = false; //Un stopWord par Ngram
				NGram ng = new NGram(index);

				for (int j = i; j < i + n; j++)
				{
					//System.out.println("j : "+j);
					u = this.get(j);
	
					if ((!stopWord && !u.isStopWord()) || (!stopWord && u.isStopWord()) || (stopWord && !u.isStopWord())) {
						cond = true;
						WordIndex w = indexWord.get(u.getmLemma());
						if (w != null)
							ng.add(w);
						else {
							System.out.println("BREAK!!!" + u.getmLemma());
							cond = false;
							break;
						}
						if (u.isStopWord())
							stopWord = true;
					} else
						cond = false;
				}
				if (cond) {
					if (index != null && index.values().contains(ng))
						ng = index.get(ng.getWord());
					else if (index != null) {
						index.put(0, ng);
						System.out.println("BREAK!!! " + ng);
					}
					ngrams_list.add(ng);
				}
				//else
					//System.out.println("Filtrée !");
			}
		}
		listNGram = ngrams_list;
	}

	public ArrayList<NGram> getNGrams(int n, Index<WordIndex> indexWord, Index<NGram> index) {
		if(listNGram == null)
			getListNGrams(n, indexWord, index);
		return new ArrayList<NGram>(listNGram);
	}
	
	public Set<NGram> getNGrams() {
		return listNGram;
	}
	
	public void setNGrams(Set<NGram> listNGram) {
		this.listNGram = listNGram;
	}
	
	public double getPosScore() {
		if (text.size() > 1)
			return (double)(text.size() - 1 - text.indexOf(this)) / (double)(text.size() - 1);
		else
			return 1;
	}
	
	public int getPosition() {
		return (text.indexOf(this)+1);
	}
	
	public TextModel getText() {
		return text;
	}

	public void setText(TextModel text) {
		this.text = text;
	}

	public int getLength() {
		int n = 0;
		for (WordModel w : this) {
			if (!w.isStopWord())
				n++;
		}
		return n;
	}

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
	public boolean add(WordModel e) {
		return listWordModel.add(e);
	}

	@Override
	public void add(int index, WordModel element) {
		listWordModel.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends WordModel> c) {
		return listWordModel.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends WordModel> c) {
		return listWordModel.addAll(index, c);
	}

	@Override
	public void clear() {
		listWordModel.clear();
	}

	@Override
	public boolean contains(Object o) {
		return listWordModel.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return listWordModel.containsAll(c);
	}

	@Override
	public WordModel get(int index) {
		return listWordModel.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return listWordModel.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return listWordModel.isEmpty();
	}

	@Override
	public Iterator<WordModel> iterator() {
		return listWordModel.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return listWordModel.lastIndexOf(o);
	}

	@Override
	public ListIterator<WordModel> listIterator() {
		return listWordModel.listIterator();
	}

	@Override
	public ListIterator<WordModel> listIterator(int index) {
		return listWordModel.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return listWordModel.remove(o);
	}

	@Override
	public WordModel remove(int index) {
		return listWordModel.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return listWordModel.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return listWordModel.retainAll(c);
	}

	@Override
	public WordModel set(int index, WordModel element) {
		return listWordModel.set(index, element);
	}

	@Override
	public int size() {
		return listWordModel.size();
	}

	@Override
	public List<WordModel> subList(int fromIndex, int toIndex) {
		return listWordModel.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return listWordModel.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return listWordModel.toArray(a);
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
}
