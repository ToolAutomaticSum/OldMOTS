package main.java.liasd.asadera.exception;

public class UnknownLanguage extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3442532275047183130L;

	public UnknownLanguage() {
		super("Unknown language. If you want to use MOTS with this language, please add the StanfordNLP model to the pom.xml and add it in java.liasd.asadera.model.task.preProcess.stanfordNLP.StanfordNLPProperties.");
	}

	public UnknownLanguage(String language) {
		super("Unknown language " + language + ". If you want to use MOTS with this language, please add the StanfordNLP model to the pom.xml and add it in java.liasd.asadera.model.task.preProcess.stanfordNLP.StanfordNLPProperties.");
	}

}
