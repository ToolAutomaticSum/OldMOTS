package model.task.postProcess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import optimize.SupportADNException;
import reader_writer.Reader;
import reader_writer.Writer;
import tools.OSDetector;
import tools.Tools;

public class EvaluationROUGE extends AbstractPostProcess {

	private String rougePath = "";
	private List<String> rougeMeasure = new ArrayList<String>();
	
	public EvaluationROUGE(int id) throws SupportADNException {
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

		if (OSDetector.isUnix()) {
			rougePath = getModel().getProcessOption(id, "RougePath");
			for (String s : getModel().getProcessOption(id, "RougePath").split("\n")) {
				rougeMeasure.add(s);
			}
		}
		
		File f = new File(getModel().getOutputPath() + File.separator + getModel().getModelRoot());
    	File[] lf = f.listFiles();
    	for (int i = 0; i<lf.length; i++) {
    		if (Tools.getFileExtension(lf[i]).equals("html"))
    			lf[i].delete();
    	}
    	
    	boolean modelWrite = false;
    	// boucle sur les process (1 résumé par process par execution)
		for (int i = 0; i<getModel().getProcess().size(); i++) {
			if (getModel().getProcess().get(i).getSummary() != null) {
				// boucle sur les multiCorpus, 1 exécution de process par multicorpus
				for (int j = 0; j<getModel().getMultiCorpusModels().size();j++) {
					writeHtmlGeneratedSummary(i, j);
					if (!modelWrite)
						writeHtmlModelSummary(j);
				}
				if (!modelWrite)
					modelWrite = true;
				writeSettingsXml(i);
			}
		}
	}

	@Override
	public void process() throws IOException {
		if (OSDetector.isUnix()) {
			for (int i = 0; i<getModel().getProcess().size(); i++) {
				@SuppressWarnings("unused")
				Process proc = Runtime.getRuntime().exec(
			        "perl " + rougePath + "/ROUGE-1.5.5.pl" + 
			        "-e " + rougePath + "/data" +
			        "-c 95 -2 -1 -U -r 1000 -n 4 -w 1.2 -a" +
			        getModel().getOutputPath() + "/settings" + i + ".xml > " + getModel().getOutputPath() + "test" + i + ".txt");
			}
		}
	}

	@Override
	public void finish() {
		if (OSDetector.isUnix()) {
			for (int i = 0; i<getModel().getProcess().size(); i++) {
				Reader r = new Reader(getModel().getOutputPath() + "test" + i + ".txt", true);
				r.open();
				String t = r.read();
				while (t != null) {
					String[] result = t.split(" ");
					if(result.length > 1 && rougeMeasure.contains(result[1]) && result[2].equals("Average_F:")) {
						currentProcess.setScore(Double.parseDouble(result[3]));
						System.out.println(result[1] + "\t" + result[2] + "\t" + result[3]);
					}
					t = r.read();
				}
			}
		}
	}
	
	private void writeHtmlGeneratedSummary(int processID, int summaryID) {
		if (getModel().getProcess().get(processID).getSummary() != null) {
			Writer w = new Writer(getModel().getOutputPath() + File.separator + getModel().getPeerRoot() + File.separator + "T" + getModel().getTaskID() + "_" + String.valueOf(processID/*getModel().getProcess().get(i).getId()*/)+ "_" + String.valueOf(summaryID) + ".html");
			w.open();
			w.write("<html>\n<head><title>" + String.valueOf(processID/*getModel().getProcess().get(i).getId()*/) + "</title></head>" +
			"<body bgcolor=\"white\">\n");
			for (int j = 0; j<getModel().getProcess().get(processID).getSummary().get(summaryID).size(); j++) {
				w.write("<a name=\""+ String.valueOf(j) + "\">[" + String.valueOf(j) + "]</a> <a href=\"#" + String.valueOf(j) + 
						"\" id=" + String.valueOf(j) + ">" + getModel().getProcess().get(processID).getSummary().get(summaryID).get(j).getSentence() + "</a>\n");
			}
			w.write("</body>\n</html>");
			w.close();
		}
	}
	
