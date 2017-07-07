package model.task.process.indexBuilder;

import java.util.List;

import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import optimize.SupportADNException;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.WordIndex;

public class BasicIndexBuilder extends AbstractIndexBuilder<WordIndex> {

	public BasicIndexBuilder(int id) throws SupportADNException {
		super(id);
		
		listParameterOut.add(new ParametrizedType(WordIndex.class, Index.class, IndexBasedOut.class));
	}

	@Override
	public AbstractIndexBuilder<WordIndex> makeCopy() throws Exception {
		BasicIndexBuilder p = new BasicIndexBuilder(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
	}

	@Override
	public void processIndex(List<Corpus> listCorpus) {
		for (Corpus corpus : listCorpus) {
			index.setNbDocument(index.getNbDocument() + corpus.size());
			//Construction du dictionnaire
			for (TextModel textModel : corpus) {
				for (SentenceModel sentenceModel : textModel) {
					for (WordModel word : sentenceModel) {
						if (readStopWords || !word.isStopWord()) {
							if(!index.containsKey(word.getmLemma())) {
								WordIndex w = new WordIndex(word.getmLemma(), index);
								w.addDocumentOccurence(corpus.getiD(), textModel.getiD());
								index.put(word.getmLemma(), w);
							}
							else {
								WordIndex w = index.get(word.getmLemma());
								w.addDocumentOccurence(corpus.getiD(), textModel.getiD());
							}
						}
					}
				}
			}
			index.putCorpusNbDoc(corpus.getiD(), corpus.size());
		}
	}

	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(WordIndex.class, Index.class, IndexBasedIn.class));
	}
	
	/**
	 * donne le/les paramètre(s) d'output en input à la class comp méthode
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		((IndexBasedIn<WordIndex>)compMethod).setIndex(index);
	}
}
