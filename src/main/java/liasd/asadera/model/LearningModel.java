package main.java.liasd.asadera.model;

import java.util.Iterator;

import main.java.liasd.asadera.exception.LacksOfFeatures;
import main.java.liasd.asadera.model.task.preProcess.AbstractPreProcess;
import main.java.liasd.asadera.model.task.process.AbstractProcess;
import main.java.liasd.asadera.model.task.process.LearningProcess;
import main.java.liasd.asadera.textModeling.MultiCorpus;

public class LearningModel extends AbstractModel {

	@Override
	public void run() {
		try {
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
					LearningProcess p = (LearningProcess) proIt.next();
					p.setModel(this);
					p.setCurrentMultiCorpus(currentMultiCorpus);
					p.initCorpusToCompress();
					p.initADN();
					runProcess(currentMultiCorpus, p);
					System.out.println(System.currentTimeMillis() - time);
				}
			}
		} catch (LacksOfFeatures e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param multiCorpus
	 * @param p
	 * @throws Exception
	 */
	public void runProcess(MultiCorpus multiCorpus, LearningProcess p) throws Exception {
		p.init();
		p.process();
		String t = "MultiCorpus " + multiCorpus.getiD() + " learn.";
		setChanged();
		notifyObservers(t);
		p.finish();
	}
}
