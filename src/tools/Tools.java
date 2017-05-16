package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import exception.VectorDimensionException;
import reader_writer.Writer;
import textModeling.SentenceModel;
import textModeling.TextModel;
import tools.vector.ToolsVector;

public class Tools {

	static public String unAccent(String str) {
	    String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    return pattern.matcher(nfdNormalizedString).replaceAll("");
	}
	
	static public String enleverPonctuation(String str) {
		if (!str.contentEquals("-"))
			str = str.replaceAll("-","wwwwwwww");
	    str = str.replaceAll("[\\p{Punct}()\n�����������������������]",""); //\\d enlever digit
	    return str.replaceAll("wwwwwwww","-");
	}
	
	static public double objectiveFunction(double a[], double b[]) throws VectorDimensionException {
		//2*s+d
		double s = ToolsVector.cosineSimilarity(a, b);
		//double t = Math.acos(s);
		//double d = 2*Math.acos(s)/Math.PI;
		return s;
	}
	
	static public void copyFile(String src, String dest) {
		File source = new File(src);
		File destination = new File(dest);
		
		 FileInputStream sourceFile = null;
		 FileOutputStream destinationFile = null;
		 
		 try {
			 destination.createNewFile();
			 sourceFile = new FileInputStream(source);
			 destinationFile = new FileOutputStream(destination);
			 
			 byte buffer[] = new byte[512*1024];
			 int nbRead;
			 while ((nbRead = sourceFile.read(buffer)) != -1) {
				 destinationFile.write(buffer, 0, nbRead);
			 }
			 sourceFile.close();
			 destinationFile.close();
		 }
		 catch (Exception e) {
			 e.printStackTrace();
		 }		 
	}

	static public String getFileExtension(File file) {
	    String name = file.getName();
	    try {
	        return name.substring(name.lastIndexOf(".") + 1);
	    } catch (Exception e) {
	        return "";
	    }
	}
	
	static public void writeDocumentBySentence(TextModel doc) {
		String str = "";
		int s = 1; // sentence variable
		Iterator<SentenceModel> senIt = doc.iterator();
		while (senIt.hasNext()) {
			SentenceModel sen = senIt.next();
			while (senIt.hasNext()) {
				str+=s + "\t" + sen.getSentence() + "\n";
				s++;
			}
		}
		Writer w = new Writer("doc.txt");
		w.open();
		w.write(str);
		w.close();
	}
	
	/**
	 * 
	 * @param filename
	 * @param parameters
	 * @param xy, premi�re colonne toujours �gale � x, ensuite une colonne par y
	 * @throws IOException
	 */
	static public void javaHistogramGnuPlot(String filename, Map<String, String> parameters, double[][] xy) throws IOException{
	    if(xy.length==0){
	        System.out.println("This one had no data - " + filename);
	        return;
	    }
	    parameters = new HashMap<String,String>();
		parameters.put("set title", "\"Score of the best topic by position of sentences in a paragraph\"");
		parameters.put("set xlabel", "\"Sentence position in a paragraph\"");
		parameters.put("set ylabel", "\"Best topic score\"");
		parameters.put("set style data histogram", "");
		parameters.put("set style histogram cluster gap", "1");
		parameters.put("set style fill solid border", "-1");
		parameters.put("set boxwidth", "1");
		parameters.put("set key outside right", "");
		parameters.put("set yrange[0:0.00023]", "");
		parameters.put("set xrange[0:63]", "");
		
	    File fold1 = new File("data.dat");
	    if(fold1.exists()){
	        if(!fold1.delete())
	        	System.out.println("Houstoonnn!!!");
	    }
	    FileWriter outF1 = new FileWriter("data.dat");
	    PrintWriter out1 = new PrintWriter(outF1);
	    
	    int i = 0;
	    while (i < xy[0].length && xy[0][i] != 0) { //boucle sur les lignes
    		String str = String.valueOf(xy[0][i]); 
			for(int j=1;j < xy.length;j++){ //boucle sur les colonnes
				if (xy[j] != null && xy[j][0] != 0)
					str+= "\t" + xy[j][i];
				else
					;//str+= "\t" + 0;					
    		}
	    	out1.println(str);
	    	i++;
	    }
	    out1.close();
	    File fold = new File(filename + ".gp");
	    try{//If the file already exists, delete it..
	        fold.delete();
	    }
	    catch(Exception e) {}
	    FileWriter outF = new FileWriter(filename + ".gp");
	    PrintWriter out = new PrintWriter(outF);
	    //out.println("set terminal gif");
	    out.println("set output \"" + filename + ".gif\"");
	    /*out.print("set title " + "\""+"a"+"\"" + "\n");
	    out.print("set xlabel " + "\"Time\"" + "\n");
	    out.print("set ylabel " + "\"UA\"" + "\n");*/
	    if (parameters != null) {
		    Iterator<Entry<String,String>> it = parameters.entrySet().iterator();
		    while (it.hasNext()) {
		    	Entry<String,String> e = it.next();
		    	out.println(e.getKey() + " " + e.getValue());
		    }
	    }
	    out.println("set key right bottom");
	    String string = "plot \"data.dat\" using 2 title \"Topic N�1\"";
	    if (xy.length>2) {
	    	int j = 2;
		   	for (int k = 2; k<xy.length; k++) {
		    	if (xy[k] != null && xy[k][0] != 0) {
		    		string += ", \"\" using " + (j+1) + " lc " + (j+1) +" title \"Topic N�" + (k) + "\"";
		    		j++;
		    	}
		    }
		}
	    out.println(string);
	    out.close();// It's done, closing document.
	    
	}
	
