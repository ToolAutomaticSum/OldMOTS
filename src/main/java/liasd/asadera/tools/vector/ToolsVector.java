package main.java.liasd.asadera.tools.vector;

import java.util.List;

import main.java.liasd.asadera.exception.VectorDimensionException;

public class ToolsVector {
	static public double[] scalarVector(double lambda, double[] a) {
		double[] temp = new double[a.length];
		for (int i = 0; i < a.length; i++)
			temp[i] = a[i] * lambda;
		return temp;
	}

	static public double scalar(double[] a, double[] b) throws VectorDimensionException {
		if (a.length == b.length) {
			double somme = 0.0;
			for (int i = 0; i < a.length; i++) {
				somme += a[i] * b[i];
			}
			return somme;
		} else
			throw new VectorDimensionException();
	}

	static public double norme(double[] a) {
		double res = 0.0;
		for (int i = 0; i < a.length; i++) {
			res += a[i] * a[i];
		}
		return Math.sqrt(res);
	}

	static public double tanimotoDistance(double[] a, double[] b) throws VectorDimensionException {
		if (a.length == b.length) {
			return scalar(a, b) / (Math.pow(norme(a), 2) + Math.pow(norme(a), 2) - scalar(a, b));
		} else
			throw new VectorDimensionException();
	}

	static public double cosineSimilarity(double[] a, double[] b) throws VectorDimensionException {
		if (a.length == b.length) {
			double a1 = scalar(a, b);
			if (a1 == 0)
				return 0;
			return a1/(norme(a)*norme(b));
		} else
			throw new VectorDimensionException();
	}

	public static double[] somme(double[] a, double[] b) throws VectorDimensionException {
		if (a.length == b.length) {
			double[] temp = new double[a.length];
			for (int i = 0; i < a.length; i++)
				temp[i] = a[i] + b[i];
			return temp;
		} else
			throw new VectorDimensionException();
	}

	public static double[] moins(double[] a) {
		double[] temp = new double[a.length];
		for (int i = 0; i < a.length; i++)
			temp[i] = -a[i];
		return temp;
	}

	/**
	 * a-b
	 * 
	 * @param a
	 * @param b
	 * @return
	 * @throws VectorDimensionException
	 */
	public static double[] soustraction(double[] a, double[] b) throws VectorDimensionException {
		if (a.length == b.length) {
			double[] temp = new double[a.length];
			for (int i = 0; i < a.length; i++)
				temp[i] = a[i] - b[i];
			return temp;
		} else
			throw new VectorDimensionException();
	}

	public static void printVector(double[] v) {
		System.out.print("[");
		for (int i = 0; i < v.length; i++) {
			System.out.print(" " + v[i]);
		}
		System.out.println(" ]");
	}

	public static double[] ListToArray(List<Double> floatList) {
		return floatList.stream().mapToDouble(f -> f != null ? f : 0d) // Or whatever default you want.
				.toArray();
	}
}
