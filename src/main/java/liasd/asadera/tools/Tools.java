package main.java.liasd.asadera.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import main.java.liasd.asadera.exception.VectorDimensionException;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.TextModel;
import main.java.liasd.asadera.tools.reader_writer.Writer;
import main.java.liasd.asadera.tools.vector.ToolsVector;

public class Tools {

	static public String unAccent(String str) {
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}

	static public String removePonctuation(String str) {
		if (!str.contentEquals("-"))
			str = str.replaceAll("-", "wwwwwwww");
		str = str.replaceAll("[\\p{Punct}()\n�����������������������]", "");
		return str.replaceAll("wwwwwwww", "-");
	}

	static public double objectiveFunction(double a[], double b[]) throws VectorDimensionException {
		return ToolsVector.cosineSimilarity(a, b);
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

			byte buffer[] = new byte[512 * 1024];
			int nbRead;
			while ((nbRead = sourceFile.read(buffer)) != -1) {
				destinationFile.write(buffer, 0, nbRead);
			}
			sourceFile.close();
			destinationFile.close();
		} catch (Exception e) {
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

	static public void writeDocumentBySentence(TextModel doc) throws Exception {
		String str = "";
		int s = 1; // sentence variable
		Iterator<SentenceModel> senIt = doc.iterator();
		while (senIt.hasNext()) {
			SentenceModel sen = senIt.next();
			while (senIt.hasNext()) {
				str += s + "\t" + sen.getSentence() + "\n";
				s++;
			}
		}
		Writer w = new Writer("doc.txt");
		w.open(false);
		w.write(str);
		w.close();
	}

	public static Set<Class<?>> getInheritance(Class<?> in) {
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
	private static void getInheritance(Class<?> in, Set<Class<?>> result) {
		Class<?> superclass = getSuperclass(in);

		if (superclass != null) {
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
	private static void getInterfaceInheritance(Class<?> in, Set<Class<?>> result) {
		for (Class<?> c : in.getInterfaces()) {
			result.add(c);

			getInterfaceInheritance(c, result);
		}
	}

	public static Set<Class<?>> getInterfaceInheritance(Class<?> in) {
		LinkedHashSet<Class<?>> result = new LinkedHashSet<Class<?>>();

		getInterfaceInheritance(in, result);

		return result;
	}

	/**
	 * Get superclass of class.
	 * 
	 * @param in
	 * @return
	 */
	private static Class<?> getSuperclass(Class<?> in) {
		if (in == null) {
			return null;
		}

		if (in.isArray() && in != Object[].class) {
			Class<?> type = in.getComponentType();

			while (type.isArray()) {
				type = type.getComponentType();
			}

			return type;
		}

		return in.getSuperclass();
	}

	/**
	 * Removes all doubled occurences in arr
	 * 
	 * @param arr
	 */
	public static <T> void toSet(ArrayList<T> arr) {
		T ng1, ng2;
		for (int i = 0; i < arr.size() - 1; i++) {
			ng1 = arr.get(i);
			for (int j = i + 1; j < arr.size(); j++) {
				ng2 = arr.get(j);
				if (ng1.equals(ng2)) {
					arr.remove(j);
					j--;
				}
			}
		}
	}

	public static boolean isUTF8MisInterpreted(String input) {
		// convenience overload for the most common UTF-8 misinterpretation
		// which is also the case in your question
		return isUTF8MisInterpreted(input, "Windows-1252");
	}

	public static boolean isUTF8MisInterpreted(String input, String encoding) {
		CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
		CharsetEncoder encoder = Charset.forName(encoding).newEncoder();
		ByteBuffer tmp;
		try {
			tmp = encoder.encode(CharBuffer.wrap(input));
		}

		catch (CharacterCodingException e) {
			return false;
		}

		try {
			decoder.decode(tmp);
			return true;
		} catch (CharacterCodingException e) {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void setEnv(String key, String value) {
	    try {
	        Map<String, String> env = System.getenv();
	        Class<?> cl = env.getClass();
	        Field field = cl.getDeclaredField("m");
	        field.setAccessible(true);
	        Map<String, String> writableEnv = (Map<String, String>) field.get(env);
	        writableEnv.put(key, value);
	    } catch (Exception e) {
	        throw new IllegalStateException("Failed to set environment variable", e);
	    }
	}
	
	
}
