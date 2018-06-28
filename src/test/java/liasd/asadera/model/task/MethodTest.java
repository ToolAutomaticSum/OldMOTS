package test.java.liasd.asadera.model.task;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.liasd.asadera.launcher.MOTS;

public abstract class MethodTest {

	private static Logger logger = LoggerFactory.getLogger(MethodTest.class);

	protected String methodName;
	protected String configFile;
	protected String multiCorpusFile = "conf" + File.separator + "test.xml";

	public MethodTest(String methodName) {
		this.methodName = methodName;
		this.configFile = "conf" + File.separator + "config_" + methodName + ".xml";
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
			if (!new File("output/temp/D0901A-A").exists()) {
				assert(false);
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
		if (new File("output/" + methodName + "/test/systems/T1_0_0_0.html").length() < 80) {
			logger.debug(String.valueOf(new File("output/" + methodName + "/test/systems/T1_0_0_0.html").length()));
			assert(false);
		}
	}
}
