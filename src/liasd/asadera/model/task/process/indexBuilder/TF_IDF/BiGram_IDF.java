package liasd.asadera.model.task.process.indexBuilder.TF_IDF;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import liasd.asadera.model.task.preProcess.GenerateTextModel;
import liasd.asadera.model.task.process.indexBuilder.AbstractIndexBuilder;
import liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import liasd.asadera.model.task.process.indexBuilder.IndexBasedOut;
import liasd.asadera.model.task.process.indexBuilder.ILP.BiGram_ILP;
import liasd.asadera.model.task.process.indexBuilder.ILP.SentenceNGramBasedIn;
import liasd.asadera.model.task.process.indexBuilder.ILP.SentenceNGramBasedOut;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.TextModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.NGram;
import liasd.asadera.textModeling.wordIndex.WordIndex;
import liasd.asadera.tools.wordFilters.WordFilter;

public class BiGram_IDF extends AbstractIndexBuilder<NGram> implements IndexBasedIn<WordIndex>{

	private Map<SentenceModel, Set<NGram>> ngrams_in_sentences;
	
	private Index<WordIndex> indexWord;
	
	public BiGram_IDF(int id) throws SupportADNException {
		super(id);

		ngrams_in_sentences = new HashMap<SentenceModel, Set<NGram>>();
		
		listParameterIn.add(new ParametrizedType(WordIndex.class, Index.class, IndexBasedIn.class));
		listParameterOut.add(new ParametrizedType(NGram.class, Index.class, IndexBasedOut.class));
		listParameterOut.add(new ParametrizedType(NGram.class, List.class, SentenceNGramBasedOut.class));
	}

	@Override
	public AbstractIndexBuilder<NGram> makeCopy() throws Exception {
		BiGram_IDF p = new BiGram_IDF(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
	}

	@Override
	public void processIndex(List<Corpus> listCorpus) throws Exception {
		BiGram_IDF.generateDictionary(listCorpus, ngrams_in_sentences, index, indexWord, getCurrentProcess().getFilter());
		for (Corpus c : getCurrentMultiCorpus()) {
			if (!listCorpus.contains(c)) {
				Corpus temp = GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp", c, true);
				BiGram_IDF.majIDFDictionnary(temp, index, indexWord, getCurrentProcess().getFilter());
				if (!getModel().isMultiThreading())
					temp.clear();
			}
		}
	}
	
	@Override
	public void finish() {
		super.finish();
		ngrams_in_sentences.clear();
	}
	
	/**
	 * Construction du dictionnaire des mots des documents ({@see WordTF_IDF})
	 */
	@SuppressWarnings("unlikely-arg-type")
	public static void generateDictionary(List<Corpus> listCorpus, Map<SentenceModel, Set<NGram>> ngrams_in_sentences, Index<NGram> index, Index<WordIndex> indexWord, WordFilter filter) {
		for (Corpus corpus : listCorpus) {
			index.setNbDocument(index.getNbDocument()+corpus.size());
			//Construction du dictionnaire
			for (TextModel textModel : corpus) {
				for (SentenceModel sen : textModel) {
					Set<NGram> senNG = BiGram_ILP.getBiGrams(indexWord, sen, filter);
					Set<NGram> indexedSenNG = new TreeSet<NGram>();
					for (NGram ng : senNG) {
						ng.setIndex(index);
						if (!index.containsKey(ng.getWord()))
							index.put(ng);
						else
							ng = index.get(ng.getWord());
						ng.addDocumentOccurence(corpus.getiD(), textModel.getiD());
						indexedSenNG.add(ng);
					}
					sen.setNGrams(indexedSenNG);
					ngrams_in_sentences.put(sen, indexedSenNG);
				}
			}
			index.putCorpusNbDoc(corpus.getiD(), corpus.size());
		}
	}
	
	/**
	 * MAJ de l'index dictionnary avec les mots rencontrés dans Corpus corpus.
	 * @param corpus
	 * @param dictionnary
	 */
	@SuppressWarnings("unlikely-arg-type")
	public static void majIDFDictionnary(Corpus corpus, Index<NGram> index, Index<WordIndex> indexWord, WordFilter filter) {
		index.setNbDocument(index.getNbDocument()+corpus.size());
		//Construction du dictionnaire
		for (TextModel textModel : corpus) {
			for (SentenceModel sen : textModel) {
				Set<NGram> senNG = BiGram_ILP.getBiGrams(indexWord, sen, filter);
				for (NGram ng : senNG) {
					if (index.containsKey(ng.getWord()))
						index.get(ng.getWord()).addDocumentOccurence(corpus.getiD(), textModel.getiD());
				}
			}
		}
		index.putCorpusNbDoc(corpus.getiD(), corpus.size());
	}
	
	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(NGram.class, Index.class, IndexBasedIn.class))
				|| compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(NGram.class, List.class, SentenceNGramBasedIn.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		if (compMethod.getParameterTypeIn().contains(new ParametrizedType(NGram.class, Index.class, IndexBasedIn.class)))
			((IndexBasedIn<NGram>)compMethod).setIndex(index);
		if(compMethod.getParameterTypeIn().contains(new ParametrizedType(NGram.class, List.class, SentenceNGramBasedIn.class)))
			((SentenceNGramBasedIn)compMethod).setSentenceNGram(ngrams_in_sentences);
	}

	@Override
	public void setIndex(Index<WordIndex> index) {
		this.indexWord = index;
	}

}
