package model.task.process.scoringMethod.ILP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import model.task.process.scoringMethod.AbstractScoringMethod;
import optimize.SupportADNException;
import textModeling.wordIndex.NGram;

public class GenerateModel_ILP extends AbstractScoringMethod implements BiGramListBasedIn, FileModelBasedOut {

	static {
		supportADN = new HashMap<String, Class<?>>();
	}
	
	private ArrayList<Double> bigram_weights;
	private ArrayList<ArrayList<Integer>> bigrams_in_sentence;
	private ArrayList<NGram> bigrams; 
	private String model;
	
	private Integer maxSummLength;
	private String tmpFile;
	
	public GenerateModel_ILP(int id) throws SupportADNException {
		super(id);
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
		for (int i = 0; i < this.bigrams.size(); i++)
		{
			//double bg_weight = this.bigram_weights.get(i); 
/*			if ( bg_weight < 3 )
				objective += "+ "+0.0+" c"+i+" ";
			else*/
				objective += "+ "+this.bigram_weights.get(i)+" c"+i+" ";
		}
		for (int i = 0; i < getCurrentProcess().getSentenceList().size(); i++)
		{
			double length = getCurrentProcess().getSentenceList().get(i).size() / 1000.;
			objective += "- "+length+" s"+i+" ";
		}
		texte+=objective+"\n\nSubject To\n";
		boolean first;
		
		for (int i = 0; i < this.bigrams.size(); i++)
		{
			first = true;
			String contrainte = "index_"+i+": ";
			for (int j = 0; j < this.bigrams_in_sentence.size(); j++)
			{
				ArrayList <Integer> curr_arr = this.bigrams_in_sentence.get(j);
				for (int k = 0; k < curr_arr.size(); k++)
				{
					if (curr_arr.get(k).equals(i))
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
			contrainte += "- c"+i+" >= 0\n";
			texte += contrainte;
		}
		
		String length_constraint = "length: ";
		for (int i = 0; i < getCurrentProcess().getSentenceList().size(); i++)
		{
			int length = getCurrentProcess().getSentenceList().get(i).size();
			if ( i == 0 )
				length_constraint += length+" s"+i;
			else
				length_constraint += " + "+length+" s"+i;
		}
		length_constraint += " <= "+this.maxSummLength+"\n";
		texte += length_constraint+"\n\nBinary\n";
		
		for (int i = 0; i < this.bigrams.size(); i++)
		{
			texte += "c"+i+"\n";
		}
		for (int j = 0; j < getCurrentProcess().getSentenceList().size(); j++)
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
	public void setBiGramWeights(ArrayList<Double> bigram_weights) {
		this.bigram_weights = bigram_weights;
	}

	@Override
	public void setBiGramsInSentence(ArrayList<ArrayList<Integer>> bigrams_in_sentence) {
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
