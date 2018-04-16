package main.java.liasd.asadera.textModeling;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeSet;

public class StopList extends TreeSet<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2599032960868004704L;

	public StopList(String file_name) {
		try {
			FileInputStream fisr = new FileInputStream(file_name);

			InputStreamReader isr = new InputStreamReader(fisr);
			BufferedReader br = new BufferedReader(isr);
			String curr_line;
			while ((curr_line = br.readLine()) != null) {
				curr_line.replaceAll("\\s+", "");
				if (!curr_line.isEmpty()) {
					this.add(curr_line);
				}
			}
			br.close();
			isr.close();
			fisr.close();
		} catch (IOException ioe) {
			System.err.println("Error while reading stoplist file. Stoplist possibly empty");
			ioe.printStackTrace();
		}

	}
}
