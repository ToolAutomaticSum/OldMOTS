package textModeling;

import java.util.ArrayList;

import model.Model;

public class MultiCorpus extends ArrayList<Corpus> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5123106615786643432L;

	protected int iD;
	protected Model model;
	/*protected Index index;
	protected InvertedIndex invertIndex;*/
	
	public MultiCorpus(int iD) {
		this.iD = iD;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	/*public Index getIndex() {
		return index;
	}

	public void setIndex(Index index) {
		this.index = index;
	}

	public InvertedIndex getInvertIndex() {
		return invertIndex;
	}

	public void setInvertIndex(InvertedIndex invertIndex) {
		this.invertIndex = invertIndex;
	}*/

	public int getiD() {
		return iD;
	}
}
