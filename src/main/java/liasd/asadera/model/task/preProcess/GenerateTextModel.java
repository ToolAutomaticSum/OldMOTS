package main.java.liasd.asadera.model.task.preProcess;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
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
import main.java.liasd.asadera.model.AbstractModel;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.textModeling.WordModel;
import main.java.liasd.asadera.tools.Tools;
import main.java.liasd.asadera.tools.reader_writer.Reader;
import main.java.liasd.asadera.tools.wordFilters.TrueFilter;
import main.java.liasd.asadera.tools.wordFilters.WordStopListFilter;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

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
			logger.error("No StopWordListFile found.");
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
		getModel().setNbDoc(nbDoc);
		logger.info("Reading " + getModel().getNbDoc() + " documents from files");

		new File(getModel().getOutputPath() + File.separator + "temp").mkdir();
		try (ProgressBar pb = new ProgressBar("Reading documents ", getModel().getNbDoc(), ProgressBarStyle.ASCII)) {
			for (Corpus corpus : getCurrentMultiCorpus()) {
				String outputPath = getModel().getOutputPath() + File.separator + "temp" + File.separator + corpus.getCorpusName();
				File corpusDirectory = new File(outputPath);
				//Tools.deleteFileAndDirectory(corpusDirectory);
				corpusDirectory.mkdirs();
				if (corpus.size() == 0)
					logger.error("Corpus list is empty.", new EmptyTextListException(String.valueOf(corpus.getiD())));
				for (TextModel text : corpus) {
					if (text != null)
						if (!loadText(text)) {
							logger.warn("Can't load " + text.getDocumentFilePath() + ".");
						}
					pb.step();
				}
			}
		}
	}

	@Override
	public void finish() {
		if (!getModel().isWritePerFile()) {
			for (Corpus corpus : getCurrentMultiCorpus()) {
				String outputPath = getModel().getOutputPath() + File.separator + "temp" + File.separator + corpus.getCorpusName();
				for (TextModel text : corpus) {
					writeTempText(getModel(), corpus.size(), text, outputPath);
				}
			}
		}
	}
	
	public static void writeTempText(AbstractModel model, int sizeCorpus, TextModel text, String outputPath) {
		if (model.getFilter() != null)
			for (SentenceModel sen : text)
				for (WordModel word : sen.getListWordModel())
					if (!model.getFilter().passFilter(word))
						word.setStopWord(true);
						
		try {
			boolean corpusFolder;
			File f;
			if (sizeCorpus == 1) {
				corpusFolder = false;
				f = new File(outputPath);
			}
			else {
				corpusFolder = true;
				f = new File(outputPath + File.separator + text.getTextName());
			}
				
			if (f.exists())
				f.delete();
			GenerateTextModel.writeTempDocumentBySentence(outputPath, text, corpusFolder);
		} catch (Exception e) {
			logger.error("Error while writing preprocessed document " + text.getTextName() + " in temp folder.");
			e.printStackTrace();
		} finally {
			text.clear();
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
				String line = reader.read();
				String temp = "";
				while (line != null) {
					temp += line + "\n";
					line = fixMissingPeriod(reader.read());
				}
				textModel.setText(temp + "\n");
				logger.trace("Reading " + textModel.getTextName());
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
					logger.trace("Reading " + textModel.getTextName());
					return true;
				} else
					return false;
			} else
				return false;
		} else
			return false;
	}
	
	public static void writeTempDocumentBySentence(String outputPath, TextModel text, boolean corpusFolder) throws Exception {
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			 
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
 
        Document document = documentBuilder.newDocument();
 
        // root element
        Element root = document.createElement("doc");
        Attr attr_name = document.createAttribute("name");
        attr_name.setValue(text.getTextName());
        root.setAttributeNode(attr_name);
        document.appendChild(root);
        for (String l : text.getLabels()) {
            Element label = document.createElement("labels");
            label.appendChild(document.createTextNode(l));
            root.appendChild(label);
        }
        int i = 0;
        for (SentenceModel sen : text) {
        	Element sentence = document.createElement("sentence");
        	
            Attr attr_id = document.createAttribute("id");
            attr_id.setValue(String.valueOf(i));
            sentence.setAttributeNode(attr_id);
            
            Attr attr_size = document.createAttribute("size");
            attr_size.setValue(String.valueOf(sen.size()));
            sentence.setAttributeNode(attr_size);
            
        	root.appendChild(sentence);
        	
        	Element original = document.createElement("original");
        	original.appendChild(document.createTextNode(sen.toString()));
        	sentence.appendChild(original);
        	
        	Element stemmed = document.createElement("stemmed");
        	stemmed.appendChild(document.createTextNode(sen.getSentence()));
        	sentence.appendChild(stemmed);
        	
        	for (String l : sen.getLabels()) {
                Element label = document.createElement("label");
                label.appendChild(document.createTextNode(l));
                sentence.appendChild(label);
            }
        	i++;
        }
	
        // create the xml file
        //transform the DOM Object to an XML File
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult;
        if (corpusFolder)
        	streamResult = new StreamResult(new File(outputPath + File.separator + text.getTextName()));
        else
        	streamResult = new StreamResult(new File(outputPath));
 
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
			throw new StateException("Preprocess corpus file don't exist, so we can't read it. Please apply preprocess first.");
		}
		int id = 0;
		File[] listFiles;
		if (corpusDoc.isDirectory())
			listFiles = corpusDoc.listFiles();
		else {
			listFiles = new File[1];
			listFiles[0] = corpusDoc;
		}
		for (File textFile : listFiles) {
			TextModel text = new TextModel(c, textFile.getAbsolutePath());
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(textFile.getAbsolutePath());
			NodeList listLabels = doc.getElementsByTagName("label");

			if (listLabels.getLength() > 0) {
				for (int i = 0; i < listLabels.getLength(); i++)
					text.getLabels().add(listLabels.item(i).getTextContent().replace("\n", ""));
			}

			int nbSentence = 0;
			NodeList listSentence = doc.getElementsByTagName("sentence");
			if (listSentence.getLength() > 0) {
				for (int i = 0; i < listSentence.getLength(); i++) {
					if (listSentence.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element sentence = (Element) listSentence.item(i);
						
						NodeList original = sentence.getElementsByTagName("original");
						String originalSen = null;
						if (original.getLength() != 0)
							originalSen = original.item(0).getTextContent();
						else
							throw new NullPointerException("original node missing in xml file " + textFile.getAbsolutePath());
						
						NodeList stemmed = sentence.getElementsByTagName("stemmed");
						String stemmedSen = null;
						if (stemmed.getLength() != 0)
							stemmedSen = stemmed.item(0).getTextContent();
						else
							throw new NullPointerException("stemmed node missing in xml file " + textFile.getAbsolutePath());
						
						NodeList labels = sentence.getElementsByTagName("label");
						List<String> labelsSen = new ArrayList<String>();
						if (labels.getLength() > 0)
							for (int j=0; j<labels.getLength(); j++)
								labelsSen.add(labels.item(j).getTextContent().replace("\n", ""));
						//else
						//	throw new NullPointerException("labels node missing in xml file " + textFile.getAbsolutePath());
						
						if (labelsSen.contains("highlight"))
							continue;
						SentenceModel sen = new SentenceModel(originalSen, id, text);
						sen.setLabels(labelsSen);
						nbSentence++;
						sen.setNbMot(Integer.parseInt(sentence.getAttribute("size")));
						text.add(sen);
						
						String[] word = stemmedSen.split(" ");
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
					id++;
				}
			}
			if (nbSentence == 0)
				continue;
			text.setNbSentence(nbSentence);
			c.add(text);
		}
		return c;
	}
	
	private static String fixMissingPeriod(String line) {
		if (line != null) {
			String END_TOKENS = "[\\.!?'`’”\\)]";
	
			if (line.contains("@highlight"))
				return line + " .";
			if (line.equals(""))
				return line;
			if (line.matches("[\\s\\p{Z}]"))
				return line;
			if (line.matches(".*" + END_TOKENS + "$"))
				return line;
			 return line + " .";
		}
		else
			return null;
	}
}
