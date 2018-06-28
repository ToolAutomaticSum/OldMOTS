package main.java.liasd.asadera.model.task.preProcess;

import java.io.File;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import main.java.liasd.asadera.exception.EmptyTextListException;
import main.java.liasd.asadera.exception.LacksOfFeatures;
import main.java.liasd.asadera.exception.StateException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.MultiCorpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.WordModel;
import main.java.liasd.asadera.tools.Tools;
import main.java.liasd.asadera.tools.reader_writer.Reader;
import main.java.liasd.asadera.tools.reader_writer.Writer;
import main.java.liasd.asadera.tools.wordFilters.TrueFilter;
import main.java.liasd.asadera.tools.wordFilters.WordStopListFilter;

public class GenerateTextModel extends AbstractPreProcess {

	private static Logger logger = LoggerFactory.getLogger(GenerateTextModel.class);
	
	public GenerateTextModel(int id) {
		super(id);
	}

	@Override
	public void init() throws Exception {

		try {
			getModel().setFilter(new WordStopListFilter(getModel().getProcessOption(id, "StopWordListFile")));
		} catch (LacksOfFeatures e) {
			getModel().setFilter(new TrueFilter());
		}
	}

	@Override
	public void process() throws Exception {
		int nbDoc = 0;
		for (Corpus corpus : getCurrentMultiCorpus())
			nbDoc += corpus.size();
		logger.trace("Reading " + nbDoc + " documents from files");
		for (Corpus corpus : getCurrentMultiCorpus()) {
			if (corpus.size() == 0)
				logger.error("Corpus list is empty.", new EmptyTextListException(String.valueOf(corpus.getiD())));
			for (TextModel text : corpus) {
				if (text != null)
					if (!loadText(text)) {
						logger.warn("Can't load " + text.getDocumentFilePath() + ".");
					}
			}
		}
	}

	@Override
	public void finish() {
		if (getModel().getFilter() != null) {
			for (Corpus corpus : getCurrentMultiCorpus()) {
				for (TextModel text : corpus) {
					for (SentenceModel sen : text) {
						for (WordModel word : sen.getListWordModel())
							if (!getModel().getFilter().passFilter(word))
								word.setStopWord(true);
					}
				}
			}
		}

		new File(getModel().getOutputPath() + File.separator + "temp").mkdir();
		try {
			GenerateTextModel.writeTempDocumentBySentence(getModel().getOutputPath() + File.separator + "temp",
					getCurrentMultiCorpus());
		} catch (Exception e) {
			logger.error("Error while writing preprocessed document in temp folder.");
			e.printStackTrace();
		}
	}

