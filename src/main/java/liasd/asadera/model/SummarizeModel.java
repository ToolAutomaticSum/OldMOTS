package main.java.liasd.asadera.model;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.liasd.asadera.exception.EmptyCorpusListException;
import main.java.liasd.asadera.model.task.preProcess.AbstractPreProcess;
import main.java.liasd.asadera.model.task.process.AbstractProcess;
import main.java.liasd.asadera.model.task.process.SummarizeProcess;
import main.java.liasd.asadera.textModeling.MultiCorpus;
import main.java.liasd.asadera.tools.reader_writer.Writer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

public class SummarizeModel extends AbstractModel {
	
	private static Logger logger = LoggerFactory.getLogger(SummarizeModel.class);

	/**
	 */
	@Override
	public void run() throws Exception {
		loadMultiCorpusModels();
		
		if (getPreProcess().size() != 0)
			logger.info("Starting preprocess.");

		Iterator<AbstractPreProcess> preProIt = getPreProcess().iterator();
		while (preProIt.hasNext()) {
			AbstractPreProcess p = preProIt.next();
			p.setModel(this);
			p.init();
		}

		Iterator<MultiCorpus> multiCorpusIt = getMultiCorpusModels().iterator();
		while (multiCorpusIt.hasNext()) {
			currentMultiCorpus = multiCorpusIt.next();
			preProIt = getPreProcess().iterator();
			while (preProIt.hasNext()) {
				AbstractPreProcess p = preProIt.next();
				p.setCurrentMultiCorpus(currentMultiCorpus);
				p.process();
			}
			preProIt = getPreProcess().iterator();
			while (preProIt.hasNext()) {
				AbstractPreProcess p = preProIt.next();
				p.finish();
			}
		}
		
		if (getPreProcess().size() != 0)
			logger.info("End of preprocessing.");
		if (getProcess().size() != 0)
			logger.info("Starting generation of abstract.");
		
		multiCorpusIt = getMultiCorpusModels().iterator();
		while (multiCorpusIt.hasNext()) {
			currentMultiCorpus = multiCorpusIt.next();
			logger.info("MultiCorpus : " + currentMultiCorpus.getiD());

			if (currentMultiCorpus.size() == 0) {
				logger.error("Multicorpus is empty. You need to add at least one corpus with one document.");
				throw new EmptyCorpusListException(String.valueOf(currentMultiCorpus.getiD()));
			}
			
			Iterator<AbstractProcess> proIt = getProcess().iterator();
			while (proIt.hasNext()) {
				long time = System.currentTimeMillis();
				SummarizeProcess p = (SummarizeProcess) proIt.next();
				p.setCurrentMultiCorpus(currentMultiCorpus);
				p.setModel(this);
				p.initCorpusToCompress();
				p.initADN();
				runProcess(currentMultiCorpus, p);
				time = System.currentTimeMillis() - time;
				Writer w = new Writer(getOutputPath() + File.separator + getName().split(File.separator)[0]
						+ File.separator + "process_time.txt");
				w.open(true);
				w.write(getName().split(File.separator)[1] + "\t" + time + "\n");
				w.close();
			}
		}
		if (getProcess().size() != 0)
			logger.info("End of abstract's generation.");
		if (isRougeEvaluation()) {
			logger.info("ROUGE Evaluation");
			if (currentMultiCorpus.hasModelSummaries())
				logger.error("WARNING : Multicorpus don't have model summaries for each corpus. Evaluation might send an error.");
			getEvalRouge().setModel(this);
			getEvalRouge().setCurrentMultiCorpus(currentMultiCorpus);
			getEvalRouge().init();
			getEvalRouge().process();
			getEvalRouge().finish();
		}
	}

	/**
	 * @param multiCorpus
	 * @param p
	 * @throws Exception
	 */
	public void runProcess(MultiCorpus multiCorpus, SummarizeProcess p) throws Exception {
		if (isMultiThreading()) {
			try (ProgressBar pb = new ProgressBar("Summarizing ", p.getListCorpusId().size(), ProgressBarStyle.ASCII)) {
				int nbThreads = p.getListCorpusId().size();
					
				if (nbThreads == 0)
					throw new EmptyCorpusListException(String.valueOf(multiCorpus.getiD()));
				SummarizeProcess[] threads = new SummarizeProcess[nbThreads];
	
				threads[0] = p;
				for (int i = 0; i < nbThreads; i++) {
					if (i != 0)
						threads[i] = p.makeCopy();
					threads[i].setCurrentMultiCorpus(new MultiCorpus(multiCorpus));
					threads[i].setModel(this);
					threads[i].setSummarizeIndex(p.getListCorpusId().get(i));
					threads[i].initADN();
					threads[i].init();
				}
				for (int i = 0; i < nbThreads; i++) {
					threads[i].start();
				}
				for (int i = 0; i < nbThreads; i++) {
					threads[i].join();
					pb.step();
				}
			}
		} else {
			try (ProgressBar pb = new ProgressBar("Summarizing ", p.getListCorpusId().size(), ProgressBarStyle.ASCII)) {
				for (int i : p.getListCorpusId()) {
					p.setCurrentMultiCorpus(multiCorpus);
					p.setSummarizeIndex(i);
					p.init();
					if (p.getCorpusToSummarize().size() == 0)
						continue;
					p.process();
					p.finish();
//					if (isVerbose()) {
					setChanged();
					notifyObservers(new ImmutablePair<String, Object>(p.getCorpusToSummarize().getCorpusName(), p.getSummary().get(multiCorpus.getiD()).get(i)));
						//notifyObservers("Corpus " + i + "\n"
						//		+ SentenceModel.listSentenceModelToString(p.getSummary().get(multiCorpus.getiD()).get(i), isVerbose()));
//					}
//					else {
//						
//					}
					pb.step();
				}
			}
		}
	}
}
