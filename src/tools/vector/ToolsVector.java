package tools.vector;

import exception.VectorDimensionException;

public class ToolsVector {
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
		int n = a.length;
	    double res = 0.0 ;
	    for (--n; n >= 0; --n) {
	        res += a[n] * a[n] ;
	    }
	    return Math.sqrt(res) ;
	}
	
	static public double tanimotoDistance(double[] a, double[] b) throws VectorDimensionException {
		if (a.length == b.length) {
			return scalar(a,b)/(Math.pow(norme(a),2) + Math.pow(norme(a),2) - scalar(a,b));
		} else
			throw new VectorDimensionException();
	}
	
	static public double cosineSimilarity(double[] a, double[] b) throws VectorDimensionException {
		if (a.length == b.length) {
			double n = 0; //numérateur scalar(a,b);
			double normeA = 0; //norme de a;
			double normeB = 0; //norme de b;
			for (int i = 0; i<a.length; i++) {
				n+=a[i]*b[i];
				normeA+=Math.pow(a[i],2);
				normeB+=Math.pow(b[i],2);
			}
			return n/(Math.sqrt(normeA)*Math.sqrt(normeB));
		} else
			throw new VectorDimensionException();
	}
	
	public static double[] somme(double[] a, double[] b) throws VectorDimensionException  {
		if (a.length == b.length) {
			double[] temp = new double[a.length];
			for (int i = 0; i<a.length; i++)
				temp[i] = a[i]+b[i];
			return temp;
		} else
			throw new VectorDimensionException();
	}
	
	public static double[] moins(double[] a) {
		double[] temp = new double[a.length];
		for (int i = 0; i<a.length; i++)
			temp[i] = -a[i];
		return temp;
	}
	
	/**
	 * a-b
	 * @param a
	 * @param b
	 * @return
	 * @throws VectorDimensionException
	 */
	public static double[] soustraction(double[] a, double[] b) throws VectorDimensionException  {
		if (a.length == b.length) {
			double[] temp = new double[a.length];
			for (int i = 0; i<a.length; i++)
				temp[i] = a[i]-b[i];
			return temp;
		} else
			throw new VectorDimensionException();
	}

	public static double[][] transposeMatrix(double [][] m){
        double[][] temp = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
    }
}
