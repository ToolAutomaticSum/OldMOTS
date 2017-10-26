package liasd.asadera.model.task.process.indexBuilder.ILP;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import liasd.asadera.model.task.preProcess.GenerateTextModel;
import liasd.asadera.model.task.process.indexBuilder.AbstractIndexBuilder;
import liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import liasd.asadera.model.task.process.indexBuilder.IndexBasedOut;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.optimize.parameter.Parameter;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.TextModel;
import liasd.asadera.textModeling.WordModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.NGram;
import liasd.asadera.textModeling.wordIndex.WordIndex;
import liasd.asadera.tools.wordFilters.WordFilter;

/**
 * TODO remplacer les maps par Index de Ngrams
 * @author valnyz
 *
 */
public class BiGram_ILP extends AbstractIndexBuilder<NGram> implements IndexBasedIn<WordIndex>, SentenceNGramBasedOut {

	private static final Logger logger = Logger.getLogger("BiGram_ILP"); 
	
	//private HashMap<NGram, Double> bigram_weights;
	private Map<SentenceModel, Set<NGram>> ngrams_in_sentences;
	//private ArrayList<TreeSet<NGram>> bigrams_in_sentence;
	private Index<WordIndex> indexWord;
	//private TreeMap <NGram, Integer> bigrams_ids;
	
	private double fscFactor = 1;
	private int minSenLength = 1;
	
	public static enum BiGramILP_Parameter {
		fscFactor("fscFactor"),
		minSenLength("minSenLength");

		private String name;

		private BiGramILP_Parameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
	
	/**
	 * Classe permettant la construction de l'index des BiGrams. On compte un bigram par document maximum
	 * @param id
	 * @throws SupportADNException
	 */
	public BiGram_ILP(int id) throws SupportADNException {
		super(id);
		supportADN = new HashMap<String, Class<?>>();
		supportADN.put(BiGramILP_Parameter.fscFactor.getName(), Double.class);
		supportADN.put(BiGramILP_Parameter.minSenLength.getName(), Integer.class);

		ngrams_in_sentences = new HashMap<SentenceModel, Set<NGram>>();
		
		listParameterIn.add(new ParametrizedType(WordIndex.class, Index.class, IndexBasedIn.class));
		listParameterOut.add(new ParametrizedType(NGram.class, Index.class, IndexBasedOut.class));
		listParameterOut.add(new ParametrizedType(NGram.class, List.class, SentenceNGramBasedOut.class));
	}
	
	@Override
	public AbstractIndexBuilder<NGram> makeCopy() throws Exception {
		BiGram_ILP p = new BiGram_ILP(id);
		initCopy(p);
		return p;
	}
	
	@Override
	public void initADN() throws Exception {
		getCurrentProcess().getADN().putParameter(new Parameter<Double>(BiGramILP_Parameter.fscFactor.getName(), Double.parseDouble(getModel().getProcessOption(id, BiGramILP_Parameter.fscFactor.getName()))));
		getCurrentProcess().getADN().putParameter(new Parameter<Integer>(BiGramILP_Parameter.minSenLength.getName(), Integer.parseInt(getModel().getProcessOption(id, BiGramILP_Parameter.minSenLength.getName()))));
	}
	
	@Override
	public void processIndex(List<Corpus> listCorpus) {
		fscFactor = getCurrentProcess().getADN().getParameterValue(Double.class, BiGramILP_Parameter.fscFactor.getName());
		minSenLength = getCurrentProcess().getADN().getParameterValue(Integer.class, BiGramILP_Parameter.minSenLength.getName());

		buildWeightsAndSentences(listCorpus);
//		for (Corpus c : getCurrentMultiCorpus()) {
//			if (!listCorpus.contains(c)) {
//				Corpus temp = GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp", c, true);
//				buildWeightsAndSentences(Arrays.asList(temp));
//				if (!getModel().isMultiThreading())
//					temp.clear();
//			}
//		}
		/*Writer w = new Writer("indexNGram.txt");
		w.open();
		for (NGram ng : index.values()) {
			w.write(ng.toString() + " " + ng.getiD() + " " + ng.getTfCorpus(0) + "\n");
		}
		w.close();*/
	}
	
	@Override
	public void finish() {
		super.finish();
		ngrams_in_sentences.clear();
	}
	
