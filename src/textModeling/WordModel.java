package textModeling;

public class WordModel {

	protected String word;
	protected boolean stopWord = false;
	
	private SentenceModel sentence;
	// CONLL X Format
	/**
	 * Position dans la phrase
	 */
	private String mID = "_";
	private String mForm = "_";
	private String mLemma = "_";
	private String mCPosTag = "_";
	private String mPosTag = "_";
	private String mFeats = "_";
	private String mHead = "_";
	private String mDepRel = "_";
	private String mPHead = "_";
	private String mPDepRel = "_";
	
	public WordModel() {
		super();
	}
	
	public WordModel(String mLemma) {
		super();
		this.mLemma = mLemma;
	}

	public WordModel(String mID, String mForm, String mLemma, String mCPosTag, String mPosTag) {
		super();
		this.mID = mID;
		this.mForm = mForm;
		this.mLemma = mLemma;
		this.mCPosTag = mCPosTag;
		this.mPosTag = mPosTag;
	}

	public WordModel(String mID, String mForm, String mLemma, String mCPosTag, String mPosTag, String mHead, String mDepRel) {
		super();
		this.mID = mID;
		this.mForm = mForm;
		this.mLemma = mLemma;
		this.mCPosTag = mCPosTag;
		this.mHead = mHead;
		this.mPosTag = mPosTag;
		this.mDepRel = mDepRel;
	}
	
	public WordModel(WordModel e) {
		super();
		this.mID = e.getmID();
		this.mForm = e.getmForm();
		this.mLemma = e.getmLemma();
		this.mCPosTag = e.getmCPosTag();
		this.mHead = e.getmHead();
		this.mPosTag = e.getmPosTag();
		this.mDepRel = e.getmDepRel();
	}

	public SentenceModel getSentence() {
		return sentence;
	}

	public void setSentence(SentenceModel sentence) {
		this.sentence = sentence;
	}

	public String getmID() {
		return mID;
	}

	public void setmID(String mID) {
		this.mID = mID;
	}

	public String getmForm() {
		return mForm;
	}

	public void setmForm(String mForm) {
		this.mForm = mForm;
	}

	public String getmLemma() {
		return mLemma;
	}

	public void setmLemma(String mLemma) {
		this.mLemma = mLemma;
	}

	public String getmCPosTag() {
		return mCPosTag;
	}

	public void setmCPosTag(String mCPosTag) {
		this.mCPosTag = mCPosTag;
	}

	public String getmPosTag() {
		return mPosTag;
	}

	public void setmPosTag(String mPosTag) {
		this.mPosTag = mPosTag;
	}

	public String getmFeats() {
		return mFeats;
	}

	public void setmFeats(String mFeats) {
		this.mFeats = mFeats;
	}

	public String getmHead() {
		return mHead;
	}

	public void setmHead(String mHead) {
		this.mHead = mHead;
	}

	public String getmDepRel() {
		return mDepRel;
	}

	public void setmDepRel(String mDepRel) {
		this.mDepRel = mDepRel;
	}

	public String getmPHead() {
		return mPHead;
	}

	public void setmPHead(String mPHead) {
		this.mPHead = mPHead;
	}

	public String getmPDepRel() {
		return mPDepRel;
	}

	public void setmPDepRel(String mPDepRel) {
		this.mPDepRel = mPDepRel;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
		this.setmForm(word);
	}

	public boolean isStopWord() {
		return stopWord;
	}

	public void setStopWord(boolean stopWord) {
		this.stopWord = stopWord;
	}

	@Override
	public String toString() {
		return mLemma;
	}
	
	public String toFullString() {
		if (stopWord)
			return "";
		else
			return mID + "\t" + mForm + "\t" + mLemma + "\t" + mCPosTag
					+ "\t" + mPosTag + "\t" + mFeats + "\t" + mHead + "\t"
					+ mDepRel + "\t" + mPHead + "\t" + mPDepRel;
	}
}
