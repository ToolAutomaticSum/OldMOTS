package model.task.summarizeMethod;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import model.Model;
import textModeling.SentenceModel;
import textModeling.cluster.TopicLDA;
import tools.Tools;

public class TopicPosition extends AbstractSummarizeMethod implements TopicLdaBasedIn {

	
	private List<TopicLDA> listTopic;
	private Map<SentenceModel, double[]> sentenceCaracteristic;
	private int K;
	private Model model;

	public TopicPosition(int id) {
		super(id);
	}

	@SuppressWarnings("unused")
	private void positionPerParagraph() throws IOException {
		int t = listTopic.get(0).getId();
		//for (int t = 0; t < listTopic.size(); t++) {
			for (int i = 0; i < model.getDocumentModels().size(); i++) {
				double[][] plot = new double[model.getDocumentModels().get(i).size()+1][];
				plot[0] = new double[20];
				for (int j = 0; j < model.getDocumentModels().get(i).size(); j++) {
					if (model.getDocumentModels().get(i).get(j).getNbSentence() > 0) {
						plot[j+1] = new double[model.getDocumentModels().get(i).get(j).getNbSentence()];
						for (int k = 0; k < model.getDocumentModels().get(i).get(j).getNbSentence(); k++) {
							SentenceModel sen = model.getDocumentModels().get(i).get(j).get(k);
							plot[0][k] = k+1;
							plot[j+1][k] = (sentenceCaracteristic.get(sen))[t];
						}
					}
				}
				Tools.javaHistogramGnuPlot("scriptGnu" + i, null, plot);
			}
		//}
	}
	
	private void positionInDocument() throws IOException {

		for (int i = 0; i < model.getDocumentModels().size(); i++) {
			double[][] plot = new double[K+1][model.getDocumentModels().get(i).size()];
			for (int t = 1; t < listTopic.size()+1; t++) {
				if (listTopic.get(t-1).getScoreCorpus() > 0) {
					double score = 0;
					int p = 0; //paragraphe variable
					for (int j = 0; j < model.getDocumentModels().get(i).size(); j++) {
						//plot[0][p] = p+1;
						if (model.getDocumentModels().get(i).get(j).getNbSentence() != 0) {
							plot[0][p] = p+1;
							for (int k = 0; k < model.getDocumentModels().get(i).get(j).getNbSentence(); k++) {
								SentenceModel sen = model.getDocumentModels().get(i).get(j).get(k);
								score += (sentenceCaracteristic.get(sen))[listTopic.get(t-1).getId()];
							}
							score = score / model.getDocumentModels().get(i).size();
							plot[t][p] = score;
							p++;
						}						
					}
					System.out.println(listTopic.get(t-1).getId());
				}
			}
			Tools.javaHistogramGnuPlot("scriptGnuDocument", null, plot);
			Tools.writeDocumentBySentence(model.getDocumentModels().get(i));
		}
	}

	@Override
	public void setListTopicLda(List<TopicLDA> listTopic) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<SentenceModel> calculateSummary() throws Exception {
		model = getCurrentProcess().getModel();
		K = listTopic.size();
		positionInDocument();
		return null;
	}

	@Override
	public void setVectorCaracterisic(Map<SentenceModel, double[]> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;
	}
}