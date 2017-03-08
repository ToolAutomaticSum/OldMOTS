package textModeling.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jgibblda.Pair;
import textModeling.SentenceModel;
import textModeling.WordModel;
import textModeling.wordIndex.Index;
import textModeling.wordIndex.WordIndex;
import textModeling.wordIndex.TF_IDF.WordTF_IDF;
import tools.wordFilters.WordFilter;
import tools.wordFilters.WordStopListFilter;

public class ClusterCentroid_TF_IDF extends ClusterCentroid {

	private WordFilter wf = new WordStopListFilter();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1760296591762349834L;

	public ClusterCentroid_TF_IDF(int id, Index dictionnary, Map<Integer, String> hashMapWord, int nbMaxWord, double thresholdCluster) {
		super(id, dictionnary, hashMapWord, nbMaxWord, thresholdCluster);
	}

	public void addSentence(SentenceModel sentence) {
		super.add(sentence);

		/** Ajout des mots de la phrase au dictionnaire du cluster */
		Iterator<WordModel> itWord = sentence.iterator();
		while (itWord.hasNext()) {
			WordModel word = itWord.next();
			if (wf.passFilter(word)) {
				if (!clusterDictionnary.containsKey(word.getmLemma())) {
					clusterDictionnary.put(word.getmLemma(), new WordTF_IDF(word.getmLemma(), clusterDictionnary, dictionnary.get(word.getmLemma()).getId()));
				}
				clusterDictionnary.get(word.getmLemma()).add(word);
			}
		}
		//sentenceCaracteristic.put(sentence, caracteristic);
		//System.out.println(this.toString());
	}
	
	/**
	 * Retourne le barycentre du cluster
	 * @return centroid, double[]
	 */
	public double[] getCentroid() {
		double[] centroid = new double[dictionnary.size()];
		List<Pair> listScoreWord = new ArrayList<Pair>();
		
		Iterator<WordIndex> wordIndexIt = clusterDictionnary.values().iterator();
		while (wordIndexIt.hasNext()) {
			WordTF_IDF wordIndex = (WordTF_IDF) wordIndexIt.next();
			//WordTF_IDF a = (WordTF_IDF)dictionnary.get(wordIndex.getWord());
			double idf = ((WordTF_IDF)dictionnary.get(wordIndex.getWord())).getIdf();
			//System.out.println(wordIndex.getWord());
			if (idf > thresholdCluster) {
				listScoreWord.add(new Pair(wordIndex, wordIndex.getTf()*idf));
			}
		}
		Collections.sort(listScoreWord);
		
		int i = 0;
		while (i<listScoreWord.size() && i<nbMaxWord) {
			WordTF_IDF w = (WordTF_IDF)listScoreWord.get(i).first;
			centroid[w.getId()] = (double)listScoreWord.get(i).second/this.size();
			i++;
		}		
		return centroid;
	}
}
