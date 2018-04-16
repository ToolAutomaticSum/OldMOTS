package main.java.liasd.asadera.tools.matrix;

import main.java.liasd.asadera.exception.VectorDimensionException;
import main.java.liasd.asadera.tools.vector.ToolsVector;

public class ToolsMatrix {

	public static double trace(double[][] a) throws RuntimeException {
		int n1 = a[0].length;
		int m1 = a.length;
		if (m1 < 1)
			throw new RuntimeException("Illegal matrix dimensions.");
		int minSize = (m1 > n1) ? n1 : m1;
		double trace = 0;
		for (int i = 0; i < minSize; i++)
			trace += a[i][i];
		return trace;
	}

	public static double innerProduct(double[][] a, double[][] b) {
		int n1 = a[0].length;
		int m2 = b.length;
		if (n1 != m2)
			throw new RuntimeException("Illegal matrix dimensions.");
		double c = 0;
		for (int i = 0; i < n1; i++)
			try {
				c += ToolsVector.scalar(a[i], b[i]);
			} catch (VectorDimensionException e) {
				e.printStackTrace();
			}
		return c;
	}

	// return n-by-n identity matrix I
	public static double[][] identity(int n) {
		double[][] a = new double[n][n];
		for (int i = 0; i < n; i++)
			a[i][i] = 1;
		return a;
	}

	// return B = A^T
	public static double[][] transpose(double[][] a) {
		int m = a.length;
		int n = a[0].length;
		double[][] b = new double[n][m];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				b[j][i] = a[i][j];
		return b;
	}

	// return c = a + b
	public static double[][] add(double[][] a, double[][] b) {
		int m = a.length;
		int n = a[0].length;
		double[][] c = new double[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				c[i][j] = a[i][j] + b[i][j];
		return c;
	}

	// return c = a - b
	public static double[][] subtract(double[][] a, double[][] b) {
		int m = a.length;
		int n = a[0].length;
		double[][] c = new double[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				c[i][j] = a[i][j] - b[i][j];
		return c;
	}

	// return c = a * b
	public static double[][] multiply(double[][] a, double[][] b) {
		int m1 = a.length;
		int n1 = a[0].length;
		int m2 = b.length;
		int n2 = b[0].length;
		if (n1 != m2)
			throw new RuntimeException("Illegal matrix dimensions.");
		double[][] c = new double[m1][n2];
		for (int i = 0; i < m1; i++)
			for (int j = 0; j < n2; j++)
				for (int k = 0; k < n1; k++)
					c[i][j] += a[i][k] * b[k][j];
		return c;
	}

	// matrix-vector multiplication (y = A * x)
	public static double[] multiply(double[][] a, double[] x) {
		int m = a.length;
		int n = a[0].length;
		if (x.length != n)
			throw new RuntimeException("Illegal matrix dimensions.");
		double[] y = new double[m];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				y[i] += a[i][j] * x[j];
		return y;
	}

	// vector-matrix multiplication (y = x^T A)
	public static double[] multiply(double[] x, double[][] a) {
		int m = a.length;
		int n = a[0].length;
		if (x.length != m)
			throw new RuntimeException("Illegal matrix dimensions.");
		double[] y = new double[n];
		for (int j = 0; j < n; j++)
			for (int i = 0; i < m; i++)
				y[j] += a[i][j] * x[i];
		return y;
	}
}
