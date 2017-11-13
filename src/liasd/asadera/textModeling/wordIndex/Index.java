package liasd.asadera.textModeling.wordIndex;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Index<T extends WordIndex> implements Map<Integer, T> {
	
	//private final Class<T> classT;
	private HashMap<Integer, T> mapWord = new HashMap<Integer, T>();
	private HashMap<String, Integer> mapIndex = new HashMap<String, Integer>();
	private int nbDoc;

	private HashMap<Integer, Integer> corpusNbDoc = new HashMap<Integer, Integer>();
	
	public Index(/*Class<T> classT*/) {
		super();
		//this.classT = classT;
	}

	public Index(/*Class<T> classT,*/ int nbDoc) {
		super();
		//this.classT = classT;
		this.nbDoc = nbDoc;
	}

	/*public Class<T> getClassT() {
		return classT;
	}*/

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
	
	public T put(T value) {
		int iD = mapIndex.size();
		value.setiD(iD);
		value.setIndex(this);
		mapIndex.put(value.getWord(), iD);
		return mapWord.put(iD, value);
	}
	
	public T put(String key, T value) {
		int iD = mapIndex.size();
		value.setiD(iD);
		value.setIndex(this);
		mapIndex.put(key, iD);
		return mapWord.put(iD, value);
	}
	
	@Override
	public T put(Integer key, T value) {
		return put(value);
	}

	/*public T put(String key, T value, int iD) {
		value.setId(iD);
		mapIndex.put(key, iD);
		return mapWord.put(iD, value);
	}*/
	
	public Integer getKeyId(String key) {
		return mapIndex.get(key);
	}
	
	public T get(Integer key) {
		return mapWord.get(key);
	}
	
	@Override
	public T get(Object key) {
		if (key.getClass() == Integer.class)
			return get((Integer)key);
		if (key.getClass() != String.class)
			throw new IncompatibleClassChangeError("Key need to be a string when accessing word in the index.");
		return mapWord.get(mapIndex.get((String)key));
	}
	
	public boolean containsKey(String key) {
		return mapIndex.containsKey(key);
	}
	
	@Override
	public T remove(Object key) {
		System.out.println("Remove !!");
		return mapWord.remove(key);
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
	
	@Override
	public void clear() {
		mapIndex.clear();
		mapWord.clear();
		nbDoc = 0;
		corpusNbDoc.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return mapWord.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return mapWord.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<Integer, T>> entrySet() {
		return mapWord.entrySet();
	}

	@Override
	public boolean isEmpty() {
		return mapWord.isEmpty();
	}

	@Override
	public Set<Integer> keySet() {
		return mapWord.keySet();
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends T> m) {
		for (Integer i : m.keySet()) {
			mapWord.put(i, m.get(i));
			mapIndex.put(m.get(i).getWord(), i);
		}
	}

	@Override
	public int size() {
		return mapWord.size();
	}

	@Override
	public Collection<T> values() {
		return mapWord.values();
	}
}