	public static Set<Class<?>> getInheritance(Class<?> in)
	{
	    LinkedHashSet<Class<?>> result = new LinkedHashSet<Class<?>>();

	    result.add(in);
	    getInheritance(in, result);

	    return result;
	}

	/**
	 * Get inheritance of type.
	 * 
	 * @param in
	 * @param result
	 */
	private static void getInheritance(Class<?> in, Set<Class<?>> result)
	{
	    Class<?> superclass = getSuperclass(in);

	    if(superclass != null)
	    {
	        result.add(superclass);
	        getInheritance(superclass, result);
	    }

	    getInterfaceInheritance(in, result);
	}

	/**
	 * Get interfaces that the type inherits from.
	 * 
	 * @param in
	 * @param result
	 */
	private static void getInterfaceInheritance(Class<?> in, Set<Class<?>> result)
	{
	    for(Class<?> c : in.getInterfaces())
	    {
	        result.add(c);

	        getInterfaceInheritance(c, result);
	    }
	}

	/**
	 * Get superclass of class.
	 * 
	 * @param in
	 * @return
	 */
	private static Class<?> getSuperclass(Class<?> in)
	{
	    if(in == null)
	    {
	        return null;
	    }

	    if(in.isArray() && in != Object[].class)
	    {
	        Class<?> type = in.getComponentType();

	        while(type.isArray())
	        {
	            type = type.getComponentType();
	        }

	        return type;
	    }

	    return in.getSuperclass();
	}
	
	/**
	 * Removes all doubled occurences in arr
	 * @param arr
	 */
	public static <T> void toSet (ArrayList <T> arr)
	{
		T ng1, ng2;
		for (int i = 0; i < arr.size() - 1 ; i++)
		{
			ng1 = arr.get(i);
			for (int j = i + 1; j < arr.size(); j++)
			{
				ng2 = arr.get(j);
				if (ng1.equals(ng2))
				{
					arr.remove(j);
					j--;
				}
			}
		}
	}
	
	public static boolean isUTF8MisInterpreted( String input ) {
        //convenience overload for the most common UTF-8 misinterpretation
        //which is also the case in your question
		return isUTF8MisInterpreted( input, "Windows-1252");  
	}

	public static boolean isUTF8MisInterpreted( String input, String encoding) {
		CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
		CharsetEncoder encoder = Charset.forName(encoding).newEncoder();
		ByteBuffer tmp;
		try {
		    tmp = encoder.encode(CharBuffer.wrap(input));
		}
		
		catch(CharacterCodingException e) {
		    return false;
		}

		try {
		    decoder.decode(tmp);
		    return true;
		}
		catch(CharacterCodingException e){
		    return false;
		}       
	}
}
