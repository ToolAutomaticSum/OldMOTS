package liasd.asadera.model.task.process.caracteristicBuilder.graphBased;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import liasd.asadera.model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import liasd.asadera.model.task.process.caracteristicBuilder.GraphBasedIn;
import liasd.asadera.model.task.process.caracteristicBuilder.GraphBasedOut;
import liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import liasd.asadera.model.task.process.scoringMethod.graphBased.LexRank.LexRank_Parameter;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.wordIndex.Index;
import liasd.asadera.textModeling.wordIndex.WordIndex;
import liasd.asadera.textModeling.wordIndex.WordVector;
import liasd.asadera.tools.sentenceSimilarity.SimilarityMetric;

public class SimilarityGraphOfWordsBuilder extends AbstractCaracteristicBuilder
		implements IndexBasedIn<WordIndex>, GraphBasedOut<WordIndex, DefaultWeightedEdge> {

	private SimpleWeightedGraph<WordIndex, DefaultWeightedEdge> graph;
	private Index<WordIndex> index;
	private double threshold = 0.0;
	private double dampingFactor = 0.15;
	private SimilarityMetric sim;

	public SimilarityGraphOfWordsBuilder(int id) throws SupportADNException {
		super(id);

		listParameterIn.add(new ParameterizedType(WordVector.class, Index.class, IndexBasedIn.class));
		listParameterOut.add(new ParameterizedType(DefaultWeightedEdge.class, WordIndex.class, GraphBasedOut.class));

		graph = new SimpleWeightedGraph<WordIndex, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	}

	@Override
	public AbstractCaracteristicBuilder makeCopy() throws Exception {
		GraphOfWordsBuilder p = new GraphOfWordsBuilder(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		threshold = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "SimilarityThreshold"));
		dampingFactor = Double
				.parseDouble(getModel().getProcessOption(id, LexRank_Parameter.DampingParameter.getName()));
		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");

		sim = SimilarityMetric.instanciateSentenceSimilarity(similarityMethod);
	}

	@Override
	public void processCaracteristics(List<Corpus> listCorpus) throws Exception {
		for (WordIndex word : index.values())
			graph.addVertex(word);

		for (WordIndex w1 : index.values()) {
			for (WordIndex w2 : index.values()) {
				if (!w1.equals(w2)) {
					if (!graph.containsVertex(w1))
						graph.addVertex(w1);
					if (!graph.containsVertex(w2))
						graph.addVertex(w2);
					if (!graph.containsEdge(w1, w2)) {
						double similarity = sim.computeSimilarity(((WordVector) w1).getWordVector(),
								((WordVector) w2).getWordVector());
						if (similarity > threshold) {
							graph.addEdge(w1, w2);
							DefaultWeightedEdge edge = graph.getEdge(w1, w2);
							graph.setEdgeWeight(edge, similarity);
						}
					}
				}
			}
		}

		PageRank<WordIndex, DefaultWeightedEdge> pr = new PageRank<WordIndex, DefaultWeightedEdge>(graph,
				dampingFactor);
		for (WordIndex vertex : graph.vertexSet())
			vertex.setWeight(pr.getVertexScore(vertex));
	}

	@Override
	public void finish() {
		graph.removeAllEdges(new ArrayList<DefaultWeightedEdge>(graph.edgeSet()));
		graph.removeAllVertices(new ArrayList<WordIndex>(graph.vertexSet()));
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(DefaultWeightedEdge.class, WordIndex.class, GraphBasedIn.class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCompatibility(ParameterizedMethod compatibleMethod) {
		((GraphBasedIn<WordIndex, DefaultWeightedEdge>) compatibleMethod).setGraph(graph);
	}

	@Override
	public Graph<WordIndex, DefaultWeightedEdge> getGraph() {
		return graph;
	}

	@Override
	public void setIndex(Index<WordIndex> index) {
		this.index = index;
	}

}