	public static boolean loadText(TextModel textModel) throws Exception {
		if (textModel.getText().equals("")) {
			File fXmlFile = new File(textModel.getDocumentFilePath());
			Reader reader = new Reader(textModel.getDocumentFilePath(), true);
			reader.open();
			String header = reader.read();
			reader.close();
			if (Tools.getFileExtension(fXmlFile).equals("json")) {
				JSONObject obj = new JSONObject(fXmlFile.getAbsolutePath());
				JSONArray arr = obj.getJSONArray("\"reviews\":");

				for (int i = 0; i < arr.length(); i++) {
					
				}
				return false;
			} else if (header != null && !header.contains("<") && !header.contains(">")) {
				reader.open();
				String text = reader.read();
				String temp = "";
				while (text != null) {
					temp += text + "\n";
					text = reader.read();
				}
				textModel.setText(temp + "\n");
				System.out.println("Reading " + textModel.getTextName());
				return true;
			} else if (header != null) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
				NodeList listLabels = doc.getElementsByTagName("LABEL");

				if (listLabels.getLength() > 0) {
					for (int i = 0; i < listLabels.getLength(); i++)
						textModel.getLabels().add(listLabels.item(i).getTextContent().replace("\n", ""));
				}

				NodeList listText = doc.getElementsByTagName("TEXT");
				if (listText.getLength() > 0) {
					for (int i = 0; i < listText.getLength(); i++) {
						if (listText.item(i).getNodeType() == Node.ELEMENT_NODE) {
							Element task = (Element) listText.item(i);
							NodeList pList = task.getElementsByTagName("P");
							if (pList.getLength() != 0) {
								for (int j = 0; j < pList.getLength(); j++) {
									if (pList.item(j).getNodeType() == Node.ELEMENT_NODE) {
										Element p = (Element) pList.item(j);
										textModel.setText(textModel.getText() + p.getTextContent().replace(":", ".")
												.replace("\n", " ").replace("  ", " ").replace("-", " ") + "\n");
									}
								}
							} else {
								textModel.setText(textModel.getText() + task.getTextContent().replace(":", ".")
										.replace("\n", " ").replace("  ", " ").replace("-", " ") + "\n");
							}
						}
					}
					logger.info("Reading " + textModel.getTextName());
					return true;
				} else
					return false;
			} else
				return false;
		} else
			return false;
	}

	public static void writeTempDocumentBySentence(String outputPath, MultiCorpus mc) throws Exception {
		Iterator<Corpus> corpusIt = mc.iterator();
		while (corpusIt.hasNext()) {
			Corpus corpus = corpusIt.next();
			Iterator<TextModel> textIt = corpus.iterator();
			while (textIt.hasNext()) {
				TextModel text = textIt.next();
				new File(outputPath + File.separator + corpus.getCorpusName()).mkdir();
				Writer w = new Writer(
						outputPath + File.separator + corpus.getCorpusName() + File.separator + text.getTextName());
				w.open(false);
				String t = "Label=";
				for (String l : text.getLabels())
					t += l + File.separator + "%%" + File.separator;
				w.write(t + "\n");
				Iterator<SentenceModel> senIt = text.iterator();
				while (senIt.hasNext()) {
					SentenceModel sen = senIt.next();
					w.write("[Sen=" + sen.toString() + File.separator + "%%" + File.separator + "NbMot=" + sen.size()
							+ "]" + sen.getSentence() + "\n");
				}
				w.close();
			}
			corpus.clear();
		}
	}

	public static Corpus readTempDocument(String inputPath, Corpus c, boolean readStopWords) throws Exception {
		c.clear();
		File corpusDoc = new File(inputPath + File.separator + c.getCorpusName());
		if (!corpusDoc.exists()) {
			throw new StateException("Corpus file don't exist, so we can't read it.");
		}
		int id = 0;
		for (File textFile : corpusDoc.listFiles()) {
			TextModel text = new TextModel(c, textFile.getAbsolutePath());
			int nbSentence = 0;
			Reader r = new Reader(textFile.getAbsolutePath(), true);
			r.open();
			String s = r.read();
			String[] tabs = s.split("=");
			if (tabs.length == 2) {
				String[] labels = tabs[1].split(File.separator + "%%" + File.separator);
				for (String l : labels)
					text.getLabels().add(l);
			}
			s = r.read();
			while (s != null) {
				tabs = s.split("]");
				if (tabs.length == 2) {
					String[] label = tabs[0].split(File.separator + "%%" + File.separator);
					SentenceModel sen = new SentenceModel(label[0].split("=")[1], id, text);
					nbSentence++;
					sen.setNbMot(Integer.parseInt(label[1].split("=")[1]));
					text.add(sen);
					String[] word = tabs[1].split(" ");
					for (String w : word) {
						WordModel wm;
						if (w.startsWith("%%")) {
							if (readStopWords) {
								w = w.replace("%%", "");
								wm = new WordModel(w);
								wm.setStopWord(true);
								wm.setmLemma(w);
								wm.setSentence(sen);
								sen.getListWordModel().add(wm);
							}
						} else {
							wm = new WordModel(w);
							wm.setmLemma(w);
							wm.setSentence(sen);
							sen.getListWordModel().add(wm);
						}
					}
				}
				s = r.read();
				id++;
			}
			text.setNbSentence(nbSentence);
			r.close();
			c.add(text);
		}
		return c;
	}
}
