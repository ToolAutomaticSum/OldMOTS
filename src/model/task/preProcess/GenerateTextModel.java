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
import exception.SizeException;
import model.task.preProcess.stanfordNLP.StanfordNLPSimplePreProcess;
import reader_writer.Reader;
import reader_writer.Writer;
import textModeling.ParagraphModel;
import textModeling.SentenceModel;
import textModeling.TextModel;
import textModeling.WordModel;
import tools.Tools;

/**
 * Generate Bag of Words
 * @author Val
 *
 */
public class GenerateTextModel extends AbstractPreProcess {

	private int nbSentence = 0;
	
	private int limitSize = 50000000;
	private boolean liveProcess = false;
	private boolean stanford = false;
	
	protected List<AbstractPreProcess> preProcess = new ArrayList<AbstractPreProcess>();
	
	public GenerateTextModel(int id) {
		super(id);	
	}

	@Override
	public void init() throws LacksOfFeatures {
		liveProcess = Boolean.parseBoolean(getModel().getProcessOption(id, "LiveProcess"));
		limitSize = Integer.parseInt(getModel().getProcessOption(id, "LimitSize"));
		stanford = Boolean.parseBoolean(getModel().getProcessOption(id, "StanfordNLP"));
		
		// TODO remplacer preProcess par filtre à appliquer aux lignes lues --> Moins de coût computationnel
		preProcess.add(new ParagraphSplitter(getModel().getCtrl().incrementProcessID()));
		if (stanford)
			preProcess.add(new StanfordNLPSimplePreProcess(getModel().getCtrl().incrementProcessID()));
		else {
			preProcess.add(new SentenceSplitter(getModel().getCtrl().incrementProcessID()));
			preProcess.add(new WordSplitter(getModel().getCtrl().incrementProcessID()));
		}
	}
	
	@Override
	public void process() throws Exception {
		if (liveProcess)
			liveProcess();
		else
			normalProcess();
	}
	
	@Override
	public void finish() {
	}
	
	/**
	 * If file temp.txt exists, do nothing,
	 * else, write it reading inputTextModel and doing preProcess
	 * @throws Exception
	 */
	private void liveProcess() throws Exception {
		StopWordsRemover stopWordsProcess = null;
		TextStemming textStemmer = null;
		
		/**
		 * Initializing preProcess
		 */
		Iterator<AbstractPreProcess> preProcIt = getModel().getPreProcess().iterator();
		while (preProcIt.hasNext()) {
			AbstractPreProcess p = preProcIt.next();
			if (p.getClass().equals(StopWordsRemover.class)) {
				stopWordsProcess = (StopWordsRemover) p;
				p.setModel(getModel());
				p.setCurrentProcess(getCurrentProcess());
				p.init();
			}
			else if (p.getClass().equals(TextStemming.class)) {
				textStemmer = (TextStemming) p;
				p.setModel(getModel());
				p.setCurrentProcess(getCurrentProcess());
				p.init();
			}
		}
		
		//int i = 0;
		nbSentence = 0;
		Iterator<TextModel> textIt = getModel().getDocumentModels().iterator();
		while (textIt.hasNext()) {
			TextModel textModel = textIt.next();
			
			//BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getModel().getOutputPath() + "\\modelLDA\\temp" + i + ".txt")));

			Reader r = new Reader(textModel.getDocumentFilePath(), true);
			r.open();
			String text = r.read();

			while (text != null)
	        {
				ParagraphModel paragraph = new ParagraphModel("", textModel);
				liveParagraphProcess(paragraph, text, stopWordsProcess, textStemmer);
				textModel.add(paragraph);
				textModel.setNbSentence(paragraph.getNbSentence()+textModel.getNbSentence());
				text = r.read();
	        }
			//w.close();
			//i++;
		}
	}
	
