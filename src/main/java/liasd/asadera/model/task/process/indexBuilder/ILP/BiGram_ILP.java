package main.java.liasd.asadera.model.task.process.indexBuilder.ILP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import main.java.liasd.asadera.model.task.process.indexBuilder.AbstractIndexBuilder;
import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedOut;
import main.java.liasd.asadera.model.task.process.indexBuilder.TF_IDF.NGram_IDF;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.optimize.parameter.Parameter;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.NGram;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class BiGram_ILP extends AbstractIndexBuilder<NGram> implements IndexBasedIn<WordIndex>, SentenceNGramBasedOut {

	private Map<SentenceModel, Set<NGram>> ngrams_in_sentences;

	private Index<WordIndex> indexWord;

	private double fscFactor = 1;
	private int minSenLength = 1;

	public static enum BiGramILP_Parameter {
		fscFactor("fscFactor"), minSenLength("minSenLength");

		private String name;

		private BiGramILP_Parameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * 
	 * @param id
	 * @throws SupportADNException
	 */
	public BiGram_ILP(int id) throws SupportADNException {
		super(id);
		
		supportADN.put(BiGramILP_Parameter.fscFactor.getName(), Double.class);
		supportADN.put(BiGramILP_Parameter.minSenLength.getName(), Integer.class);

		listParameterIn.add(new ParameterizedType(WordIndex.class, Index.class, IndexBasedIn.class));
		listParameterOut.add(new ParameterizedType(NGram.class, Index.class, IndexBasedOut.class));
		listParameterOut.add(new ParameterizedType(NGram.class, List.class, SentenceNGramBasedOut.class));
	}

	@Override
	public AbstractIndexBuilder<NGram> makeCopy() throws Exception {
		BiGram_ILP p = new BiGram_ILP(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		ngrams_in_sentences = new HashMap<SentenceModel, Set<NGram>>();
		
		getCurrentProcess().getADN().putParameter(new Parameter<Double>(BiGramILP_Parameter.fscFactor.getName(),
				Double.parseDouble(getModel().getProcessOption(id, BiGramILP_Parameter.fscFactor.getName()))));
		getCurrentProcess().getADN().putParameter(new Parameter<Integer>(BiGramILP_Parameter.minSenLength.getName(),
				Integer.parseInt(getModel().getProcessOption(id, BiGramILP_Parameter.minSenLength.getName()))));
	}

	@Override
	public void processIndex(List<Corpus> listCorpus) throws Exception {
		super.processIndex(listCorpus);
		fscFactor = getCurrentProcess().getADN().getParameterValue(Double.class,
				BiGramILP_Parameter.fscFactor.getName());
		minSenLength = getCurrentProcess().getADN().getParameterValue(Integer.class,
				BiGramILP_Parameter.minSenLength.getName());

		buildWeightsAndSentences(listCorpus);
	}

	@Override
	public void finish() {
		super.finish();
		ngrams_in_sentences.clear();
	}

	@SuppressWarnings("unlikely-arg-type")
	private void buildWeightsAndSentences(List<Corpus> listCorpus) {
		Set<NGram> curr_bg_set;
		Set<NGram> curr_doc_bg_set;

		for (Corpus corpus : listCorpus) {
			Set<NGram> firstSentencesConcepts;
			for (TextModel text : corpus) {
				firstSentencesConcepts = new TreeSet<NGram>();
				curr_doc_bg_set = new TreeSet<NGram>();
				for (SentenceModel sen : text)
					if (sen.getNbMot() >= minSenLength) {
						listSen.add(sen);
						curr_bg_set = NGram_IDF.getBiGrams(indexWord, sen, getCurrentProcess().getFilter());

						if (fscFactor != 0 && sen.getPosition() == 1)
							firstSentencesConcepts.addAll(curr_bg_set);
						curr_doc_bg_set.addAll(curr_bg_set);
						sen.setN(2);
						sen.setListWordIndex(2, curr_bg_set);
						ngrams_in_sentences.put(sen, curr_bg_set);
					}

				for (NGram ng : curr_doc_bg_set)
					if (index.containsKey(ng.getWord())) {
						index.get(ng.getWord()).setWeight(index.get(ng.getWord()).getWeight() + 1.);
						index.get(ng.getWord()).addDocumentOccurence(corpus.getiD(), text.getiD());
					} else {
						index.put(ng);
						ng.addDocumentOccurence(corpus.getiD(), text.getiD());
						ng.setWeight(1.);
					}

				for (NGram ng : firstSentencesConcepts)
					index.get(ng.getWord()).setWeight(index.get(ng.getWord()).getWeight() + fscFactor);
			}
		}
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(NGram.class, Index.class, IndexBasedIn.class))
				|| compatibleMethod.getParameterTypeIn()
						.contains(new ParameterizedType(NGram.class, List.class, SentenceNGramBasedIn.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
		if (compMethod.getParameterTypeIn()
				.contains(new ParameterizedType(NGram.class, Index.class, IndexBasedIn.class)))
			((IndexBasedIn<NGram>) compMethod).setIndex(index);
		if (compMethod.getParameterTypeIn()
				.contains(new ParameterizedType(NGram.class, List.class, SentenceNGramBasedIn.class)))
			((SentenceNGramBasedIn) compMethod).setSentenceNGram(ngrams_in_sentences);
	}

	@Override
	public void setIndex(Index<WordIndex> index) {
		this.indexWord = index;
	}

	@Override
	public Map<SentenceModel, Set<NGram>> getSentenceNGramList() {
		return ngrams_in_sentences;
	}
}
