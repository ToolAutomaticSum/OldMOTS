package main.java.liasd.asadera.model.task.process.indexBuilder;

import java.util.ArrayList;
import java.util.List;

import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.WordModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class BasicIndexBuilder extends AbstractIndexBuilder<WordIndex> {

	public BasicIndexBuilder(int id) throws SupportADNException {
		super(id);

		listParameterOut.add(new ParameterizedType(WordIndex.class, Index.class, IndexBasedOut.class));
	}

	@Override
	public AbstractIndexBuilder<WordIndex> makeCopy() throws Exception {
		BasicIndexBuilder p = new BasicIndexBuilder(id);
		initCopy(p);
		return p;
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void processIndex(List<Corpus> listCorpus) throws Exception {
		super.processIndex(listCorpus);
		for (Corpus corpus : listCorpus) {
			index.setNbDocument(index.getNbDocument() + corpus.size());
			for (TextModel textModel : corpus) {
				for (SentenceModel sentenceModel : textModel) {
					List<WordIndex> listWordIndex = new ArrayList<WordIndex>();
					for (WordModel word : sentenceModel.getListWordModel()) {
						if (getCurrentProcess().getFilter().passFilter(word)) {
							WordIndex w;
							if (!index.containsKey(word.getmLemma())) {
								w = new WordIndex(word.getmLemma());
								w.addDocumentOccurence(corpus.getiD(), textModel.getiD());
								index.put(word.getmLemma(), w);
							} else {
								w = index.get(word.getmLemma());
								w.addDocumentOccurence(corpus.getiD(), textModel.getiD());
							}
							listWordIndex.add(w);
						}
					}
					sentenceModel.setN(1);
					sentenceModel.setListWordIndex(1, listWordIndex);
				}
			}
			index.putCorpusNbDoc(corpus.getiD(), corpus.size());
		}
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(WordIndex.class, Index.class, IndexBasedIn.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
		((IndexBasedIn<WordIndex>) compMethod).setIndex(index);
	}
}