	private void normalProcess() throws Exception {
		Iterator<TextModel> textIt = getModel().getDocumentModels().iterator();
		while (textIt.hasNext()) {
			TextModel textModel = textIt.next();
			if (textModel != null) {
				loadText(textModel, limitSize);
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
	
	public static boolean loadText(TextModel textModel, int limitSize) throws Exception {
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
						        		textModel.setText(textModel.getText() + p.getTextContent().replace("\n", " ").replace("  ", " ") + "\n");
						        	}
						        }
					        }
					        else {
					        	textModel.setText(textModel.getText() + task.getTextContent().replace("\n", "").replace("   ", "\n").replace("  ", " ") + "\n");
							}
						}
					}
					
					/*String[] listLine = task.getTextContent().split("\n");
					textModel.setText("");
					for (int i=0; i<listLine.length; i++) {
						if (!listLine[i].contains("<") && !listLine[i].contains(">")) {
							listLine[i] = listLine[i].replace("\n", "");
							//listLine[i] = listLine[i].replace("  ", "");
							String str = listLine[i];
							//System.out.println(str.trim());
							if (!listLine[i].trim().isEmpty()) 
								textModel.setText(textModel.getText() + listLine[i].trim() + " ");
							else
								textModel.setText(textModel.getText() + "\n");
						}
					}*/
					return true;
				}
				else
					return false;
			}
			else {
				Reader r = new Reader(textModel.getDocumentFilePath(), true);
				r.open();
				//textModel.setTextSize(r.size());
				if (textModel.getTextSize() > limitSize) {
					r.close();
					cutText(textModel, limitSize);
					throw new SizeException(textModel.getDocumentFilePath());
				} else {
					String text = r.read();
					textModel.setText("");
					while (text != null) {
						textModel.setText(textModel.getText()+ text + "\n");
						text = r.read();
					}
					r.close();
					return true;
				}
			}
		} else 
			return true;
	}
	
	public static List<TextModel> cutText (TextModel textModel, int limitSize) {
		List<TextModel> list = new ArrayList<TextModel>();
		int currentDoc = 1;
		//TextModel tm = new TextModel(textModel.getDocumentFilePath() + currentDoc);
		
		Writer w = new Writer(textModel.getDocumentFilePath() + "Cut//" + currentDoc + ".txt");
		w.open();
		Reader r = new Reader(textModel.getDocumentFilePath(), true);
		r.open();
		String text = r.read();
		//tm.setText(text);
		int readSize = text.length();
		while (text != null)
        {
			w.write(text + "\n");
			text = r.read();
			//tm.setText(tm.getText()+text);
        	if (text != null)
        		readSize +=	text.length();
        	if (readSize > limitSize) {
        		//TextModel tm = new TextModel(textModel.getDocumentFilePath() + currentDoc);
        		//tm.setText(text);
        		//list.add(tm);
        		readSize = text.length();
        		w.close();
        		w.open(textModel.getDocumentFilePath() + "Cut//" + (++currentDoc) + ".txt");
        	}
        }
		w.close();
		return list;
	}
	
	private ParagraphModel liveParagraphProcess(ParagraphModel paragraph, String text, StopWordsRemover stopWordsProcess, TextStemming textStemmer) {
		int lastNbSentence = nbSentence;
		List<String> listOfSentence = SentenceSplitter.splitTextIntoSentence(text);
		
		Iterator<String> senIt = listOfSentence.iterator();
		while (senIt.hasNext()) {
			String sentence = senIt.next();
			if (!sentence.equals("")) {
				SentenceModel sen = new SentenceModel(sentence, nbSentence, paragraph);
				int wordId = 0;
				List<String> listOfWord = WordSplitter.splitSentenceIntoWord(sentence);
				List<String> listOfGramWord = stopWordsProcess.returnListOfGramWord(listOfWord);
				Iterator<String> wordIt = listOfWord.iterator();
				while (wordIt.hasNext()) {
					String word = wordIt.next();
					String stemmWord = textStemmer.stemming(word);
					WordModel wm = new WordModel(String.valueOf(wordId), word, stemmWord, "", "");
					wm.setWord(word);
					if (listOfGramWord.contains(word))
						wm.setStopWord(true);
					sen.add(wm);
					//returnText += word + " ";
					wordId++;
				}
				paragraph.add(sen);
				nbSentence++;
			}
		}
		paragraph.setNbSentence(nbSentence - lastNbSentence);
		return paragraph;
	}

	public List<AbstractPreProcess> getPreProcess() {
		return preProcess;
	}

	public void setPreProcess(List<AbstractPreProcess> preProcess) {
		this.preProcess = preProcess;
	}
}
