package main.java.liasd.asadera.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import main.java.liasd.asadera.model.AbstractModel;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.tools.reader_writer.Writer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

public class CommandView extends AbstractView {

	private static Logger logger = LoggerFactory.getLogger(CommandView.class);

	private boolean separateConfFile;
	private String confProcessFilePath;
	private String confMultiCorpusFilePath;

	public CommandView(String confFilePath) {
		super();
		this.separateConfFile = false;
		this.confProcessFilePath = confFilePath;
	}

	public CommandView(String confProcessFilePath, String confMultiCorpusFilePath) {
		super();
		this.separateConfFile = true;
		this.confProcessFilePath = confProcessFilePath;
		this.confMultiCorpusFilePath = confMultiCorpusFilePath;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) {
		AbstractModel m = ctrl.getModel();
		ImmutablePair<String, Object> p = (ImmutablePair<String, Object>) arg;
		List<SentenceModel> l = (List<SentenceModel>) p.getValue();
		String name = p.getKey();
		if (m.isVerbose()) {
			//System.out.println("\n" + (String) arg);
			System.out.println("\n" + SentenceModel.listSentenceModelToString(l, true));
		}
		String output = m.getOutputPath() + File.separator + m.getName() + File.separator + "summary";
		new File(output).mkdirs();
		Writer w = new Writer(output + File.separator + name);
		try {
			w.open(true);
			for (SentenceModel s : l) {
				try {
					w.write(s.toString() + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				w.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void display() throws Exception {
		if (separateConfFile) {
			loadProcessConfiguration(confProcessFilePath);
			loadMultiCorpusConfiguration(confMultiCorpusFilePath);
		} else
			loadConfiguration(confProcessFilePath);
		getCtrl().run();
	}

	@Override
	public void close() {
	}

	public void init() throws Exception {
		if (separateConfFile) {
			loadProcessConfiguration(confProcessFilePath);
			loadMultiCorpusConfiguration(confMultiCorpusFilePath);
		} else
			loadConfiguration(confProcessFilePath);
	}

	private void loadMultiCorpusConfiguration(String configFilePath) throws Exception {
		File fXmlFile = new File(configFilePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		Element root = doc.getDocumentElement();
		NodeList listTask = root.getChildNodes();

		for (int i = 0; i < listTask.getLength(); i++) {
			if (listTask.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element task = (Element) listTask.item(i);

				//getCtrl().notifyTaskChanged(Integer.parseInt(task.getAttribute("ID")));

				NodeList multiCorpusList = task.getElementsByTagName("MULTICORPUS");
				for (int l = 0; l < multiCorpusList.getLength(); l++) {
					if (multiCorpusList.item(l).getNodeType() == Node.ELEMENT_NODE) {
						NodeList languages = ((Element)multiCorpusList.item(l)).getElementsByTagName("LANGUAGE");
						if (languages.getLength() > 0 && languages.item(0).getTextContent() != getCtrl().getModel().getLanguage())
							continue;
						getCtrl().notifyMultiCorpusChanged();

						NodeList corpusList = task.getElementsByTagName("CORPUS");
						try (ProgressBar pb = new ProgressBar("Configuring Multicorpus n°" + i, corpusList.getLength(), ProgressBarStyle.ASCII)) {
							for (int j = 0; j < corpusList.getLength(); j++) {
								if (corpusList.item(j).getNodeType() == Node.ELEMENT_NODE) {								
									Element corpus = (Element) corpusList.item(j);
									
									NodeList summaryElement = corpus.getElementsByTagName("SUMMARY_PATH");
									String summaryInputPath = null;
									if (summaryElement != null && summaryElement.getLength() != 0)
										summaryInputPath = summaryElement.item(0).getTextContent();
									
									NodeList inputElement = corpus.getElementsByTagName("INPUT_PATH");
									String corpusInputPath = null;
									if (inputElement != null && inputElement.getLength() != 0)
										corpusInputPath = inputElement.item(0).getTextContent();
									else
										throw new NullPointerException("Missing INPUT_PATH node in XML file " + configFilePath + " for corpus ID " + corpus.getAttribute("ID") + ".");
									
									NodeList documentList = corpus.getElementsByTagName("DOCUMENT");
									List<String> docNames = new ArrayList<String>();
									for (int k = 0; k < documentList.getLength(); k++) {
										if (documentList.item(k).getNodeType() == Node.ELEMENT_NODE) {
											docNames.add(documentList.item(k).getTextContent());
										}
									}
									
									NodeList summaryList = corpus.getElementsByTagName("SUMMARY");
									List<String> summaryNames = new ArrayList<String>();
									for (int k = 0; k < summaryList.getLength(); k++) {
										if (summaryList.item(k).getNodeType() == Node.ELEMENT_NODE) {
											summaryNames.add(summaryList.item(k).getTextContent());
										}
									}
									if (summaryInputPath == null)
										getCtrl().notifyCorpusChanged(corpusInputPath, docNames);
									else
										getCtrl().notifyCorpusChanged(summaryInputPath, summaryNames, corpusInputPath,
											docNames);
									pb.step();
									logger.trace("Corpus n°" + j + "/" + corpusList.getLength() + " has been configured.");
								}
							}
						}
					}
				}
			}
		}
	}

	private void loadProcessConfiguration(String configFilePath) throws ClassNotFoundException {
		Document doc = null;
		try {
			File fXmlFile = new File(configFilePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(fXmlFile);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
			
		Element root = doc.getDocumentElement();
		NodeList listTask = root.getChildNodes();

		for (int i = 0; i < listTask.getLength(); i++) {
			if (listTask.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element task = (Element) listTask.item(i);

				getCtrl().notifyTaskChanged(Integer.parseInt(task.getAttribute("ID")));

				getCtrl().notifyLanguageChanged(task.getElementsByTagName("LANGUAGE").item(0).getTextContent());

				getCtrl().notifyOutputPathChanged(task.getElementsByTagName("OUTPUT_PATH").item(0).getTextContent());
				try {
					getCtrl().notifyMultiThreadBoolChanged(Boolean
							.parseBoolean(task.getElementsByTagName("MULTITHREADING").item(0).getTextContent()));
				} catch (Exception e) {
					getCtrl().notifyMultiThreadBoolChanged(false);
				}

				NodeList preProcessList = task.getElementsByTagName("PREPROCESS");
				for (int j = 0; j < preProcessList.getLength(); j++) {
					if (preProcessList.item(j).getNodeType() == Node.ELEMENT_NODE) {
						Element preProcess = (Element) preProcessList.item(j);
						getCtrl().notifyProcessOptionChanged(getProcessOptionMap(preProcess));

						getCtrl().notifyPreProcessChanged(preProcess.getAttribute("NAME"));
					}
				}

				NodeList processList = task.getElementsByTagName("PROCESS");
				for (int j = 0; j < processList.getLength(); j++) {
					if (processList.item(j).getNodeType() == Node.ELEMENT_NODE) {
						Element process = (Element) processList.item(j);
						getCtrl().notifyProcessOptionChanged(getProcessOptionMap(process));

						getCtrl().notifyProcessChanged(process.getAttribute("NAME"));

						NodeList indexBuilderList = process.getElementsByTagName("INDEX_BUILDER");
						for (int k = 0; k < indexBuilderList.getLength(); k++) {
							if (indexBuilderList.item(k).getNodeType() == Node.ELEMENT_NODE) {
								Element indexBuilder = (Element) process.getElementsByTagName("INDEX_BUILDER")
										.item(k);
								getCtrl().notifyProcessOptionChanged(getProcessOptionMap(indexBuilder));
								getCtrl().notifyIndexBuilderChanged(process.getAttribute("NAME"),
										indexBuilder.getAttribute("NAME"));
							}
						}

						NodeList caracBuilderList = process.getElementsByTagName("CARACTERISTIC_BUILDER");
						for (int k = 0; k < caracBuilderList.getLength(); k++) {
							if (caracBuilderList.item(k).getNodeType() == Node.ELEMENT_NODE) {
								Element caracBuilder = (Element) process.getElementsByTagName("CARACTERISTIC_BUILDER").item(k);
								getCtrl().notifyProcessOptionChanged(getProcessOptionMap(caracBuilder));
								getCtrl().notifyCaracteristicBuilderChanged(process.getAttribute("NAME"),
										caracBuilder.getAttribute("NAME"));
							}
						}

						NodeList scoringMethodList = process.getElementsByTagName("SCORING_METHOD");
						for (int k = 0; k < scoringMethodList.getLength(); k++) {
							if (scoringMethodList.item(k).getNodeType() == Node.ELEMENT_NODE) {
								Element scoringMethod = (Element) process.getElementsByTagName("SCORING_METHOD")
										.item(k);
								getCtrl().notifyProcessOptionChanged(getProcessOptionMap(scoringMethod));
								getCtrl().notifyScoringMethodChanged(process.getAttribute("NAME"),
										scoringMethod.getAttribute("NAME"));
							}
						}

						Element summarizeMethod = (Element) process.getElementsByTagName("SUMMARIZE_METHOD")
								.item(0);
						if (summarizeMethod != null) {
							getCtrl().notifyProcessOptionChanged(getProcessOptionMap(summarizeMethod));
							getCtrl().notifySelectionMethodChanged(process.getAttribute("NAME"),
									summarizeMethod.getAttribute("NAME"));
						}

						NodeList postProcessList = process.getElementsByTagName("POSTPROCESS");
						for (int k = 0; k < postProcessList.getLength(); k++) {
							if (postProcessList.item(k).getNodeType() == Node.ELEMENT_NODE) {
								Element postProcess = (Element) postProcessList.item(k);
								getCtrl().notifyProcessOptionChanged(getProcessOptionMap(postProcess));

								getCtrl().notifyPostProcessChanged(process.getAttribute("NAME"),
										postProcess.getAttribute("NAME"));
							}
						}
					}
				}
				NodeList rougeList = task.getElementsByTagName("ROUGE_EVALUATION");
				if (rougeList.getLength() > 0) {
					getCtrl().notifyRougeEvaluationChanged(true);
					Element rouge = (Element) rougeList.item(0);
					getCtrl().notifyRougeMeasureChanged(
							rouge.getElementsByTagName("ROUGE_MEASURE").item(0).getTextContent());
//						getCtrl().notifyRougePathChanged(
//								rouge.getElementsByTagName("ROUGE_PATH").item(0).getTextContent());
					getCtrl().notifyModelRootChanged(
							rouge.getElementsByTagName("MODEL_ROOT").item(0).getTextContent());
					getCtrl().notifyPeerRootChanged(
							rouge.getElementsByTagName("PEER_ROOT").item(0).getTextContent());
				} else
					getCtrl().notifyRougeEvaluationChanged(false);
			}
		}
	}

	private void loadConfiguration(String configFilePath) {
		try {
			File fXmlFile = new File(configFilePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			Element racine = doc.getDocumentElement();
			NodeList listTask = racine.getChildNodes();
			for (int i = 0; i < listTask.getLength(); i++) {
				if (listTask.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element task = (Element) listTask.item(i);

					getCtrl().notifyTaskChanged(Integer.parseInt(task.getAttribute("ID")));

					getCtrl().notifyLanguageChanged(task.getElementsByTagName("LANGUAGE").item(0).getTextContent());
					getCtrl().notifyOutputPathChanged(task.getElementsByTagName("OUTPUT_PATH").item(0).getTextContent());
					try {
						getCtrl().notifyMultiThreadBoolChanged(Boolean
								.parseBoolean(task.getElementsByTagName("MULTITHREADING").item(0).getTextContent()));
					} catch (Exception e) {
						getCtrl().notifyMultiThreadBoolChanged(false);
					}

					NodeList multiCorpusList = task.getElementsByTagName("MULTICORPUS");
					for (int l = 0; l < multiCorpusList.getLength(); l++) {
						if (multiCorpusList.item(l).getNodeType() == Node.ELEMENT_NODE) {
							NodeList languages = ((Element)multiCorpusList.item(l)).getElementsByTagName("LANGUAGE");
							if (languages.getLength() > 0 && languages.item(0).getTextContent() != getCtrl().getModel().getLanguage())
								continue;
							getCtrl().notifyMultiCorpusChanged();

							NodeList corpusList = task.getElementsByTagName("CORPUS");
							for (int j = 0; j < corpusList.getLength(); j++) {
								if (corpusList.item(j).getNodeType() == Node.ELEMENT_NODE) {
									Element corpus = (Element) corpusList.item(j);
									NodeList summaryElement = corpus.getElementsByTagName("SUMMARY_PATH");
									String summaryInputPath = null;
									if (summaryElement != null && summaryElement.getLength() != 0)
										summaryInputPath = summaryElement.item(0).getTextContent();
									String corpusInputPath = corpus.getElementsByTagName("INPUT_PATH").item(0)
											.getTextContent();
									NodeList documentList = corpus.getElementsByTagName("DOCUMENT");
									List<String> docNames = new ArrayList<String>();
									for (int k = 0; k < documentList.getLength(); k++) {
										if (documentList.item(k).getNodeType() == Node.ELEMENT_NODE) {
											docNames.add(documentList.item(k).getTextContent());
										}
									}
									NodeList summaryList = corpus.getElementsByTagName("SUMMARY");
									List<String> summaryNames = new ArrayList<String>();
									for (int k = 0; k < summaryList.getLength(); k++) {
										if (summaryList.item(k).getNodeType() == Node.ELEMENT_NODE) {
											summaryNames.add(summaryList.item(k).getTextContent());
										}
									}
									if (summaryInputPath == null)
										getCtrl().notifyCorpusChanged(corpusInputPath, docNames);
									else
										getCtrl().notifyCorpusChanged(summaryInputPath, summaryNames, corpusInputPath,
											docNames);
								}
							}
						}
					}

					NodeList preProcessList = task.getElementsByTagName("PREPROCESS");
					for (int j = 0; j < preProcessList.getLength(); j++) {
						if (preProcessList.item(j).getNodeType() == Node.ELEMENT_NODE) {
							Element preProcess = (Element) preProcessList.item(j);
							getCtrl().notifyProcessOptionChanged(getProcessOptionMap(preProcess));

							getCtrl().notifyPreProcessChanged(preProcess.getAttribute("NAME"));
						}
					}

					NodeList processList = task.getElementsByTagName("PROCESS");
					for (int j = 0; j < processList.getLength(); j++) {
						if (processList.item(j).getNodeType() == Node.ELEMENT_NODE) {
							Element process = (Element) processList.item(j);
							getCtrl().notifyProcessOptionChanged(getProcessOptionMap(process));

							getCtrl().notifyProcessChanged(process.getAttribute("NAME"));

							NodeList indexBuilderList = process.getElementsByTagName("INDEX_BUILDER");
							for (int k = 0; k < indexBuilderList.getLength(); k++) {
								if (indexBuilderList.item(k).getNodeType() == Node.ELEMENT_NODE) {
									Element indexBuilder = (Element) process.getElementsByTagName("INDEX_BUILDER")
											.item(k);
									getCtrl().notifyProcessOptionChanged(getProcessOptionMap(indexBuilder));
									getCtrl().notifyIndexBuilderChanged(process.getAttribute("NAME"),
											indexBuilder.getAttribute("NAME"));
								}
							}

							NodeList caracBuilderList = process.getElementsByTagName("CARACTERISTIC_BUILDER");
							for (int k = 0; k < caracBuilderList.getLength(); k++) {
								if (caracBuilderList.item(k).getNodeType() == Node.ELEMENT_NODE) {
									Element caracBuilder = (Element) process
											.getElementsByTagName("CARACTERISTIC_BUILDER").item(k);
									getCtrl().notifyProcessOptionChanged(getProcessOptionMap(caracBuilder));
									getCtrl().notifyCaracteristicBuilderChanged(process.getAttribute("NAME"),
											caracBuilder.getAttribute("NAME"));
								}
							}

							NodeList scoringMethodList = process.getElementsByTagName("SCORING_METHOD");
							for (int k = 0; k < scoringMethodList.getLength(); k++) {
								if (scoringMethodList.item(k).getNodeType() == Node.ELEMENT_NODE) {
									Element scoringMethod = (Element) process.getElementsByTagName("SCORING_METHOD")
											.item(k);
									getCtrl().notifyProcessOptionChanged(getProcessOptionMap(scoringMethod));
									getCtrl().notifyScoringMethodChanged(process.getAttribute("NAME"),
											scoringMethod.getAttribute("NAME"));
								}
							}

							Element summarizeMethod = (Element) process.getElementsByTagName("SUMMARIZE_METHOD")
									.item(0);
							if (summarizeMethod != null) {
								getCtrl().notifyProcessOptionChanged(getProcessOptionMap(summarizeMethod));
								getCtrl().notifySelectionMethodChanged(process.getAttribute("NAME"),
										summarizeMethod.getAttribute("NAME"));
							}

							NodeList postProcessList = process.getElementsByTagName("POSTPROCESS");
							for (int k = 0; k < postProcessList.getLength(); k++) {
								if (postProcessList.item(k).getNodeType() == Node.ELEMENT_NODE) {
									Element postProcess = (Element) postProcessList.item(k);
									getCtrl().notifyProcessOptionChanged(getProcessOptionMap(postProcess));

									getCtrl().notifyPostProcessChanged(process.getAttribute("NAME"),
											postProcess.getAttribute("NAME"));
								}
							}
						}
					}
					NodeList rougeList = task.getElementsByTagName("ROUGE_EVALUATION");
					if (rougeList.getLength() > 0) {
						getCtrl().notifyRougeEvaluationChanged(true);
						Element rouge = (Element) rougeList.item(0);
						getCtrl().notifyRougeMeasureChanged(
								rouge.getElementsByTagName("ROUGE_MEASURE").item(0).getTextContent());
//						getCtrl().notifyRougePathChanged(
//								rouge.getElementsByTagName("ROUGE-PATH").item(0).getTextContent());
						getCtrl().notifyModelRootChanged(
								rouge.getElementsByTagName("MODEL_ROOT").item(0).getTextContent());
						getCtrl().notifyPeerRootChanged(
								rouge.getElementsByTagName("PEER_ROOT").item(0).getTextContent());
					} else
						getCtrl().notifyRougeEvaluationChanged(false);
				}
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
	}

	private Map<String, String> getProcessOptionMap(Element element) {
		NodeList optionList = element.getElementsByTagName("OPTION");
		if (optionList.getLength() > 0) {
			Map<String, String> processOption = new HashMap<String, String>();
			for (int k = 0; k < optionList.getLength(); k++) {
				Element option = (Element) optionList.item(k);
				if (option.getParentNode().isEqualNode(element)) {
					processOption.put(option.getAttribute("NAME"), option.getTextContent());
				}
			}
			return processOption;
		} else
			return null;
	}
}
