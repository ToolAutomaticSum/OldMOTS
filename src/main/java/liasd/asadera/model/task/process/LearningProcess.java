package main.java.liasd.asadera.model.task.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import main.java.liasd.asadera.model.AbstractModel;
import main.java.liasd.asadera.model.task.preProcess.GenerateTextModel;
import main.java.liasd.asadera.model.task.preProcess.StanfordNLPPreProcess;
import main.java.liasd.asadera.model.task.process.indexBuilder.LearningModelBuilder;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.optimize.parameter.ADN;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.MultiCorpus;

public class LearningProcess extends AbstractProcess {

	protected List<Corpus> listCorpus = new ArrayList<Corpus>();

	protected List<LearningModelBuilder> modelBuilders;

	protected boolean liveProcess = false;

	protected String modelName;

	public LearningProcess(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public AbstractProcess makeCopy() throws Exception {
		throw new NullPointerException("Can't copy LearningProcess !");
	}

	@Override
	public void initADN() throws Exception {
		initCorpusToCompress();

		liveProcess = Boolean.parseBoolean(getModel().getProcessOption(id, "LiveProcess"));
		modelName = getModel().getProcessOption(id, "ModelName");

		for (Corpus c : getCurrentMultiCorpus()) {
			if (listCorpusId.contains(c.getiD()))
				listCorpus.add(c);
		}

		adn = new ADN(supportADN);
	}

	@Override
	public void init() throws Exception {
		super.init();
		if (!liveProcess) {
			for (Corpus c : getCurrentMultiCorpus()) {
				GenerateTextModel.readTempDocument(getModel().getOutputPath() + File.separator + "temp", c, true);
				System.out.println("Corpus " + c.getiD() + " read");
			}
		}
	}

	@Override
	public void process() throws Exception {
		if (!liveProcess) {
			for (LearningModelBuilder lmb : modelBuilders)
				lmb.learn(listCorpus, modelName);
		} else {
			int limitSize = 50000;
			String propStanfordNLP = "tokenize, ssplit, pos, lemma";
			Properties props = new Properties();
			props.put("annotators", propStanfordNLP);
			props.put("threads", "8");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

			for (Corpus c : getCurrentMultiCorpus()) {
				for (String docName : c.getDocNames()) {
					File textFile = new File(c.getInputPath() + File.separator + docName);

					try {
						InputStream ips = new FileInputStream(textFile);
						InputStreamReader ipsr = new InputStreamReader(ips);
						BufferedReader br = new BufferedReader(ipsr);
						String line;
						String textToProcess = "";
						int readSize = 0;
						int totalSize = 0;
						int i = 0;
						while ((line = br.readLine()) != null) {
							long time = System.currentTimeMillis();
							readSize += line.length();
							totalSize += line.length();
							textToProcess += line;
							if (readSize > limitSize) {
								System.out.println("Reading : " + (System.currentTimeMillis() - time));
								List<String> listSentence = StanfordNLPPreProcess
										.liveProcessToListString(pipeline, textToProcess);
								textToProcess = "";
								System.out.println("Reading + processing : " + (System.currentTimeMillis() - time));
								for (LearningModelBuilder lmb : modelBuilders)
									lmb.liveLearn(listSentence, modelName);
								System.out.println("Part " + i + " Read. TotalSize : " + totalSize);
								readSize = 0;
								i++;
								time = System.currentTimeMillis();
							}
						}
						br.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void finish() throws Exception {
	}

	@Override
	public void initOptimize() throws Exception {
		throw new NullPointerException("Can't optimize LearningProcess !");
	}

	@Override
	public void optimize() throws Exception {
		throw new NullPointerException("Can't optimize LearningProcess !");
	}

	public List<LearningModelBuilder> getModelBuilders() {
		return modelBuilders;
	}

	public void setModelBuilders(List<LearningModelBuilder> modelBuilders) {
		this.modelBuilders = modelBuilders;
		for (LearningModelBuilder acb : modelBuilders) {
			if (acb.getSupportADN() != null)
				supportADN.putAll(acb.getSupportADN());
		}
	}

	@Override
	public void setModel(AbstractModel model) {
		super.setModel(model);
		if (modelBuilders != null) {
			for (LearningModelBuilder lmb : modelBuilders)
				lmb.setModel(model);
		}
	}

	@Override
	public void setCurrentMultiCorpus(MultiCorpus currentMultiCorpus) {
		super.setCurrentMultiCorpus(currentMultiCorpus);
		if (modelBuilders != null) {
			for (LearningModelBuilder lmb : modelBuilders)
				lmb.setCurrentMultiCorpus(currentMultiCorpus);
		}
	}
}
