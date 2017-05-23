package model.task.process.indexBuilder.TF_IDF;

import java.io.File;

import model.task.preProcess.GenerateTextModel;
import model.task.process.indexBuilder.AbstractIndexBuilder;
import model.task.process.indexBuilder.IndexBasedIn;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import optimize.SupportADNException;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;

public class TF_IDF extends AbstractIndexBuilder<WordTF_IDF> {

	public TF_IDF(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public AbstractIndexBuilder<WordTF_IDF> makeCopy() throws Exception {
		TF_IDF p = new TF_IDF(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
	}

	@Override
	public void processIndex() {
		TF_IDF.generateDictionary(getCurrentProcess().getCorpusToSummarize(), index);
		for (Corpus c : getCurrentMultiCorpus()) {
			if (c!=getCurrentProcess().getCorpusToSummarize()) {
				Corpus temp = GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp", c, readStopWords);
				TF_IDF.majIDFDictionnary(temp, index);
				if (!getModel().isMultiThreading())
					temp.clear();
			}
		}
	}

	/**
	 * Construction du dictionnaire des mots des documents ({@see WordTF_IDF})
	 */
	public static void generateDictionary(Corpus corpus, Index<WordTF_IDF> dictionnary) {
		dictionnary.setNbDocument(dictionnary.getNbDocument()+corpus.size());
		//Construction du dictionnaire
		for (TextModel textModel : corpus) {
			for (SentenceModel sentenceModel : textModel) {
				for (WordModel word : sentenceModel) {
					//TODO ajouter filtre à la place de getmLemma
					if (!word.isStopWord()) {
						if(!dictionnary.containsKey(word.getmLemma())) {
							WordTF_IDF w = new WordTF_IDF(word.getmLemma(), dictionnary);
							w.addDocumentOccurence(corpus.getiD(), textModel.getiD());
							dictionnary.put(word.getmLemma(), w);
						}
						else {
							WordTF_IDF w = (WordTF_IDF) dictionnary.get(word.getmLemma());
							w.addDocumentOccurence(corpus.getiD(), textModel.getiD());
						}
						//dictionnary.get(word.getmLemma()).add(word); //Ajout au wordIndex des WordModel correspondant
					}
					//else if (!dictionnary.containsKey(word.getmLemma()))
						//dictionnary.put(word.getmLemma(), new WordIndex(word.getmLemma(), dictionnary));
				}
			}
		}
		dictionnary.putCorpusNbDoc(corpus.getiD(), corpus.size());
	}
	
	/**
	 * MAJ de l'index dictionnary avec les mots rencontrés dans Corpus corpus.
	 * @param corpus
	 * @param dictionnary
	 */
	public static void majIDFDictionnary(Corpus corpus, Index<WordTF_IDF> dictionnary) {
		dictionnary.setNbDocument(dictionnary.getNbDocument()+corpus.getNbDocument());			
		//Construction du dictionnaire
		for (TextModel text : corpus) {
			for (SentenceModel sentenceModel : text) {
				for (WordModel word : sentenceModel) {
					//TODO ajouter filtre à la place de getmLemma
					if (!word.isStopWord() && dictionnary.containsKey(word.getmLemma())) {
						WordTF_IDF w = (WordTF_IDF) dictionnary.get(word.getmLemma());
						w.addDocumentOccurence(corpus.getiD(), text.getiD());
						//dictionnary.get(word.getmLemma()).add(word); //Ajout au wordIndex des WordModel correspondant
					}
					//else
					//	dictionnary.put(word.getmLemma(), new WordIndex(word.getmLemma(), dictionnary));
				}
			}
		}
		dictionnary.putCorpusNbDoc(corpus.getiD(), corpus.size());
	}

	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(WordTF_IDF.class, Index.class, IndexBasedIn.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		((IndexBasedIn<WordTF_IDF>)compMethod).setIndex(index);
	}
}
