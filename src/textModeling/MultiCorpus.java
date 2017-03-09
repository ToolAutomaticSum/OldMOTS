package textModeling;

import java.util.ArrayList;

import model.SModel;

public class MultiCorpus extends ArrayList<Corpus> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5123106615786643432L;

	protected int iD;
	protected SModel model;
	/*protected Index index;
	protected InvertedIndex invertIndex;*/
	
	public MultiCorpus(int iD) {
		this.iD = iD;
	}

	public SModel getModel() {
		return model;
	}

	public void setModel(SModel model) {
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
