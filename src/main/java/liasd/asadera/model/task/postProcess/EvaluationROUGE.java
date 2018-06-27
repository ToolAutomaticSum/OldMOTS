package main.java.liasd.asadera.model.task.postProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import main.java.liasd.asadera.exception.LacksOfFeatures;
import main.java.liasd.asadera.model.task.process.SummarizeProcess;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.tools.OSDetector;
import main.java.liasd.asadera.tools.reader_writer.Reader;
import main.java.liasd.asadera.tools.reader_writer.Writer;

public class EvaluationROUGE extends AbstractPostProcess {
	
	private static Logger logger = LoggerFactory.getLogger(EvaluationROUGE .class);

	private String rougePath = System.getenv("ROUGE_HOME");
	private List<String> rougeMeasure = new ArrayList<String>();
	protected String modelRoot;
	protected String peerRoot;
	protected String rougeTempFilePath;

	public EvaluationROUGE(int id) throws SupportADNException {
		super(id);
	}

	/**
	 * Write setting.xml for ROUGE evaluation
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 */
	@Override
	public void init() throws LacksOfFeatures, ParserConfigurationException, TransformerException, Exception {
		rougeTempFilePath = getModel().getOutputPath() + File.separator + getModel().getName();
		new File(rougeTempFilePath + File.separator + peerRoot).mkdirs();
		new File(rougeTempFilePath + File.separator + modelRoot).mkdirs();

		boolean modelWrite = false;
		for (int i = 0; i < getModel().getProcess().size(); i++) {
			if (((SummarizeProcess) getModel().getProcess().get(i)).getSummary() != null) {
				for (int j = 0; j < getModel().getMultiCorpusModels().size(); j++) {
					for (int k : getModel().getProcess().get(i).getListCorpusId()) {
						writeHtmlGeneratedSummary(i, j, k);
						if (!modelWrite)
							writeHtmlModelSummary(j, k);
					}
				}
				if (!modelWrite)
					modelWrite = true;
				writeSettingsXml(i);
			}
		}
	}

