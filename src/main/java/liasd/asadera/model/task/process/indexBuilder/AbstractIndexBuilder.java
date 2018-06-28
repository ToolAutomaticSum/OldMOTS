package main.java.liasd.asadera.model.task.process.indexBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.liasd.asadera.exception.LacksOfFeatures;
import main.java.liasd.asadera.model.task.process.AbstractProcess;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;
import main.java.liasd.asadera.tools.reader_writer.Reader;

public abstract class AbstractIndexBuilder<T extends WordIndex> extends ParameterizedMethod
		implements IndexBasedOut<T>, ListSentenceBasedOut {

	private static Logger logger = LoggerFactory.getLogger(AbstractIndexBuilder.class);

	private AbstractProcess currentProcess;
	protected final Index<T> index;

	private String listIdSenPath;
	protected List<SentenceModel> listSen;

	public AbstractIndexBuilder(int id) throws SupportADNException {
		super(id);
		index = new Index<T>();
		
		listParameterOut.add(new ParameterizedType(SentenceModel.class, List.class, ListSentenceBasedOut.class));
	}

	public AbstractIndexBuilder(int id, List<ParameterizedType> in, List<ParameterizedType> out)
			throws SupportADNException {
		super(id, in, out);
		index = new Index<T>();
		listSen = new ArrayList<SentenceModel>();
	}

	public abstract AbstractIndexBuilder<T> makeCopy() throws Exception;

	protected void initCopy(AbstractIndexBuilder<T> p) {
		p.setCurrentProcess(currentProcess);
		p.setSupportADN(new HashMap<String, Class<?>>(supportADN));
		p.setModel(model);
	}

	public void initADN() throws Exception {
		listSen = new ArrayList<SentenceModel>();
	}

	public void processIndex(List<Corpus> listCorpus) throws Exception {
		try {
			listIdSenPath = getModel().getProcessOption(id, "ListCompressedSenPath");
		} catch (LacksOfFeatures lof) {
			listIdSenPath = "all";
		}
		if (listIdSenPath.equals("all"))
			for (Corpus corpus : listCorpus)
				listSen.addAll(corpus.getAllSentence());
		else
			for (Corpus corpus : listCorpus) {
				File[] folder = new File(corpus.getInputPath() + File.separator + listIdSenPath).listFiles();
				for (int i = 0; i < folder.length; i++) {
					Reader reader = new Reader(folder[i].getPath(), true);
					reader.open();
					String line;
					while ((line = reader.read()) != null)
						try {
							listSen.add(corpus.getSentenceByID(Integer.parseInt(line)));
						} catch (Exception e) {
							logger.error(folder[i].getPath());
						}
					reader.close();
				}
			}
	}

	public void finish() {
		index.clear();
	}

	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(AbstractProcess currentProcess) {
		this.currentProcess = currentProcess;
	}

	public final Index<T> getIndex() {
		if (index == null)
			throw new NullPointerException("Index is null, use processIndex() first.");
		return index;
	}

	public final List<SentenceModel> getListSentence() {
		if (listSen.size() == 0)
			throw new NullPointerException("ListSentence is empty, use processIndex() first.");
		return listSen;
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
		((ListSentenceBasedIn) compMethod).setListSentence(listSen);
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(SentenceModel.class, List.class, ListSentenceBasedIn.class));
	}
}
