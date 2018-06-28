package test.java.liasd.asadera.model.task.process.processCompatibiliy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import main.java.liasd.asadera.model.task.process.caracteristicBuilder.vector.MeanVectorSentence;
import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedIn;
import main.java.liasd.asadera.model.task.process.indexBuilder.IndexBasedOut;
import main.java.liasd.asadera.model.task.process.indexBuilder.ListSentenceBasedOut;
import main.java.liasd.asadera.model.task.process.indexBuilder.LDA.LDA;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.scoringMethod.ScoreBasedIn;
import main.java.liasd.asadera.model.task.process.scoringMethod.graphBased.LexRank;
import main.java.liasd.asadera.model.task.process.selectionMethod.Knapsack;
import main.java.liasd.asadera.model.task.process.selectionMethod.scorer.SentenceBasedScorer;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.WordVector;

public class ProcessCompatibilityTest {
	
	public int id = 0;
	
	@Test
	public void testCompatible_LDA_MeanVectorSentence() {
		try {
			LDA methodObject1 = new LDA(id++);
			MeanVectorSentence methodObject2 = new MeanVectorSentence(id++);
			
			List<ParameterizedType> paramOut1 = new ArrayList<ParameterizedType>();
			paramOut1.add(new ParameterizedType(SentenceModel.class, List.class, ListSentenceBasedOut.class));
			paramOut1.add(new ParameterizedType(WordVector.class, Index.class, IndexBasedOut.class));
			List<ParameterizedType> paramIn2 = new ArrayList<ParameterizedType>();
			paramIn2.add(new ParameterizedType(WordVector.class, Index.class, IndexBasedIn.class));
			
			assert(methodObject1.getParameterTypeOut().equals(paramOut1));
			assert(methodObject2.getParameterTypeIn().equals(paramIn2));
			
			
			assert(methodObject1.isOutCompatible(methodObject2));
		} catch (SupportADNException e) {
			e.printStackTrace();
			assert(false);
		}
	}
	
	@Test
	public void testCompatible_LexRank_Knapsack() {
		try {
			LexRank methodObject1 = new LexRank(id++);
			Knapsack methodObject2 = new Knapsack(id++);
			SentenceBasedScorer subMethodObject2 = new SentenceBasedScorer(methodObject2);
			methodObject2.setScorer(subMethodObject2);
			
			List<ParameterizedType> paramIn1 = new ArrayList<ParameterizedType>();
			paramIn1.add(new ParameterizedType(Double.class, Map.class, ScoreBasedIn.class));
			paramIn1.add(new ParameterizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
			paramIn1.add(new ParameterizedType(double[][].class, Map.class, SentenceCaracteristicBasedIn.class));
			paramIn1.add(new ParameterizedType(double[][][].class, Map.class, SentenceCaracteristicBasedIn.class));
			List<ParameterizedType> paramIn2 = new ArrayList<ParameterizedType>();

			assert(methodObject1.getParameterTypeIn().equals(paramIn1));
			assert(methodObject2.getParameterTypeIn().equals(paramIn2));
			
			assert(!methodObject1.isOutCompatible(methodObject2));
			for (ParameterizedMethod subMethod : methodObject2.getSubMethod())
				assert(methodObject1.isOutCompatible(subMethod));
			
		} catch (SupportADNException e) {
			e.printStackTrace();
			assert(false);
		}
	}
}
