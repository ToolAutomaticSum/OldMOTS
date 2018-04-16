package main.java.liasd.asadera.model.task.process.caracteristicBuilder;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public interface GraphBasedOut<V extends WordIndex, E extends DefaultWeightedEdge> {
	public Graph<V, E> getGraph();
}