	private void writeHtmlModelSummary(int summaryID) {
		for (String modelSummary : getModel().getCurrentMultiCorpus().get(summaryID).getSummaryNames()) {
    		Writer w = new Writer(getModel().getOutputPath() + File.separator + getModel().getModelRoot() + File.separator + modelSummary.replace(".txt", "") + ".html");
			w.open();
			w.write("<html>\n<head><title>" + modelSummary.replace(".txt", "") + ".html" + "</title></head>" +
			"<body bgcolor=\"white\">\n");
			Reader r = new Reader( getModel().getCurrentMultiCorpus().get(summaryID).getSummaryPath() + File.separator + modelSummary, true);
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
	    
	    for (int i = 0; i<getModel().getCurrentMultiCorpus().size(); i++) {
	    	Element process = document.createElement("EVAL");
	    	process.setAttribute("ID", "CORPUS_" + i);
	    	//process.appendChild(document.createTextNode(getModel().getProcess().get(i).getClass().toString()));
	    	
	    	Element modelRoot = document.createElement("MODEL-ROOT");
	    	modelRoot.appendChild(document.createTextNode(/*getModel().getOutputPath() + File.separator +*/ "/cygdrive/g/theseWorkspace/AutomaticSummarization/doc/Output/" + getModel().getModelRoot()));
	    	process.appendChild(modelRoot);
	    	
	    	Element peerRoot = document.createElement("PEER-ROOT");
	    	peerRoot.appendChild(document.createTextNode(/*getModel().getOutputPath() + File.separator +*/ "/cygdrive/g/theseWorkspace/AutomaticSummarization/doc/Output/" + getModel().getPeerRoot()));
	    	process.appendChild(peerRoot);
	    	
	    	Element inputFormat = document.createElement("INPUT-FORMAT");
	    	inputFormat.setAttribute("TYPE", "SEE");
	    	process.appendChild(inputFormat);
	    	
	    	Element peers = document.createElement("PEERS");
	    	//for (int j = 0; j<getModel().getProcess().size(); j++) {
	    	//for (int j = 0; j<getModel().getProcess().get(processID).getSummary().size();j++) {
		    	Element generatedSummary = document.createElement("P");
	    		generatedSummary.setAttribute("ID", String.valueOf(processID/*getModel().getProcess().get(j).getId()*/));
	    		generatedSummary.appendChild(document.createTextNode("T" + getModel().getTaskID() + "_" + String.valueOf(processID/*getModel().getProcess().get(j).getId()*/)+ "_" + i + ".html"));
	    		peers.appendChild(generatedSummary);
			//}
	    	//}
	    	process.appendChild(peers);

	    	Element models = document.createElement("MODELS");
	    	
	    	/*File f = new File(getModel().getOutputPath() + File.separator + getModel().getModelRoot());
	    	File[] lf = f.listFiles();
	    	for (int j = 0; j<lf.length; j++) {
	    		if (Tools.getFileExtension(lf[j]).equals("html")) {*/
	    	for (int j = 0; j<getModel().getCurrentMultiCorpus().get(i).getSummaryNames().size();j++) {
		    		Element modelSummary = document.createElement("M");
		    		modelSummary.setAttribute("ID", String.valueOf(i) + String.valueOf(j));
		    		modelSummary.appendChild(document.createTextNode(getModel().getCurrentMultiCorpus().get(i).getSummaryNames().get(j).replace(".txt", "") + ".html"));
		    		models.appendChild(modelSummary);
	    	}
	    		/*}
	    	}*/
	    	process.appendChild(models);
	    	
	    	rootNode.appendChild(process);
	    }
	    	TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    	Transformer transformer = transformerFactory.newTransformer();
	    	DOMSource source = new DOMSource(document);
	    	StreamResult sortie = new StreamResult(new File(getModel().getOutputPath() + File.separator + "settings" + getModel().getTaskID() + processID + ".xml"));
	    	transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
	    	transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    	transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
	    	
	    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    	transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	    	
	    	transformer.transform(source, sortie);	

	}
}
