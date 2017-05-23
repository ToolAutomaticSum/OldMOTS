package model.task.process.summarizeMethod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.task.process.indexBuilder.ILP.GLPLauncher;
import optimize.SupportADNException;
import textModeling.SentenceModel;

public class ILP extends AbstractSummarizeMethod implements FileModelBasedIn {

	private String tmpFile;
	
	public ILP(int id) throws SupportADNException {
		super(id);
	}
	
	@Override
	public AbstractSummarizeMethod makeCopy() throws Exception {
		ILP p = new ILP(id);
		initCopy(p);
		return p;
	}
	
	@Override
	public void initADN() throws Exception {
	}
	
	@Override
	public ArrayList<SentenceModel> calculateSummary() {
		runGLPK();
		
		ArrayList <Integer> ind_selected_sentences;
		System.out.println("Summary computation");
		ind_selected_sentences = this.getSentencesFromGLPKSol();
		ArrayList<SentenceModel> summary = new ArrayList <SentenceModel>();
		
		for (Integer i : ind_selected_sentences)
		{
			summary.add(getCurrentProcess().getCorpusToSummarize().getAllSentence().get(i));
		}
		
		this.eraseTmpFiles();
		return summary;
	}
	
	private ArrayList<Integer> getSentencesFromGLPKSol()
	{
		ArrayList <Integer> ind_sentences = new ArrayList <Integer> ();
		String line = "";
		int ind;
		try{
			InputStreamReader isr = new InputStreamReader (new FileInputStream ("sortie_ilp.sol"), "ASCII" );
			BufferedReader br = new BufferedReader(isr);
			while ( (line = br.readLine() ) != null)
			{
				if ( (ind = this.decodeLine (line)) != -1 )
				{
					ind_sentences.add(ind);
				}
			}
			br.close();
		}catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		
		return ind_sentences;
	}
	
	/**
	 * Decode a line from glpk sol
	 * @param line the line to decode
	 * @return -1 if not a line containing a selected sentence, the index of the selected sentence elsewhere
	 */
	private int decodeLine (String line)
	{
		int i = -1;
		Pattern pattern = Pattern.compile ("^[\\s]+[0-9]+[\\s]+s([0-9]+)[ \\*]+1");
		Matcher match = pattern.matcher(line);
		
		if (match.find())
		{
			return new Integer(match.group(1)).intValue();
		}

		return i;
	}
	
	private void eraseTmpFiles()
	{
		File file = new File("sortie_ilp.sol");
		if(! file.delete())
			System.err.println(file.getName() + " not deleted!");
	}
	
	private void runGLPK()
	{
		GLPLauncher glp = new GLPLauncher(tmpFile);
		glp.runGLP();
	}

	@Override
	public void setFileModel(String fileModel) {
		this.tmpFile = fileModel;
	}
}
