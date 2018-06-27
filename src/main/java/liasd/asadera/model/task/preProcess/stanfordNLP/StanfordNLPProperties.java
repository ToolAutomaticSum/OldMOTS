package main.java.liasd.asadera.model.task.preProcess.stanfordNLP;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class StanfordNLPProperties extends Properties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String language = "";
	
	public static final Map<String, String> languageAbbr = new HashMap<>();
	public static final Map<String, String> languagePosModel = new HashMap<>();

	static {
		languageAbbr.put("english", "en");
		languagePosModel.put("english", "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
		
		languageAbbr.put("french", "fr");
		languagePosModel.put("french", "edu/stanford/nlp/models/pos-tagger/french/french.tagger");
	}
	
	public StanfordNLPProperties(String language) {
		super();
		this.language = language;
	}
	
	@Override
	public synchronized Object put(Object key, Object value) {
		if (key.getClass() == String.class) {
			String propName = (String) key;
			if (propName.equals("tokenize.language"))
				return super.put(propName, languageAbbr.get(language));
			else if (propName.equals("pos.model"))
				return super.put(propName, languagePosModel.get(language));
			else
				return super.put(key, value);
		}
		else
			return super.put(key, value);
	}
}
