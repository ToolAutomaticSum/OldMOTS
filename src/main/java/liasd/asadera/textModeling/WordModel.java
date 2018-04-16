package main.java.liasd.asadera.textModeling;

public class WordModel {

	protected String word;
	protected boolean stopWord = false;

	private SentenceModel sentence;
	// CONLL X Format
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
	}

	public WordModel(String mLemma) {
		this.mLemma = mLemma;
	}

	public WordModel(String mID, String mForm, String mLemma, String mCPosTag, String mPosTag) {
		this.mID = mID;
		this.mForm = mForm;
		this.mLemma = mLemma;
		this.mCPosTag = mCPosTag;
		this.mPosTag = mPosTag;
	}

	public WordModel(String mID, String mForm, String mLemma, String mCPosTag, String mPosTag, String mHead,
			String mDepRel) {
		this.mID = mID;
		this.mForm = mForm;
		this.mLemma = mLemma;
		this.mCPosTag = mCPosTag;
		this.mHead = mHead;
		this.mPosTag = mPosTag;
		this.mDepRel = mDepRel;
	}

	public WordModel(WordModel e) {
		this.mID = e.getmID();
		this.mForm = e.getmForm();
		this.mLemma = e.getmLemma();
		this.mCPosTag = e.getmCPosTag();
		this.mHead = e.getmHead();
		this.mPosTag = e.getmPosTag();
		this.mDepRel = e.getmDepRel();
		this.word = e.getWord();
		this.stopWord = e.isStopWord();
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
			return mID + "\t" + mForm + "\t" + mLemma + "\t" + mCPosTag + "\t" + mPosTag + "\t" + mFeats + "\t" + mHead
					+ "\t" + mDepRel + "\t" + mPHead + "\t" + mPDepRel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mCPosTag == null) ? 0 : mCPosTag.hashCode());
		result = prime * result + ((mDepRel == null) ? 0 : mDepRel.hashCode());
		result = prime * result + ((mFeats == null) ? 0 : mFeats.hashCode());
		result = prime * result + ((mForm == null) ? 0 : mForm.hashCode());
		result = prime * result + ((mHead == null) ? 0 : mHead.hashCode());
		result = prime * result + ((mID == null) ? 0 : mID.hashCode());
		result = prime * result + ((mLemma == null) ? 0 : mLemma.hashCode());
		result = prime * result + ((mPDepRel == null) ? 0 : mPDepRel.hashCode());
		result = prime * result + ((mPHead == null) ? 0 : mPHead.hashCode());
		result = prime * result + ((mPosTag == null) ? 0 : mPosTag.hashCode());
		result = prime * result + ((sentence == null) ? 0 : sentence.hashCode());
		result = prime * result + (stopWord ? 1231 : 1237);
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WordModel other = (WordModel) obj;
		if (mCPosTag == null) {
			if (other.mCPosTag != null)
				return false;
		} else if (!mCPosTag.equals(other.mCPosTag))
			return false;
		if (mDepRel == null) {
			if (other.mDepRel != null)
				return false;
		} else if (!mDepRel.equals(other.mDepRel))
			return false;
		if (mFeats == null) {
			if (other.mFeats != null)
				return false;
		} else if (!mFeats.equals(other.mFeats))
			return false;
		if (mForm == null) {
			if (other.mForm != null)
				return false;
		} else if (!mForm.equals(other.mForm))
			return false;
		if (mHead == null) {
			if (other.mHead != null)
				return false;
		} else if (!mHead.equals(other.mHead))
			return false;
		if (mLemma == null) {
			if (other.mLemma != null)
				return false;
		} else if (!mLemma.equals(other.mLemma))
			return false;
		if (mPDepRel == null) {
			if (other.mPDepRel != null)
				return false;
		} else if (!mPDepRel.equals(other.mPDepRel))
			return false;
		if (mPHead == null) {
			if (other.mPHead != null)
				return false;
		} else if (!mPHead.equals(other.mPHead))
			return false;
		if (mPosTag == null) {
			if (other.mPosTag != null)
				return false;
		} else if (!mPosTag.equals(other.mPosTag))
			return false;
		if (stopWord != other.stopWord)
			return false;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}
}
