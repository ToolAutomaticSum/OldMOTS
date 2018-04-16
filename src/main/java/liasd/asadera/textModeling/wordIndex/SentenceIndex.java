package main.java.liasd.asadera.textModeling.wordIndex;

import java.util.List;
import java.util.Map;

import javax.management.InvalidAttributeValueException;

public class SentenceIndex {

	protected List<WordIndex> listWordIndex;
	protected Map<Integer, List<WordIndex>> mapNGram;

	public List<WordIndex> getListWordIndex(int n) throws InvalidAttributeValueException {
		if (n <= 0)
			throw new InvalidAttributeValueException();
		else if (n == 1)
			return listWordIndex;
		else
			return mapNGram.get(n);
	}
}
