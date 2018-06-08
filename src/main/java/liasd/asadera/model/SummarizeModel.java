package main.java.liasd.asadera.model;

import java.io.File;
import java.util.Iterator;

import main.java.liasd.asadera.exception.LacksOfFeatures;
import main.java.liasd.asadera.model.task.preProcess.AbstractPreProcess;
import main.java.liasd.asadera.model.task.process.AbstractProcess;
import main.java.liasd.asadera.model.task.process.SummarizeProcess;
import main.java.liasd.asadera.textModeling.MultiCorpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.tools.reader_writer.Writer;

public class SummarizeModel extends AbstractModel {

	/**
	 */
	@Override
	public void run() throws Exception {
		//try {
			loadMultiCorpusModels();

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

				System.out.println(currentMultiCorpus);
			}

			multiCorpusIt = getMultiCorpusModels().iterator();
			while (multiCorpusIt.hasNext()) {
				currentMultiCorpus = multiCorpusIt.next();
				System.out.println("MultiCorpus : " + currentMultiCorpus.getiD());

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
					System.out.println(time);
					Writer w = new Writer(getOutputPath() + File.separator + getName().split(File.separator)[0]
							+ File.separator + "process_time.txt");
					w.open(true);
					w.write(getName().split(File.separator)[1] + "\t" + time + "\n");
					w.close();
				}
			}
			if (isRougeEvaluation()) {
				getEvalRouge().setModel(this);
				getEvalRouge().setCurrentMultiCorpus(currentMultiCorpus);
				getEvalRouge().init();
				getEvalRouge().process();
				getEvalRouge().finish();
			}
		/*} catch (LacksOfFeatures e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	/**
	 * @param multiCorpus
	 * @param p
	 * @throws Exception
	 */
	public void runProcess(MultiCorpus multiCorpus, SummarizeProcess p) throws Exception {
		if (isMultiThreading()) {
			int nbThreads = p.getListCorpusId().size();

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
			}
		} else {
			for (int i : p.getListCorpusId()) {
				p.setCurrentMultiCorpus(multiCorpus);
				p.setSummarizeIndex(i);
				p.init();
				p.process();
				p.finish();
				setChanged();
				notifyObservers("Corpus " + i + "\n"
						+ SentenceModel.listSentenceModelToString(p.getSummary().get(multiCorpus.getiD()).get(i)));
			}
		}
	}
}
