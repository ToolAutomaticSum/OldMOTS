package model.task.preProcess;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import exception.LacksOfFeatures;
import model.task.preProcess.stanfordNLP.StanfordNLPSimplePreProcess;
import reader_writer.Reader;
import reader_writer.Writer;
import textModeling.Corpus;
import textModeling.MultiCorpus;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import tools.Tools;
import tools.wordFilters.WordStopListFilter;

/**
 * Generate Bag of Words
 * @author Val
 *
 */
public class GenerateTextModel extends AbstractPreProcess {

	private String stopWordListFile;
	private boolean stanford = false;
	private Lemmatizer lemm = null;
	
	protected List<AbstractPreProcess> preProcess = new ArrayList<AbstractPreProcess>();
	
	public GenerateTextModel(int id) {
		super(id);	
	}

	@Override
	public void init() throws LacksOfFeatures {

		try {
			stopWordListFile = getModel().getProcessOption(id, "StopWordListFile");
		}
		catch (LacksOfFeatures e) {
			stopWordListFile = null;
		}
		stanford = Boolean.parseBoolean(getModel().getProcessOption(id, "StanfordNLP"));
		
		// TODO remplacer preProcess par filtre � appliquer aux lignes lues --> Moins de co�t computationnel
		if (stanford) {
			preProcess.add(new StanfordNLPSimplePreProcess(id));
			lemm=(StanfordNLPSimplePreProcess)preProcess.get(0);
		}
		else {
			preProcess.add(new SentenceSplitter(id));
			preProcess.add(new WordSplitter(id));
		}
	}

	@Override
	public void process() throws Exception {
		Iterator<Corpus> corpusIt = getCurrentMultiCorpus().iterator();
		while (corpusIt.hasNext()) {
			Iterator<TextModel> textIt = corpusIt.next().iterator();
			while (textIt.hasNext()) {
				TextModel textModel = textIt.next();
				if (textModel != null) {
					loadText(textModel);
				}
			}
		}
		
		Iterator<AbstractPreProcess> it = preProcess.iterator();
		while (it.hasNext()) {
			AbstractPreProcess p = it.next();
			p.setModel(getModel());
			p.init();
		}

		it = preProcess.iterator();
		while (it.hasNext()) {
			AbstractPreProcess p = it.next();
			p.setModel(getModel());
			p.process();
		}
		
		it = preProcess.iterator();
		while (it.hasNext()) {
			AbstractPreProcess p = it.next();
			p.setModel(getModel());
			p.finish();
		}
	}

	@Override
	public void finish() {
		preProcess = null;
		
		if (stopWordListFile != null) {
			WordStopListFilter filter = new WordStopListFilter(stopWordListFile);
			for(Corpus corpus : getCurrentMultiCorpus()) {
				for(TextModel text : corpus) {
					for(SentenceModel sen : text) {
						for(WordModel word : sen)
							if(!filter.passFilter(word))
								word.setStopWord(true);
					}
				}
			}
		}
		
		new File(getModel().getOutputPath()+File.separator+"temp").mkdir();
		GenerateTextModel.writeTempDocumentBySentence(getModel().getOutputPath()+File.separator+"temp", getCurrentMultiCorpus());
	}
	
	public static boolean loadText(TextModel textModel) throws Exception {
		if (textModel.getText().equals("")) {
			File fXmlFile = new File(textModel.getDocumentFilePath());
			if (!Tools.getFileExtension(fXmlFile).equals("txt")) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
				NodeList listText =  doc.getElementsByTagName("TEXT");
				if (listText.getLength() > 0) {
					for (int i = 0; i<listText.getLength(); i++) {
						if(listText.item(i).getNodeType() == Node.ELEMENT_NODE) {
					        Element task = (Element) listText.item(i);
					        NodeList pList = task.getElementsByTagName("P");
					        if (pList.getLength() != 0) {
						        for (int j = 0; j<pList.getLength(); j++) {
						        	if(pList.item(j).getNodeType() == Node.ELEMENT_NODE) {
						        		Element p = (Element) pList.item(j);
						        		textModel.setText(textModel.getText() + p.getTextContent().replace(":",".").replace("\n", " ").replace("  ", " ") + "\n");
						        	}
						        }
					        }
					        else {
					        	textModel.setText(textModel.getText() + task.getTextContent().replace(":",".").replace("\n", " ").replace("  ", " ") + "\n");
							}
						}
					}
					return true;
				}
				else
					return false;
			}
			else {
				Reader reader = new Reader(textModel.getDocumentFilePath(), true);
				reader.open();
				String text = reader.read();
				String temp = "";
				while(text != null) {
					//System.out.println(temp.length());
					temp += text + "\n";
					text = reader.read();
				}
				textModel.setText(temp + "\n");
				System.out.println("Lecture terminée.");
				return true;
			}
		} else 
			return true;
	}

	public List<AbstractPreProcess> getPreProcess() {
		return preProcess;
	}

	public void setPreProcess(List<AbstractPreProcess> preProcess) {
		this.preProcess = preProcess;
	}
	
	public static void writeTempDocumentBySentence(String outputPath, MultiCorpus mc) {
		Iterator<Corpus> corpusIt = mc.iterator();
		while (corpusIt.hasNext()) {
			Corpus corpus = corpusIt.next();
			Iterator<TextModel> textIt = corpus.iterator();
			while (textIt.hasNext()) {
				TextModel text = textIt.next();
				new File(outputPath + File.separator + corpus.getCorpusName()).mkdir();
				Writer w = new Writer(outputPath + File.separator + corpus.getCorpusName() + File.separator + text.getTextName());
				w.open();
				
				Iterator<SentenceModel> senIt = text.iterator();
				while (senIt.hasNext()) {
					SentenceModel sen = senIt.next();
					w.write("[Sen=" + sen.toString() + File.separator + "%%" + File.separator + "NbMot="+sen.size()+"]" + sen.getSentence() + "\n");
				}
				w.close();
			}
			corpus.clear();
		}
	}
	
	public static Corpus readTempDocument(String inputPath, Corpus c, boolean readStopWords) {
		c.clear();
		File corpusDoc = new File(inputPath + File.separator + c.getCorpusName());
		for (File textFile : corpusDoc.listFiles()) {
			TextModel text = new TextModel(c, textFile.getAbsolutePath());
			int nbSentence = 0;
			Reader r = new Reader(textFile.getAbsolutePath(), true);
			r.open();
			int id=0;
			String s = r.read();
			while (s != null) {
				String[] tabs =  s.split("]");
				if (tabs.length == 2) {
					String[] label =  tabs[0].split(File.separator + "%%" + File.separator);
					//System.out.println(label[0].split("=")[1]);
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
								sen.add(wm);
							}
						}
						else {
							wm = new WordModel(w);		
							wm.setmLemma(w);	
							wm.setSentence(sen);
							sen.add(wm);
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
	
	public Lemmatizer getLemm() {
		return lemm;
	}

}