	@Override
	public void process() throws Exception {
		if (OSDetector.isUnix()) {
			for (int i = 0; i < getModel().getProcess().size(); i++) {
				String cmd = "perl " + rougePath + File.separator + "ROUGE-1.5.5.pl" + " -e " + rougePath
						+ File.separator + "data -n 2 -x -m -c 95 -r 1000 -f A -p 0.5 -t 0 -a " + rougeTempFilePath
						+ File.separator + "settings" + getModel().getTaskID() + i + ".xml";
				logger.trace(cmd);
				System.out.println("");
				Process proc = Runtime.getRuntime().exec(cmd);

				BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));

				Writer w = new Writer(
						rougeTempFilePath + File.separator + "rouge_result" + getModel().getTaskID() + i + ".txt");
				w.open(false);
				String line = "";
				while ((line = input.readLine()) != null)
					w.write(line + "\n");

				w.close();
				proc.waitFor();
			}
		}
	}

	@Override
	public void finish() throws Exception {
		if (OSDetector.isUnix()) {
			for (int i = 0; i < getModel().getProcess().size(); i++) {
				Reader r = new Reader(rougeTempFilePath + File.separator + "rouge_result" + getModel().getTaskID() + i + ".txt",
						true);
				r.open();
				String t = r.read();
				while (t != null) {
					System.out.println(t);
					/*String[] result = t.split(" ");
					if (result.length > 1 && rougeMeasure.contains(result[1]) && result[2].equals("Average_R:")) {
						if (result[1].equals("ROUGE-2"))
							getModel().getProcess().get(i).setScore(Double.parseDouble(result[3]));
						System.out.println(result[1] + "\t" + result[2] + "\t" + result[3]);
					}*/
					t = r.read();
				}
				r.close();
			}
		}
	}

	private void writeHtmlGeneratedSummary(int processID, int multiCorpusId, int corpusId) throws Exception {
		if (((SummarizeProcess) getModel().getProcess().get(processID)).getSummary().get(multiCorpusId)
				.get(corpusId) != null) {
			Writer w = new Writer(rougeTempFilePath + File.separator + peerRoot + File.separator + "T"
					+ getModel().getTaskID() + "_" + processID + "_" + multiCorpusId + "_" + corpusId + ".html");
			w.open(false);
			w.write("<html>\n<head><title>" + processID + "</title></head>" + "<body bgcolor=\"white\">\n");
			for (int j = 0; j < ((SummarizeProcess) getModel().getProcess().get(processID)).getSummary()
					.get(multiCorpusId).get(corpusId).size(); j++) {
				w.write("<a name=\"" + String.valueOf(j) + "\">[" + String.valueOf(j) + "]</a> <a href=\"#"
						+ String.valueOf(j) + "\" id=" + String.valueOf(j) + ">"
						+ ((SummarizeProcess) getModel().getProcess().get(processID)).getSummary().get(multiCorpusId)
								.get(corpusId).get(j).toString()
						+ "</a>\n");
			}
			w.write("</body>\n</html>");
			w.close();
		} else
			logger.error("Summary corpus " + corpusId + " MultiCorpus " + multiCorpusId + " is null.");
	}

	private void writeHtmlModelSummary(int multiCorpusId, int corpusId) throws Exception {
		for (String modelSummary : getModel().getMultiCorpusModels().get(multiCorpusId).get(corpusId)
				.getSummaryNames()) {
			Writer w = new Writer(rougeTempFilePath + File.separator + modelRoot + File.separator
					+ modelSummary.replace(".txt", "") + ".html");
			w.open(false);
			w.write("<html>\n<head><title>" + modelSummary.replace(".txt", "") + ".html" + "</title></head>"
					+ "<body bgcolor=\"white\">\n");
			Reader r = new Reader(getModel().getMultiCorpusModels().get(multiCorpusId).get(corpusId).getSummaryPath()
					+ File.separator + modelSummary, true);
			r.open();
			int j = 0;
			String text = r.read();
			while (text != null) {
				w.write("<a name=\"" + String.valueOf(j) + "\">[" + String.valueOf(j) + "]</a> <a href=\"#"
						+ String.valueOf(j) + "\" id=" + String.valueOf(j) + ">" + text + "</a>\n");
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
		Document document = builder.newDocument();

		Element rootNode = document.createElement("ROUGE_EVAL");
		rootNode.setAttribute("version", "1.55");
		document.appendChild(rootNode);

		for (int multiCorpusId = 0; multiCorpusId < getModel().getMultiCorpusModels().size(); multiCorpusId++) {
			for (int corpusId : getModel().getProcess().get(processID).getListCorpusId()) {
				Element process = document.createElement("EVAL");
				process.setAttribute("ID", "CORPUS_" + corpusId);
				Element modelRoot = document.createElement("MODEL-ROOT");
				modelRoot.appendChild(document.createTextNode(rougeTempFilePath + File.separator + this.modelRoot));
				process.appendChild(modelRoot);

				Element peerRoot = document.createElement("PEER-ROOT");
				peerRoot.appendChild(document.createTextNode(rougeTempFilePath + File.separator + this.peerRoot));
				process.appendChild(peerRoot);

				Element inputFormat = document.createElement("INPUT-FORMAT");
				inputFormat.setAttribute("TYPE", "SEE");
				process.appendChild(inputFormat);

				Element peers = document.createElement("PEERS");

				Element generatedSummary = document.createElement("P");
				generatedSummary.setAttribute("ID",
						String.valueOf(processID));
				generatedSummary.appendChild(document.createTextNode("T" + getModel().getTaskID() + "_" + processID
						+ "_" + multiCorpusId + "_" + corpusId + ".html"));
				peers.appendChild(generatedSummary);

				process.appendChild(peers);

				Element models = document.createElement("MODELS");

				for (String modelSummaryName : getModel().getMultiCorpusModels().get(multiCorpusId).get(corpusId)
						.getSummaryNames()) {
					Element modelSummary = document.createElement("M");
					modelSummary.setAttribute("ID", String.valueOf(multiCorpusId) + String.valueOf(corpusId));
					modelSummary.appendChild(document.createTextNode(modelSummaryName.replace(".txt", "") + ".html"));
					models.appendChild(modelSummary);
				}

				process.appendChild(models);

				rootNode.appendChild(process);
			}
		}
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(document);
		StreamResult sortie = new StreamResult(new File(
				rougeTempFilePath + File.separator + "settings" + getModel().getTaskID() + processID + ".xml"));
		transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		transformer.transform(source, sortie);
	}

	public static void inheritIO(final InputStream src, final PrintStream dest) throws InterruptedException {
		new Thread(new Runnable() {
			public void run() {
				Scanner sc = new Scanner(src);
				while (sc.hasNextLine()) {
					dest.println(sc.nextLine());
				}
				sc.close();
			}
		}).join();
	}

//	public String getRougePath() {
//		return rougePath;
//	}
//
//	public void setRougePath(String rougePath) {
//		this.rougePath = rougePath;
//	}

	public List<String> getRougeMeasure() {
		return rougeMeasure;
	}

	public void setRougeMeasure(List<String> rougeMeasure) {
		this.rougeMeasure = rougeMeasure;
	}

	public String getModelRoot() {
		return modelRoot;
	}

	public void setModelRoot(String modelRoot) {
		this.modelRoot = modelRoot;
	}

	public String getPeerRoot() {
		return peerRoot;
	}

	public void setPeerRoot(String peerRoot) {
		this.peerRoot = peerRoot;
	}
}
