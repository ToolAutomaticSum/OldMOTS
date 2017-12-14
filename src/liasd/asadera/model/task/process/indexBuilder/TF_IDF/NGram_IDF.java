package liasd.asadera.model.task.process.indexBuilder.TF_IDF;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omg.DynamicAny.DynAnyPackage.InvalidValue;

import liasd.asadera.model.task.preProcess.GenerateTextModel;
import liasd.asadera.model.task.process.indexBuilder.AbstractIndexBuilder;
import liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import liasd.asadera.model.task.process.indexBuilder.IndexBasedOut;
import liasd.asadera.model.task.process.indexBuilder.ILP.SentenceNGramBasedIn;
import liasd.asadera.model.task.process.indexBuilder.ILP.SentenceNGramBasedOut;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.TextModel;
import liasd.asadera.textModeling.WordModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.NGram;
import liasd.asadera.textModeling.wordIndex.WordIndex;
import liasd.asadera.tools.wordFilters.WordFilter;

public class NGram_IDF extends AbstractIndexBuilder<NGram> implements IndexBasedIn<WordIndex> {

	private Map<SentenceModel, Set<NGram>> ngrams_in_sentences;
	
	private Index<WordIndex> indexWord;
	
	private int n;
	
	public NGram_IDF(int id) throws SupportADNException {
		super(id);

		ngrams_in_sentences = new HashMap<SentenceModel, Set<NGram>>();
		
		listParameterIn.add(new ParametrizedType(WordIndex.class, Index.class, IndexBasedIn.class));
		listParameterOut.add(new ParametrizedType(NGram.class, Index.class, IndexBasedOut.class));
		listParameterOut.add(new ParametrizedType(NGram.class, List.class, SentenceNGramBasedOut.class));
	}

	@Override
	public AbstractIndexBuilder<NGram> makeCopy() throws Exception {
		NGram_IDF p = new NGram_IDF(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		n = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "n"));
		if (n <= 1)
			throw new InvalidValue("N need to be >1 for NGram_IDF.");
	}

	@Override
	public void processIndex(List<Corpus> listCorpus) throws Exception {
		super.processIndex(listCorpus);
		NGram_IDF.generateIndex(n, listCorpus, ngrams_in_sentences, index, indexWord, getCurrentProcess().getFilter());
		for (Corpus c : getCurrentMultiCorpus()) {
			if (!listCorpus.contains(c)) {
				Corpus temp = GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp", c, true);
				NGram_IDF.majIDFIndex(n, temp, index, indexWord, getCurrentProcess().getFilter());
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
	public static void generateIndex(int n, List<Corpus> listCorpus, Map<SentenceModel, Set<NGram>> ngrams_in_sentences, Index<NGram> index, Index<WordIndex> indexWord, WordFilter filter) {
		for (Corpus corpus : listCorpus) {
			index.setNbDocument(index.getNbDocument()+corpus.size());
			//Construction du dictionnaire
			for (TextModel textModel : corpus)
				for (SentenceModel sen : textModel) {
					Set<NGram> senNG;
					if (n==2)
						senNG = NGram_IDF.getBiGrams(indexWord, sen, filter);
					else
						senNG = NGram_IDF.getNGrams(n, indexWord, sen, filter);
					Set<NGram> indexedSenNG = new LinkedHashSet<NGram>();
					for (NGram ng : senNG) {
						if (!index.containsKey(ng.getWord()))
							index.put(ng);
						else
							ng = index.get(ng.getWord());
						ng.addDocumentOccurence(corpus.getiD(), textModel.getiD());
						indexedSenNG.add(ng);
					}
					sen.setN(n);
					sen.setListWordIndex(n, indexedSenNG);//setNGrams(indexedSenNG);
					ngrams_in_sentences.put(sen, indexedSenNG);
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
	public static void majIDFIndex(int n, Corpus corpus, Index<NGram> index, Index<WordIndex> indexWord, WordFilter filter) {
		index.setNbDocument(index.getNbDocument()+corpus.size());
		//Construction du dictionnaire
		for (TextModel textModel : corpus) {
			for (SentenceModel sen : textModel) {
				Set<NGram> senNG;
				if (n==2)
					senNG = NGram_IDF.getBiGrams(indexWord, sen, filter);
				else
					senNG = NGram_IDF.getNGrams(n, indexWord, sen, filter);
				for (NGram ng : senNG) {
					if (index.containsKey(ng.getWord()))
						index.get(ng.getWord()).addDocumentOccurence(corpus.getiD(), textModel.getiD());
				}
			}
		}
		index.putCorpusNbDoc(corpus.getiD(), corpus.size());
	}

	@SuppressWarnings("unlikely-arg-type")
	public static Set<NGram> getBiGrams(Index<WordIndex> index, SentenceModel sen, WordFilter filter) {
		WordModel u1, u2;
		Set<NGram> ngrams_list = new LinkedHashSet<NGram>();
		for (int i = 0; i < sen.getListWordModel().size() - 1; i++) {
			u1 = sen.getListWordModel().get(i);
			u2 = sen.getListWordModel().get(i+1);
			
			if (filter.passFilter(u1) && filter.passFilter(u2) && index.containsKey(u1.getmLemma()) && index.containsKey(u2.getmLemma())) {
				NGram ng = new NGram();
				ng.add(index.get(u1.getmLemma()));
				ng.add(index.get(u2.getmLemma()));
				ngrams_list.add(ng);
			}			
		}
		sen.setListWordIndex(2, ngrams_list);
		return ngrams_list;
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public static Set<NGram> getNGrams(int n, Index<WordIndex> index, SentenceModel sen, WordFilter filter) {
		Set<NGram> ngrams_list = new LinkedHashSet<NGram>();
		WordModel u;
		for (int i = 0; i < sen.getListWordModel().size() - n + 1; i++) {
			boolean cond = false;
			boolean filtered = false; //Un stopWord par Ngram
			int nbFiltered = 0;
			NGram ng = new NGram();

			for (int j = i; j < i + n; j++)	{
				//System.out.println("j : "+j);
				u = sen.getListWordModel().get(j);
				
				if (index.containsKey(u.getmLemma()) && (!filtered || (filtered && !filter.passFilter(u)))) {
					cond = true;
					WordIndex w = index.get(u.getmLemma());
					if (w != null)
						ng.add(w);
					else {
						System.out.println("BREAK!!! " + u.getmLemma());
						cond = false;
						break;
					}
					if (!filter.passFilter(u))
						nbFiltered++;
					if (nbFiltered==n-1)
						filtered = true;
				} else
					cond = false;
			}
			if (cond)
				ngrams_list.add(ng);
		}
		return ngrams_list;
	}
	
	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return  super.isOutCompatible(compatibleMethod)
				|| compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(NGram.class, Index.class, IndexBasedIn.class))
				|| compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(NGram.class, List.class, SentenceNGramBasedIn.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		if (super.isOutCompatible(compMethod))
			super.setCompatibility(compMethod);
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
