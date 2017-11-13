package liasd.asadera.model.task.process.indexBuilder.TF_IDF;

import java.io.File;
import java.util.List;

import liasd.asadera.model.task.preProcess.GenerateTextModel;
import liasd.asadera.model.task.process.indexBuilder.AbstractIndexBuilder;
import liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import liasd.asadera.model.task.process.indexBuilder.IndexBasedOut;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.TextModel;
import liasd.asadera.textModeling.WordModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.WordIndex;
import liasd.asadera.tools.wordFilters.WordFilter;

public class TF_IDF extends AbstractIndexBuilder<WordIndex> {

	public TF_IDF(int id) throws SupportADNException {
		super(id);
		
		listParameterOut.add(new ParametrizedType(WordIndex.class, Index.class, IndexBasedOut.class));
	}

	@Override
	public AbstractIndexBuilder<WordIndex> makeCopy() throws Exception {
		TF_IDF p = new TF_IDF(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
	}

	@Override
	public void processIndex(List<Corpus> listCorpus) {
		TF_IDF.generateDictionary(listCorpus, index, getCurrentProcess().getFilter());
		for (Corpus c : getCurrentMultiCorpus()) {
			if (!listCorpus.contains(c)) {
				Corpus temp = GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp", c, true);
				TF_IDF.majIDFDictionnary(temp, index, getCurrentProcess().getFilter());
				if (!getModel().isMultiThreading())
					temp.clear();
			}
		}

//		List<Pair<WordTF_IDF, Double>> listWord = new ArrayList<Pair<WordTF_IDF, Double>>();
//		Writer w = new Writer("indexTF_IDF.txt");
//		w.open();
//		for (WordTF_IDF word : index.values())
//			listWord.add(new Pair<WordTF_IDF, Double>(word,word.getIdf()));
//		Collections.sort(listWord);
//		for (Pair<WordTF_IDF, Double> p: listWord)
//			w.write(p.getKey().getWord() + "\t" + p.getValue() + "\n");
	}

	/**
	 * Construction du dictionnaire des mots des documents ({@see WordTF_IDF})
	 */
	public static void generateDictionary(List<Corpus> listCorpus, Index<WordIndex> dictionnary, WordFilter filter) {
		for (Corpus corpus : listCorpus) {
			dictionnary.setNbDocument(dictionnary.getNbDocument()+corpus.size());
			//Construction du dictionnaire
			for (TextModel textModel : corpus) {
				for (SentenceModel sentenceModel : textModel) {
					for (WordModel word : sentenceModel) {
						//TODO ajouter filtre à la place de getmLemma
						if (filter.passFilter(word)) {
							if(!dictionnary.containsKey(word.getmLemma())) {
								WordIndex w = new WordIndex(word.getmLemma(), dictionnary);
								w.addDocumentOccurence(corpus.getiD(), textModel.getiD());
								dictionnary.put(word.getmLemma(), w);
							}
							else {
								@SuppressWarnings("unlikely-arg-type")
								WordIndex w = dictionnary.get(word.getmLemma());
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
	}
	
	/**
	 * MAJ de l'index dictionnary avec les mots rencontrés dans Corpus corpus.
	 * @param corpus
	 * @param dictionnary
	 */
	public static void majIDFDictionnary(Corpus corpus, Index<WordIndex> dictionnary, WordFilter filter) {
		dictionnary.setNbDocument(dictionnary.getNbDocument()+corpus.getNbDocument());			
		//Construction du dictionnaire
		for (TextModel text : corpus) {
			for (SentenceModel sentenceModel : text) {
				for (WordModel word : sentenceModel) {
					//TODO ajouter filtre à la place de getmLemma
					if (filter.passFilter(word) && dictionnary.containsKey(word.getmLemma())) {
						@SuppressWarnings("unlikely-arg-type")
						WordIndex w = (WordIndex) dictionnary.get(word.getmLemma());
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
		return compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(WordIndex.class, Index.class, IndexBasedIn.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		((IndexBasedIn<WordIndex>)compMethod).setIndex(index);
	}
}
