package model.task.process.indexBuilder.ILP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.task.process.indexBuilder.AbstractIndexBuilder;
import model.task.process.indexBuilder.IndexBasedIn;
import model.task.process.indexBuilder.IndexBasedOut;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import optimize.SupportADNException;
import optimize.parameter.Parameter;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.NGram;
import textModeling.wordIndex.WordIndex;

/**
 * TODO remplacer les maps par Index de Ngrams
 * @author valnyz
 *
 */
public class BiGram_ILP extends AbstractIndexBuilder<NGram> implements IndexBasedIn<WordIndex>, SentenceNGramBasedOut {

	//private HashMap<NGram, Double> bigram_weights;
	private Map<SentenceModel, List<NGram>> ngrams_in_sentences;
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
	
	public BiGram_ILP(int id) throws SupportADNException {
		super(id);
		supportADN = new HashMap<String, Class<?>>();
		supportADN.put(BiGramILP_Parameter.fscFactor.getName(), Double.class);
		supportADN.put(BiGramILP_Parameter.minSenLength.getName(), Integer.class);

		ngrams_in_sentences = new HashMap<SentenceModel, List<NGram>>();
		
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
	}
	
	@Override
	public void finish() {
		super.finish();
		ngrams_in_sentences.clear();
	}
	
	private void buildWeightsAndSentences(List<Corpus> listCorpus)
	{		
		List<NGram> curr_bg_set;
		List<NGram> curr_doc_bg_set;

		System.out.println("Construction du modèle");
		for (Corpus corpus : listCorpus) {
			for (TextModel text : corpus) {
				curr_doc_bg_set = new ArrayList<NGram>();
				for (SentenceModel sen : text) {
					if (sen.getNbMot() >= minSenLength) {
						//On construit le set des bigrams dans la phrases
						curr_bg_set = generateBiGram(sen);
						if ( sen.getPosition() == 1 ) {
							for (NGram ng : curr_bg_set) {
								if (index.containsKey(ng.getWord()))
									index.get(ng.getWord()).setWeight(index.get(ng.getWord()).getWeight() + this.fscFactor);
							}
						}
						else
							curr_doc_bg_set.addAll(curr_bg_set);
						ngrams_in_sentences.put(sen, curr_bg_set);
					}
				}
				
				for (NGram ng : curr_doc_bg_set) {
					if (index.containsKey(ng.getWord()))
						index.get(ng.getWord()).setWeight(index.get(ng.getWord()).getWeight() + 1.);
				}
			}
			
			/*Iterator<SentenceModel> it = getCurrentProcess().getCorpusToSummarize().getAllSentence().iterator();
			while(it.hasNext()) {
				SentenceModel sen = it.next();
				if (sen.getNbMot() < minSenLength)
					it.remove();
			}*/
		}
	}
	
	private List<NGram> generateBiGram(SentenceModel sentence) {
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
					index.put(0, ng);
				ng.addDocumentOccurence(getCurrentProcess().getSummarizeCorpusId(), sentence.getText().getiD());
				listNGram.add(ng);
			}
		}
		return listNGram;
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

	@Override
	public Map<SentenceModel, List<NGram>> getSentenceNGramList() {
		return ngrams_in_sentences;
	}
}
