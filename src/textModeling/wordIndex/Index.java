package textModeling.wordIndex;

import java.util.HashMap;

public class Index extends HashMap<Integer, WordIndex> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4164985351782026778L;
	
	private HashMap<String, Integer> hashMapIndex = new HashMap<String, Integer>();
	private int nbDoc;
	
	/** TODO Non utilis�, utile ?
	 * Tf et Idf par documents : Key = idCorpus
	 */
	private HashMap<Integer, Integer> corpusNbDoc = new HashMap<Integer, Integer>();
	
	public Index() {
		super();
	}

	public Index(int nbDoc) {
		super();
		this.nbDoc = nbDoc;
	}

	public int getNbDocument() {
		return nbDoc;
	}

	public void setNbDocument(int nbDoc) {
		this.nbDoc = nbDoc;
	}

	/*public HashMap<Integer, Integer> getCorpusNbDoc() {
		return corpusNbDoc;
	}*/
	
	public void putCorpusNbDoc(int corpusId, int nbDoc) {
		corpusNbDoc.put(corpusId, nbDoc);
	}
	
	@Override
	public WordIndex put(Integer key, WordIndex value) {
		int iD = hashMapIndex.size();
		value.setId(iD);
		hashMapIndex.put(value.getWord(), iD);
		return super.put(iD, value);
	}
	
	public WordIndex put(String key, WordIndex value) {
		int iD = hashMapIndex.size();
		value.setId(iD);
		hashMapIndex.put(key, iD);
		return super.put(iD, value);
	}

	public WordIndex put(String key, WordIndex value, int iD) {
		value.setId(iD);
		hashMapIndex.put(key, iD);
		return super.put(iD, value);
	}
	
	public Integer getKeyId(String key) {
		return hashMapIndex.get(key);
	}
	
	public WordIndex get(String key) {
		return super.get(hashMapIndex.get(key));
	}
	
	public boolean containsKey(String key) {
		return hashMapIndex.containsKey(key);
	}
	
	@Override
	public WordIndex remove(Object key) {
		System.out.println("Remove !!");
		return super.remove(key);
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
	
	@Override
	public void clear() {
		hashMapIndex.clear();
		super.clear();
		nbDoc = 0;
		corpusNbDoc.clear();
	}
}