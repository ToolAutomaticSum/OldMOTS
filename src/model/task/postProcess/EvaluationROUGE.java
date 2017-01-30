package model.task.postProcess;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import exception.LacksOfFeatures;
import reader_writer.Reader;
import reader_writer.Writer;
import tools.Tools;

public class EvaluationROUGE extends AbstractPostProcess {

	public EvaluationROUGE(int id) {
		super(id);
	}

	/**
	 * Ecrit le fichier system.xml nécessaire au fonction de ROUGE
	 * ainsi que les différents résumé sous forme de html.
	 * @throws TransformerException 
	 * @throws ParserConfigurationException 
	 */
	@Override
	public void init() throws LacksOfFeatures, ParserConfigurationException, TransformerException {

		File f = new File(getModel().getOutputPath() + "\\" + getModel().getModelRoot());
    	File[] lf = f.listFiles();
    	for (int i = 0; i<lf.length; i++) {
    		if (Tools.getFileExtension(lf[i]).equals("html"))
    			lf[i].delete();
    	}
    	
		for (int i = 0; i<getModel().getProcess().size(); i++) {
			for (int j = 0; j<getModel().getProcess().get(i).getSummary().size();j++) {
				writeHtmlGeneratedSummary(i, j);
				if (i == 0)
					writeHtmlModelSummary(j);
			}
			writeSettingsXml(i);
		}
	}

	@Override
	public void process() {
		/*String script ="#!/usr/bin/perl -w\n"
				+ "$cmd=\"./ROUGE-1.5.5.pl -e data -c 95 -2 -1 -U -r 1000 -n 4 -w 1.2 -a LDA/settings.xml\""
				+ "print $cmd,\"\\n\";\n"
				+ "system($cmd);\n";
		Writer w = new Writer("doc\\LDA\\scriptRouge.pl");
		w.open();
		w.write(script);
		w.close();*/
		//Tools.copyFile("G:\\theseWorkspace\\AutomaticSummarization\\doc\\LDA", "lib\\ROUGE-1.5.5\\RELEASE-1.5.5");
	}

	@Override
	public void finish() {
	}
	
	private void writeHtmlGeneratedSummary(int processID, int summaryID) {
		//for (int i = 0; i<getModel().getProcess().size(); i++) {
			Writer w = new Writer(getModel().getOutputPath() + "\\" + getModel().getPeerRoot() + "\\T" + getModel().getTaskID() + "_" + String.valueOf(processID/*getModel().getProcess().get(i).getId()*/)+ "_" + String.valueOf(summaryID) + ".html");
			w.open();
			w.write("<html>\n<head><title>" + String.valueOf(processID/*getModel().getProcess().get(i).getId()*/) + "</title></head>" +
			"<body bgcolor=\"white\">\n");
			for (int j = 0; j<getModel().getProcess().get(processID).getSummary().get(summaryID).size(); j++) {
				w.write("<a name=\""+ String.valueOf(j) + "\">[" + String.valueOf(j) + "]</a> <a href=\"#" + String.valueOf(j) + 
						"\" id=" + String.valueOf(j) + ">" + getModel().getProcess().get(processID).getSummary().get(summaryID).get(j).getSentence() + "</a>\n");
			}
			w.write("</body>\n</html>");
			w.close();
		//}
	}
	
	private void writeHtmlModelSummary(int summaryID) {
		for (String modelSummary : getModel().getCorpusModels().get(summaryID).getSummaryNames()) {
    		Writer w = new Writer(getModel().getOutputPath() + "\\" + getModel().getModelRoot() + "\\" + modelSummary.replace(".txt", ".html"));
			w.open();
			w.write("<html>\n<head><title>" + modelSummary.replace(".txt", ".html") + "</title></head>" +
			"<body bgcolor=\"white\">\n");
			Reader r = new Reader( getModel().getCorpusModels().get(summaryID).getSummaryPath() + "\\" + modelSummary, true);
			r.open();
			int j = 0;
			String text = r.read();
	        while (text != null) {
				w.write("<a name=\""+ String.valueOf(j) + "\">[" + String.valueOf(j) + "]</a> <a href=\"#" + String.valueOf(j) + 
						"\" id=" + String.valueOf(j) + ">" + text + "</a>\n");
				text = r.read();
				j++;
	        }
	        r.close();
			w.write("</body>\n</html>");
			w.close();
    	}
	}
	
	private void writeSettingsXml(int processID) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document document= builder.newDocument();
	    
	    Element rootNode = document.createElement("ROUGE_EVAL");
	    rootNode.setAttribute("version", "1.55");
	    document.appendChild(rootNode);
	    
	    //for (int i = 0; i<getModel().getProcess().size(); i++) {
	    	Element process = document.createElement("EVAL");
	    	process.setAttribute("ID", "TASK_" + getModel().getTaskID());
	    	//process.appendChild(document.createTextNode(getModel().getProcess().get(i).getClass().toString()));
	    	
	    	Element modelRoot = document.createElement("MODEL-ROOT");
	    	modelRoot.appendChild(document.createTextNode(/*getModel().getOutputPath() + "\\" +*/ "/cygdrive/g/theseWorkspace/AutomaticSummarization/doc/Output/" + getModel().getModelRoot()));
	    	process.appendChild(modelRoot);
	    	
	    	Element peerRoot = document.createElement("PEER-ROOT");
	    	peerRoot.appendChild(document.createTextNode(/*getModel().getOutputPath() + "\\" +*/ "/cygdrive/g/theseWorkspace/AutomaticSummarization/doc/Output/" + getModel().getPeerRoot()));
	    	process.appendChild(peerRoot);
	    	
	    	Element inputFormat = document.createElement("INPUT-FORMAT");
	    	inputFormat.setAttribute("TYPE", "SEE");
	    	process.appendChild(inputFormat);
	    	
	    	Element peers = document.createElement("PEERS");
	    	//for (int j = 0; j<getModel().getProcess().size(); j++) {
	    	for (int j = 0; j<getModel().getProcess().get(processID).getSummary().size();j++) {
		    	Element generatedSummary = document.createElement("P");
	    		generatedSummary.setAttribute("ID", String.valueOf(j/*getModel().getProcess().get(j).getId()*/));
	    		generatedSummary.appendChild(document.createTextNode("T" + getModel().getTaskID() + "_" + String.valueOf(processID/*getModel().getProcess().get(j).getId()*/)+ "_" + String.valueOf(j) + ".html"));
	    		peers.appendChild(generatedSummary);
			}
	    	//}
	    	process.appendChild(peers);

	    	Element models = document.createElement("MODELS");
	    	File f = new File(getModel().getOutputPath() + "\\" + getModel().getModelRoot());
	    	File[] lf = f.listFiles();
	    	for (int j = 0; j<lf.length; j++) {
	    		if (Tools.getFileExtension(lf[j]).equals("html")) {
		    		Element modelSummary = document.createElement("M");
		    		modelSummary.setAttribute("ID", String.valueOf(j));
		    		modelSummary.appendChild(document.createTextNode(lf[j].getName()));
		    		models.appendChild(modelSummary);
	    		}
	    	}
	    	process.appendChild(models);
	    	
	    	rootNode.appendChild(process);
	    //}
	    	TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    	Transformer transformer = transformerFactory.newTransformer();
	    	DOMSource source = new DOMSource(document);
	    	StreamResult sortie = new StreamResult(new File(getModel().getOutputPath() + "\\settings" + getModel().getTaskID() + processID + ".xml"));
	    	transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
	    	transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    	transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
	    	
	    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    	transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	    	
	    	transformer.transform(source, sortie);	

	}
}
