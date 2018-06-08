package test.java.liasd.asadera.model.task.process;

import java.io.File;

import org.junit.Test;

import main.java.liasd.asadera.launcher.MOTS;

public abstract class MethodTest {

	private String configFile;
	private String multiCorpusFile = "conf" + File.separator + "test.xml";
	
	public MethodTest(String methodName) {
		configFile = "conf" + File.separator + "config_" + methodName + ".xml";
	}
	
	@Test
	public void build() {
		String[] args = {"-c", configFile, "-m", multiCorpusFile};
		try {
			MOTS.main(args);
		} catch (Exception e) {
			assert(false);
		}
	}
}
