package test.java.liasd.asadera.model.task.preprocess;

import java.io.File;

import main.java.liasd.asadera.launcher.MOTS;
import test.java.liasd.asadera.model.task.MethodTest;

public class PreProcessTest extends MethodTest {

	public PreProcessTest() {
		super("preprocess");
		this.multiCorpusFile = "conf" + File.separator + "test.xml";
	}
	
	@Override
	public void init() {
	}
	
	@Override
	public void build() {
		if (new File("output/temp/D0901A-A").exists())
			new File("output/temp/D0901A-A").delete();
		String[] args = {"-c", configFile, "-m", multiCorpusFile};
		try {
			MOTS.main(args);
		} catch (Exception e) {
			e.printStackTrace(System.out);
			assert (false);
		}
		if (!new File("output/temp/D0901A-A").exists()) {
			assert(false);
		}
	}
}
