package main.java.liasd.asadera.model.task.process.indexBuilder.TF_IDF;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import main.java.liasd.asadera.model.task.preProcess.GenerateTextModel;
import main.java.liasd.asadera.model.task.process.indexBuilder.AbstractIndexBuilder;
import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedOut;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.WordModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;
import main.java.liasd.asadera.tools.wordFilters.WordFilter;

public class TF_IDF extends AbstractIndexBuilder<WordIndex> {

	public TF_IDF(int id) throws SupportADNException {
		super(id);

		listParameterOut.add(new ParameterizedType(WordIndex.class, Index.class, IndexBasedOut.class));
	}

	@Override
	public AbstractIndexBuilder<WordIndex> makeCopy() throws Exception {
		TF_IDF p = new TF_IDF(id);
		initCopy(p);
		return p;
	}

	@Override
	public void processIndex(List<Corpus> listCorpus) throws Exception {
		super.processIndex(listCorpus);
		TF_IDF.generateDictionary(listCorpus, index, getCurrentProcess().getFilter());
		for (Corpus c : getCurrentMultiCorpus()) {
			if (!listCorpus.contains(c)) {
				boolean clear = c.size() == 0;
				Corpus temp = c;
				if (clear)
					temp = GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp", c,
							true);
				TF_IDF.majIDFDictionnary(temp, index, getCurrentProcess().getFilter());
				if (clear)
					temp.clear();
			}
		}
	}

	@SuppressWarnings("unlikely-arg-type")
	public static void generateDictionary(List<Corpus> listCorpus, Index<WordIndex> index, WordFilter filter) {
		for (Corpus corpus : listCorpus) {
			index.setNbDocument(index.getNbDocument() + corpus.size());
			for (TextModel textModel : corpus) {
				for (SentenceModel sentenceModel : textModel) {
					List<WordIndex> listWordIndex = new ArrayList<WordIndex>();
					for (WordModel word : sentenceModel.getListWordModel())
						if (filter.passFilter(word)) {
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
					sentenceModel.setN(1);
					sentenceModel.setListWordIndex(1, listWordIndex);
				}
			}
			index.putCorpusNbDoc(corpus.getiD(), corpus.size());
		}
	}

	/**
	 * @param corpus
	 * @param dictionnary
	 */
	public static void majIDFDictionnary(Corpus corpus, Index<WordIndex> index, WordFilter filter) {
		index.setNbDocument(index.getNbDocument() + corpus.getNbDocument());
		for (TextModel text : corpus) {
			for (SentenceModel sentenceModel : text) {
				for (WordModel word : sentenceModel.getListWordModel()) {
					if (filter.passFilter(word) && index.containsKey(word.getmLemma())) {
						@SuppressWarnings("unlikely-arg-type")
						WordIndex w = (WordIndex) index.get(word.getmLemma());
						w.addDocumentOccurence(corpus.getiD(), text.getiD());
					}
				}
			}
		}
		index.putCorpusNbDoc(corpus.getiD(), corpus.size());
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return super.isOutCompatible(compatibleMethod) || compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(WordIndex.class, Index.class, IndexBasedIn.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
		if (super.isOutCompatible(compMethod))
			super.setCompatibility(compMethod);
		else
			((IndexBasedIn<WordIndex>) compMethod).setIndex(index);
	}
}
