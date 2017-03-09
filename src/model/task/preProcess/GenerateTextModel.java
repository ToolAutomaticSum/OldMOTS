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
import textModeling.Corpus;
import textModeling.TextModel;
import tools.Tools;

/**
 * Generate Bag of Words
 * @author Val
 *
 */
public class GenerateTextModel extends AbstractPreProcess {

	private boolean stanford = false;
	
	protected List<AbstractPreProcess> preProcess = new ArrayList<AbstractPreProcess>();
	
	public GenerateTextModel(int id) {
		super(id);	
	}

	@Override
	public void init() throws LacksOfFeatures {
		stanford = Boolean.parseBoolean(getModel().getProcessOption(id, "StanfordNLP"));
		
		// TODO remplacer preProcess par filtre � appliquer aux lignes lues --> Moins de co�t computationnel
		preProcess.add(new ParagraphSplitter(getModel().getCtrl().incrementProcessID()));
		if (stanford)
			preProcess.add(new StanfordNLPSimplePreProcess(getModel().getCtrl().incrementProcessID()));
		else {
			preProcess.add(new SentenceSplitter(getModel().getCtrl().incrementProcessID()));
			preProcess.add(new WordSplitter(getModel().getCtrl().incrementProcessID()));
		}
	}
	
	@Override
	public void finish() {
	}

	@Override
	public void process() throws Exception {
		Iterator<Corpus> corpusIt = getModel().getCurrentMultiCorpus().iterator();
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
						        		textModel.setText(textModel.getText() + p.getTextContent().replace("\n", " ").replace("  ", " ") + "\n");
						        	}
						        }
					        }
					        else {
					        	textModel.setText(textModel.getText() + task.getTextContent().replace("\n", "").replace("   ", "\n").replace("  ", " ") + "\n");
							}
						}
					}
					return true;
				}
				else
					return false;
			}
			else
				return false;
		} else 
			return true;
	}

	public List<AbstractPreProcess> getPreProcess() {
		return preProcess;
	}

	public void setPreProcess(List<AbstractPreProcess> preProcess) {
		this.preProcess = preProcess;
	}
}
