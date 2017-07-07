package model.task.process.scoringMethod.ILP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.task.process.indexBuilder.IndexBasedIn;
import model.task.process.indexBuilder.ILP.SentenceNGramBasedIn;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import model.task.process.scoringMethod.AbstractScoringMethod;
import optimize.SupportADNException;
import textModeling.Corpus;
import textModeling.SentenceModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.NGram;

public class GenerateModel_ILP extends AbstractScoringMethod implements IndexBasedIn<NGram>, SentenceNGramBasedIn {
	
	private Index<NGram> index;
	//private List<TreeSet<NGram>> bigrams_in_sentence;
	private Map<SentenceModel, Set<NGram>> ngrams_in_sentences;
	
	/**
	 * Modèle des BiGram construit dans computeScores
	 */
	private String model;
	
	private Integer maxSummLength;
	
	public GenerateModel_ILP(int id) throws SupportADNException {
		super(id);

		listParameterIn.add(new ParametrizedType(NGram.class, Index.class, IndexBasedIn.class));
		listParameterIn.add(new ParametrizedType(NGram.class, List.class, SentenceNGramBasedIn.class));
	}
	
	@Override
	public AbstractScoringMethod makeCopy() throws Exception {
		GenerateModel_ILP p = new GenerateModel_ILP(id);
		initCopy(p);
		return p;
	}
	
	@Override
	public void initADN() throws Exception {
	}

	@Override
	public void computeScores(List<Corpus> listCorpus) throws Exception {
		maxSummLength = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "SummarySize"));
		
		buildModel(listCorpus);
		writeModelToTmpFile();
	}
	
	public void buildModel(List<Corpus> listCorpus)
	{
		List<SentenceModel> listSentence = new ArrayList<SentenceModel>();
		//for (Corpus c : listCorpus)
		listSentence.addAll(ngrams_in_sentences.keySet());
		Collections.sort(listSentence);
		//HashMap<NGram, Double> bigram_weights = new HashMap <NGram, Double>();
		//ArrayList<TreeSet<NGram>> bigrams_in_sentence = new ArrayList <TreeSet <NGram>> ();
		//TreeMap<NGram, Integer> bigrams_ids = new TreeMap <NGram, Integer>();
		
		/*for(NGram ng : index.values()) {
			bigram_weights.put(ng, ng.getWeight());
			bigrams_ids.put(ng, ng.getiD());
		}*/
		//for(SentenceModel sen : listSentence)
		//	bigrams_in_sentence.add(new TreeSet<NGram>(ngrams_in_sentences.get(sen)));
		
		String texte = "Maximize\n";
		String objective = "score: ";
		for (NGram ng : index.values()) {
			double bg_weight = ng.getWeight();
			int id_bg = ng.getiD();
			/*if ( bg_weight < 3 )
				objective += "+ "+0.0+" c"+id_bg+" ";
			else*/
				objective += "+ "+bg_weight+" c"+id_bg+" ";
		}
		int i = 0;
		for(SentenceModel sen : listSentence) {
			/*Phrase p = ss.getSentences().get(i);
			if (p.getNbWords() < 10)
				continue;*/
			double length = sen.getNbMot() / 1000.;
			objective += "- "+length+" s"+i+" ";
			i++;
		}
		texte+=objective+"\n\nSubject To\n";
		boolean first;
		for (NGram ng : index.values()) {
			first = true;
			String contrainte = "index_"+ng.getiD()+": ";
			int j = 0;
			for (SentenceModel sen : listSentence) {
				//Phrase p = ss.getSentences().get(j);
				//if (sen.getNbMot() < 10)
					//continue;
				//TreeSet<NGram> curr_set = bigrams_in_sentence.get(j);
				for (NGram ng1 : ngrams_in_sentences.get(sen)) {
					if (ng1.equals(ng)) {
						if (first) {
							contrainte += "s"+j+" ";
							first = false;
						}
						else
							contrainte += "+ "+"s"+j+" ";
					}
				}
				j++;
			}
			contrainte += "- c"+ng.getiD()+" >= 0\n";
			texte += contrainte;
		}
		String length_constraint = "length: ";
		i = 0;
		for (SentenceModel sen : listSentence) {
			/*if (p.getNbWords() < 10)
				continue;*/
			//System.out.println("Phrase "+i);
			int length = sen.getNbMot();
			if ( i == 0 )
				length_constraint += length+" s"+i;
			else
				length_constraint += " + "+length+" s"+i;
			i++;
		}
		length_constraint += " <= "+maxSummLength+"\n";
		
		texte += length_constraint+"\n\nBinary\n";
		
		for (NGram ng: index.values())
			//if (e.getValue() > 2.9)
				texte+= "c"+ng.getiD()+"\n";
		
		for (int j = 0; j < listSentence.size(); j++)
		{
			texte += "s"+j+"\n";
		}
		texte += "End";
		
		//System.out.println(texte);
		model = texte;
	}
	
	private void writeModelToTmpFile()
	{
		try{
			File file = new File("tempILP.ilp");
			if(!file.delete())
				System.err.println(file.getName() + " not deleted!");
			
			FileOutputStream fw = new FileOutputStream("tempILP.ilp");
			OutputStreamWriter osr = new OutputStreamWriter (fw, "UTF-8");
			//BufferedWriter output = new BufferedWriter(fw);
			osr.write(model);
			osr.flush();
			osr.close();
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return false;
	}

	@Override
	public void setSentenceNGram(Map<SentenceModel, Set<NGram>> ngrams_in_sentences) {
		this.ngrams_in_sentences = ngrams_in_sentences;
	}

	@Override
	public void setIndex(Index<NGram> index) {
		this.index = index;
	}
}
