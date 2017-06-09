package model.task.process.tempProcess;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import exception.LacksOfFeatures;
import model.task.AbstractTaskModel;
import model.task.preProcess.GenerateTextModel;
import optimize.Optimize;
import optimize.SupportADNException;
import textModeling.Corpus;

public abstract class AbstractProcess extends Optimize implements AbstractTaskModel/*, Runnable*/ {
	//private Thread t;
	//private AbstractProcess[] threads = null;
	//int nbThreads = 0;
	
	protected int summarizeIndex = 0;
	protected List<Integer> listCorpusId;
	protected Corpus corpusToSummarize;
	protected boolean readStopWords = false;
	
	public AbstractProcess(int id) throws SupportADNException {
		super(id);
		
		supportADN = new HashMap<String, Class<?>>();
	}
	
	public abstract AbstractProcess makeCopy() throws Exception;
	
	protected void initCopy(AbstractProcess p) throws Exception {
		p.setListCorpusId(new ArrayList<Integer>(listCorpusId));
		p.setReadStopWords(readStopWords);
		p.setSupportADN(new HashMap<String, Class<?>>(supportADN));
	}

	public final void initCorpusToCompress() throws NumberFormatException, LacksOfFeatures {
		listCorpusId = new ArrayList<Integer>();
		for (String corpusId : getModel().getProcessOption(id, "CorpusIdToSummarize").split("\t"))
			listCorpusId.add(Integer.parseInt(corpusId));
	}
	
	@Override
	public abstract void initADN() throws Exception;

	@Override
	public void init() throws Exception {
		try {
			readStopWords = Boolean.parseBoolean(getModel().getProcessOption(id, "ReadStopWords"));
		}
		catch (LacksOfFeatures e) {
			System.out.println("ReadStopWords = false");
			readStopWords = false;
		}
		corpusToSummarize = GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp", getCurrentMultiCorpus().get(getSummarizeCorpusId()), readStopWords);
		System.out.println("Corpus " + corpusToSummarize.getiD() + " read");
	}
	
	/**
	 * Appel à super.process() une fois les process effectués.
	 */
	@Override
	public abstract void process() throws Exception;
	
	@Override
	public abstract void finish() throws Exception;
	
	@Override
	public abstract void initOptimize() throws Exception;

	@Override
	public abstract void optimize() throws Exception;
	
	public int getSummarizeIndex() {
		return summarizeIndex;
	}

	public void setSummarizeIndex(int summarizeIndex) {
		this.summarizeIndex = summarizeIndex;
	}
	
	public List<Integer> getListCorpusId() {
		return listCorpusId;
	}

	public Corpus getCorpusToSummarize() {
		return corpusToSummarize;
	}
	
	/**
	 * Retourne l'Id du corpus résumé
	 * @return
	 */
	public Integer getSummarizeCorpusId() {
		return listCorpusId.get(summarizeIndex);
	}
	
	
	public void setListCorpusId(List<Integer> listCorpusId) {
		this.listCorpusId = listCorpusId;
	}

	public void setReadStopWords(boolean readStopWords) {
		this.readStopWords = readStopWords;
	}

	/**
	 * Nécessite utilisation de ROUGE
	 */
	@Override
	public double getScore() {
		if (!getModel().isbRougeEvaluation())
			return 0;
		else
			return score;
	}


}
