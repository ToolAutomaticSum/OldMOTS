package test.java.liasd.asadera.model.task.process;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import main.java.liasd.asadera.launcher.MOTS;

public abstract class MethodTest {

	private String configFile;
	private String multiCorpusFile = "conf" + File.separator + "test.xml";

	public MethodTest(String methodName) {
		configFile = "conf" + File.separator + "config_" + methodName + ".xml";
	}

	@Before
	public void init() {
		if (!new File("output/temp/D0901A-A").exists()) {
			String[] args = { "-c", "conf" + File.separator + "config_preprocess.xml", "-m", multiCorpusFile };
			try {
				MOTS.main(args);
			} catch (Exception e) {
				assert (false);
			}
		}
	}

	@Test
	public void build() {
		String[] args = { "-c", configFile, "-m", multiCorpusFile };
		try {
			MOTS.main(args);
		} catch (Exception e) {
			assert (false);
		}
	}
}
