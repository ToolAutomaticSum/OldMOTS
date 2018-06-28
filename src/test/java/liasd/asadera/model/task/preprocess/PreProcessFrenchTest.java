package test.java.liasd.asadera.model.task.preprocess;

import java.io.File;

import main.java.liasd.asadera.launcher.MOTS;
import test.java.liasd.asadera.model.task.MethodTest;

public class PreProcessFrenchTest extends MethodTest {

	public PreProcessFrenchTest() {
		super("preprocessFrench");
		this.multiCorpusFile = "conf" + File.separator + "testFrench.xml";
	}
	
	@Override
	public void init() {
	}
	
	@Override
	public void build() {
		if (new File("output/temp/DF1801A-A").exists())
			new File("output/temp/DF1801A-A").delete();
		String[] args = {"-c", configFile, "-m", multiCorpusFile};
		try {
			MOTS.main(args);
		} catch (Exception e) {
			e.printStackTrace(System.out);
			assert (false);
		}
		if (!new File("output/temp/DF1801A-A").exists()) {
			assert(false);
		}
	}
}
