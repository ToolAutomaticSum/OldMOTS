package model.task.process.scoringMethod.ILP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import model.task.process.ILP.BiGram_ILP.BiGramILP_Parameter;
import model.task.process.scoringMethod.AbstractScoringMethod;
import optimize.SupportADNException;
import textModeling.SentenceModel;
import textModeling.wordIndex.NGram;

public class GenerateModel_ILP extends AbstractScoringMethod implements BiGramListBasedIn, FileModelBasedOut {

	/**
	 * BiGramListBasedIn
	 */
	private HashMap<NGram, Double> bigram_weights;
	/**
	 * BiGramListBasedIn
	 */
	private ArrayList<TreeSet<NGram>> bigrams_in_sentence;
	/**
	 * BiGramListBasedIn
	 */
	private ArrayList<NGram> bigrams; 
	//Give an id to each NGram
	private TreeMap <NGram, Integer> bigrams_ids;
	
	/**
	 * Modèle des BiGram construit dans computeScores
	 */
	private String model;
	
	private Integer maxSummLength;
	private String tmpFile;
	
	public GenerateModel_ILP(int id) throws SupportADNException {
		super(id);
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
	public void computeScores() throws Exception {
		maxSummLength = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "SummarySize"));
		tmpFile = getCurrentProcess().getModel().getProcessOption(id, "TempName");
		
		buildModel();
		writeModelToTmpFile();
		bigram_weights = null;
		bigrams_in_sentence = null;
		bigrams = null; 
	}
	
	public void buildModel()
	{
		String texte = "Maximize\n";
		String objective = "score: ";
		for (NGram ng : this.bigram_weights.keySet())
		{
			double bg_weight = this.bigram_weights.get(ng);
			int id_bg = this.bigrams_ids.get(ng);
			/*if ( bg_weight < 3 )
				objective += "+ "+0.0+" c"+id_bg+" ";
			else*/
				objective += "+ "+bg_weight+" c"+id_bg+" ";
		}
		for (int i = 0; i < getCurrentProcess().getCorpusToSummarize().getAllSentence().size(); i++)
		{
			/*Phrase p = this.ss.getSentences().get(i);
			if (p.getNbWords() < 10)
				continue;*/
			int nbMot =  getCurrentProcess().getCorpusToSummarize().getAllSentence().get(i).getNbMot();
			if (nbMot >= getCurrentProcess().getADN().getParameterValue(Integer.class, BiGramILP_Parameter.minSenLength.getName())) {
				double length = getCurrentProcess().getCorpusToSummarize().getAllSentence().get(i).getNbMot() / 1000.;
				objective += "- "+length+" s"+i+" ";
			} 
		}
		texte+=objective+"\n\nSubject To\n";
		boolean first;
		for (NGram ng : this.bigram_weights.keySet())
		{
			first = true;
			String contrainte = "index_"+this.bigrams_ids.get(ng)+": ";
			for (int j = 0; j < this.bigrams_in_sentence.size(); j++)
			{
				/*Phrase p = this.ss.getSentences().get(j);
				if (p.getNbWords() < 10)
					continue;*/
				TreeSet <NGram> curr_set = this.bigrams_in_sentence.get(j);
				for (NGram ng1 : curr_set)
				{
					if (ng1.equals(ng))
					{
						if (first)
						{
							contrainte += "s"+j+" ";
							first = false;
						}
						else contrainte += "+ "+"s"+j+" ";
					}
				}
			}
			contrainte += "- c"+this.bigrams_ids.get(ng)+" >= 0\n";
			texte += contrainte;
		}
		String length_constraint = "length: ";
		for (int i = 0; i < getCurrentProcess().getCorpusToSummarize().getAllSentence().size(); i++)
		{
			SentenceModel p = getCurrentProcess().getCorpusToSummarize().getAllSentence().get(i);
			/*if (p.getNbWords() < 10)
				continue;*/
			//System.out.println("Phrase "+i);
			int length = p.getNbMot();
			if ( i == 0 )
				length_constraint += length+" s"+i;
			else
				length_constraint += " + "+length+" s"+i;
		}
		length_constraint += " <= "+this.maxSummLength+"\n";
		
		texte += length_constraint+"\n\nBinary\n";
		
		for (Entry <NGram, Double> e: this.bigram_weights.entrySet())
			//if (e.getValue() > 2.9)
				texte+= "c"+this.bigrams_ids.get(e.getKey())+"\n";

		for (int j = 0; j < getCurrentProcess().getCorpusToSummarize().getAllSentence().size(); j++)
		{
			texte += "s"+j+"\n";
		}
		texte += "End";
		
		//System.out.println(texte);
		this.model = texte;
	}
	
	private void writeModelToTmpFile()
	{
		try{
			File file = new File(tmpFile);
			if(!file.delete())
				System.err.println(file.getName() + " not deleted!");
			
			FileOutputStream fw = new FileOutputStream(tmpFile);
			OutputStreamWriter osr = new OutputStreamWriter (fw, "UTF-8");
			//BufferedWriter output = new BufferedWriter(fw);
			osr.write(this.model);
			osr.flush();
			osr.close();
		} catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	@Override
	public void setBiGramWeights(HashMap<NGram, Double> bigram_weights) {
		this.bigram_weights = bigram_weights;
	}

	@Override
	public void setBiGramsIds(TreeMap<NGram, Integer> bigrams_ids) {
		this.bigrams_ids = bigrams_ids;
	}

	@Override
	public void setBiGramsInSentence(ArrayList<TreeSet<NGram>> bigrams_in_sentence) {
		this.bigrams_in_sentence = bigrams_in_sentence;
	}

	@Override
	public void setBiGrams(ArrayList<NGram> bigrams) {
		this.bigrams = bigrams;		
	}

	@Override
	public String getFileModel() {
		return tmpFile;
	}
}
