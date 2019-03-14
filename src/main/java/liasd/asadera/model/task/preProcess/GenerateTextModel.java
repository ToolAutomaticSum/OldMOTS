package main.java.liasd.asadera.model.task.preProcess;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import main.java.liasd.asadera.exception.EmptyTextListException;
import main.java.liasd.asadera.exception.LacksOfFeatures;
import main.java.liasd.asadera.exception.StateException;
import main.java.liasd.asadera.textModeling.Corpus;
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

		try {
			getModel().setWritePerFile(Boolean.valueOf(getModel().getProcessOption(id, "WritePerFile")));
		} catch (LacksOfFeatures lof) {
			getModel().setWritePerFile(false);
		}
	}

	@Override
	public void process() throws Exception {
		int nbDoc = 0;
		for (Corpus corpus : getCurrentMultiCorpus())
			nbDoc += corpus.size();
		logger.trace("Reading " + nbDoc + " documents from files");

		new File(getModel().getOutputPath() + File.separator + "temp").mkdir();
		for (Corpus corpus : getCurrentMultiCorpus()) {
			String outputPath = getModel().getOutputPath() + File.separator + "temp" + File.separator + corpus.getCorpusName();
			File corpusDirectory = new File(outputPath);
			Tools.deleteFileAndDirectory(corpusDirectory);
			corpusDirectory.mkdirs();
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

		if (!getModel().isWritePerFile()) {
			for (Corpus corpus : getCurrentMultiCorpus()) {
				String outputPath = getModel().getOutputPath() + File.separator + "temp" + File.separator + corpus.getCorpusName();
				for (TextModel text : corpus) {
					try {
						GenerateTextModel.writeTempDocumentBySentence(outputPath, text);
					} catch (Exception e) {
						logger.error("Error while writing preprocessed document " + text.getTextName() + " in temp folder.");
						e.printStackTrace();
					} finally {
						text.clear();
					}
				}
			}
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
			} else if (Tools.getFileExtension(fXmlFile).equals("story")) {
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

	
	
	public static void writeTempDocumentBySentence(String outputPath, TextModel text) throws Exception {
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			 
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
 
        Document document = documentBuilder.newDocument();
 
        // root element
        Element root = document.createElement("doc");
        document.appendChild(root);
        Element labels = document.createElement("labels");
        root.appendChild(labels);
        for (String l : text.getLabels()) {
            Element label = document.createElement("label");
            label.appendChild(document.createTextNode(l));
            labels.appendChild(label);
        }
        int i = 0;
        Element sentences = document.createElement("sentences");
        root.appendChild(sentences);
        for (SentenceModel sen : text) {
        	Element sentence = document.createElement("sentence");
        	
            Attr attr_id = document.createAttribute("id");
            attr_id.setValue(String.valueOf(i));
            sentence.setAttributeNode(attr_id);
            
            Attr attr_size = document.createAttribute("size");
            attr_size.setValue(String.valueOf(sen.size()));
            sentence.setAttributeNode(attr_size);
            
        	sentences.appendChild(sentence);
        	
        	Element original = document.createElement("original");
        	original.appendChild(document.createTextNode(sen.toString()));
        	sentence.appendChild(original);
        	
        	Element stemmed = document.createElement("stemmed");
        	stemmed.appendChild(document.createTextNode(sen.getSentence()));
        	sentence.appendChild(stemmed);
        	i++;
        }
	
        // create the xml file
        //transform the DOM Object to an XML File
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(text.getTextName()));
 
        // If you use
        // StreamResult result = new StreamResult(System.out);
        // the output will be pushed to the standard output ...
        // You can use that for debugging 
 
        transformer.transform(domSource, streamResult);
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