	private void buildWeightsAndSentences(List<Corpus> listCorpus)
	{		
		Set<NGram> curr_bg_set;
		Set<NGram> curr_doc_bg_set;

		//System.out.println("Model building");
		for (Corpus corpus : listCorpus) {
			Set<NGram> firstSentencesConcepts;
			for (TextModel text : corpus) {
				firstSentencesConcepts = new TreeSet<NGram>();
				curr_doc_bg_set = new TreeSet<NGram>();
				for (SentenceModel sen : text)
					if (sen.getNbMot() >= minSenLength) {
						
						//On construit le set des bigrams dans la phrases
						curr_bg_set = getBiGrams(indexWord, sen, getCurrentProcess().getFilter());
						/*for (NGram ng : curr_bg_set)
							if (!index.containsKey(ng.getWord())) {
								index.put(ng);
								ng.setWeight(1.);
							}
							else
								index.get(ng.getWord()).setWeight(index.get(ng.getWord()).getWeight() + 1.);
						*/
						if (fscFactor != 0 && sen.getPosition() == 1 ) {
							firstSentencesConcepts.addAll(curr_bg_set);
							/*for (NGram ng : curr_bg_set) {
								if (!index.containsKey(ng.getWord())) {
									index.put(ng);
									ng.setWeight(fscFactor);
								}
								else
									index.get(ng.getWord()).setWeight(index.get(ng.getWord()).getWeight() + fscFactor);
							
							}*/
						}
						curr_doc_bg_set.addAll(curr_bg_set);
						ngrams_in_sentences.put(sen, curr_bg_set);
					}
				
				for (NGram ng : curr_doc_bg_set)
					if (index.containsKey(ng.getWord())) {
						index.get(ng.getWord()).setWeight(index.get(ng.getWord()).getWeight() + 1.);
						index.get(ng.getWord()).addDocumentOccurence(corpus.getiD(), text.getiD());
					}
					else {
						index.put(ng);
						//System.out.println(ng.getWord());
						ng.setIndex(index);
						ng.addDocumentOccurence(corpus.getiD(), text.getiD());
						ng.setWeight(1.);
					}
				
				for (NGram ng : firstSentencesConcepts)
					index.get(ng.getWord()).setWeight(index.get(ng.getWord()).getWeight() + fscFactor);
			}
			/*for (NGram ng : firstSentencesConcepts.keySet())
			{
				Integer i = this.bigrams.indexOf(ng);
				this.bigram_weights.set( i, this.bigram_weights.get(i) * (1.+this.fsc_factor) );
			}*/
		}
		//System.out.println(index.get(" | boeing | 747").getWeight());
		//System.out.println(index.get(" | boeing | 747").getTf());
	}
	
	private static Set<NGram> getBiGrams(Index<WordIndex> index, SentenceModel sen, WordFilter filter)
	{
		WordModel u1, u2;
		Set<NGram> ngrams_list = new TreeSet<NGram>();
		for (int i = 0; i < sen.size() - 1; i++)
		{
			u1 = sen.get(i);
			u2 = sen.get(i+1);
			
			if (filter.passFilter(u1) && filter.passFilter(u2))
			{
				NGram ng = new NGram();

				ng.add(index.get(u1.getmLemma()));
				ng.add(index.get(u2.getmLemma()));
				//if (! ngrams_list.contains(ng));
				ngrams_list.add(ng);
				//System.out.println("Pas Filtrée !");
			}			
		}
		sen.setNGrams(ngrams_list);
		return ngrams_list;
	}
	
	/*private List<NGram> generateBiGram(SentenceModel sentence) {
		List<NGram> listNGram = new ArrayList<NGram>();
		List<WordIndex> tempList;
		for (int i = 0; i < sentence.size() - 1; i++)
		{
			boolean cond = false;
			boolean stopWord = false; //Un stopWord par Ngram
			tempList = new ArrayList<WordIndex>();
			for (int j = i; j < i + 2; j++)
			{
				//System.out.println("j : "+j);
				WordModel u = sentence.get(j);

				if (!stopWord || !u.isStopWord()) {
					cond = true;
					WordIndex w = indexWord.get(u.getmLemma());
					if (w != null)
						tempList.add(w);
					else {
						System.out.println("BREAK!!!" + u.getmLemma());
						cond = false;
						break;
					}
					if (u.isStopWord())
						stopWord = true;
				} else
					cond = false;
			}
			if (cond) {
				NGram ng = new NGram(index);
				ng.addAll(tempList);
				if (index != null && index.containsKey(ng.getWord()))
					ng = index.get(ng.getWord());
				else if (index != null)
					index.put(ng);
				ng.addDocumentOccurence(getCurrentProcess().getSummarizeCorpusId(), sentence.getText().getiD());
				listNGram.add(ng);
			}
		}
		return listNGram;
	}*/
	
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

	@Override
	public Map<SentenceModel, Set<NGram>> getSentenceNGramList() {
		return ngrams_in_sentences;
	}
}
