package liasd.asadera.model.task.process.scoringMethod.ILP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import liasd.asadera.model.task.process.indexBuilder.ILP.SentenceNGramBasedIn;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.model.task.process.scoringMethod.AbstractScoringMethod;
import liasd.asadera.model.task.process.scoringMethod.FileNameBasedIn;
import liasd.asadera.model.task.process.scoringMethod.FileNameBasedOut;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.NGram;

public class GenerateModel_ILP extends AbstractScoringMethod implements IndexBasedIn<NGram>, SentenceNGramBasedIn, FileNameBasedOut {

	private static int ilp_nb = 0;
	private final int ilp_id;
	
	
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
		ilp_id = ilp_nb;
		ilp_nb++;

		listParameterIn.add(new ParametrizedType(NGram.class, Index.class, IndexBasedIn.class));
		listParameterIn.add(new ParametrizedType(NGram.class, List.class, SentenceNGramBasedIn.class));
		listParameterOut.add(new ParametrizedType(null, String.class, FileNameBasedOut.class));
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
			File file = new File("tempILP" + ilp_id + ".ilp_out");
			file.delete();
			
			FileOutputStream fw = new FileOutputStream("tempILP" + ilp_id + ".ilp_out");
			OutputStreamWriter osr = new OutputStreamWriter (fw, "UTF-8");
			//BufferedWriter output = new BufferedWriter(fw);
			osr.write(model);
			osr.flush();
			osr.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(null, String.class, FileNameBasedIn.class));
	}

	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		((FileNameBasedIn)compMethod).setFileName(getFileName());
	}

	@Override
	public void setSentenceNGram(Map<SentenceModel, Set<NGram>> ngrams_in_sentences) {
		this.ngrams_in_sentences = ngrams_in_sentences;
	}

	@Override
	public void setIndex(Index<NGram> index) {
		this.index = index;
	}

	@Override
	public String getFileName() {
		return "tempILP" + ilp_id + ".ilp_out";
	}
}
