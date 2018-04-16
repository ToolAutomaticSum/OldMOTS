package main.java.liasd.asadera.model;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import main.java.liasd.asadera.exception.LacksOfFeatures;
import main.java.liasd.asadera.model.task.preProcess.AbstractPreProcess;
import main.java.liasd.asadera.model.task.process.AbstractProcess;
import main.java.liasd.asadera.model.task.process.ComparativeProcess;
import main.java.liasd.asadera.textModeling.MultiCorpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.tools.Pair;

public class ComparativeModel extends AbstractModel {

	@Override
	public void run() {
		try {
			loadMultiCorpusModels();

			Iterator<AbstractPreProcess> preProIt = getPreProcess().iterator();
			while (preProIt.hasNext()) {
				AbstractPreProcess p = preProIt.next();
				p.setModel(this);
				p.init();
			}

			Iterator<MultiCorpus> multiCorpusIt = getMultiCorpusModels().iterator();
			while (multiCorpusIt.hasNext()) {
				currentMultiCorpus = multiCorpusIt.next();
				preProIt = getPreProcess().iterator();
				while (preProIt.hasNext()) {
					AbstractPreProcess p = preProIt.next();
					p.setCurrentMultiCorpus(currentMultiCorpus);
					p.process();
				}
				preProIt = getPreProcess().iterator();
				while (preProIt.hasNext()) {
					AbstractPreProcess p = preProIt.next();
					p.finish();
				}

				System.out.println(currentMultiCorpus);
			}

			multiCorpusIt = getMultiCorpusModels().iterator();
			while (multiCorpusIt.hasNext()) {
				currentMultiCorpus = multiCorpusIt.next();
				System.out.println("MultiCorpus : " + currentMultiCorpus.getiD());

				Iterator<AbstractProcess> proIt = getProcess().iterator();
				while (proIt.hasNext()) {
					long time = System.currentTimeMillis();
					ComparativeProcess p = (ComparativeProcess) proIt.next();
					p.setModel(this);
					p.setCurrentMultiCorpus(currentMultiCorpus);
					p.initCorpusToCompress();
					p.initADN();
					runProcess(currentMultiCorpus, p);
					System.out.println(System.currentTimeMillis() - time);
				}
			}
		} catch (LacksOfFeatures e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param multiCorpus
	 * @param p
	 * @throws Exception
	 */
	public void runProcess(MultiCorpus multiCorpus, ComparativeProcess p) throws Exception {
		p.init();
		p.process();
		String t = "MultiCorpus " + multiCorpus.getiD() + "\n" + buildDifferenceFile(p.getSummary());
		setChanged();
		notifyObservers(t);
		p.finish();
	}

	private String buildDifferenceFile(List<Pair<SentenceModel, String>> summary) throws ParserConfigurationException {
		Set<String> setLabels = new TreeSet<String>();

		for (Pair<SentenceModel, String> p : summary)
			setLabels.addAll(p.getKey().getLabels());

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();

		Element rootNode = document.createElement("SUMMARY");
		document.appendChild(rootNode);

		Element labels = document.createElement("LABELS");
		for (String l : setLabels) {
			Element label = document.createElement("LABEL");
			label.appendChild(document.createTextNode(l));

			labels.appendChild(label);
		}

		rootNode.appendChild(labels);

		for (Pair<SentenceModel, String> p : summary) {
			Element sentence = document.createElement(p.getValue());
			sentence.appendChild(document.createTextNode(p.getKey().toString()));
			String t = "";
			for (String l : p.getKey().getLabels())
				t += l + ", ";
			sentence.setAttribute("type", t.substring(0, t.length() - 2));
			sentence.setAttribute("doc_id", String.valueOf((p.getKey().getText().getiD())));
			sentence.setAttribute("sen_id", String.valueOf(p.getKey().getiD()));
			rootNode.appendChild(sentence);
		}

		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StringWriter writer = new StringWriter();
			StreamResult sortie = new StreamResult(writer);
			transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			transformer.transform(source, sortie);
			return writer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
