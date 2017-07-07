package model.task.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import exception.LacksOfFeatures;
import model.task.AbstractTask;
import optimize.Optimize;
import optimize.SupportADNException;
import textModeling.Corpus;

public abstract class AbstractProcess extends Optimize implements AbstractTask/*, Runnable*/ {
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
		try {
			readStopWords = Boolean.parseBoolean(getModel().getProcessOption(id, "ReadStopWords"));
		}
		catch (LacksOfFeatures e) {
			System.out.println("ReadStopWords = false");
			readStopWords = false;
		}
		
		listCorpusId = new ArrayList<Integer>();
		for (String corpusId : getModel().getProcessOption(id, "CorpusIdToSummarize").split("\t"))
			listCorpusId.add(Integer.parseInt(corpusId));
	}
	
	@Override
	public abstract void initADN() throws Exception;

	@Override
	public abstract void init() throws Exception;
	
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
		return summarizeIndex;
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
