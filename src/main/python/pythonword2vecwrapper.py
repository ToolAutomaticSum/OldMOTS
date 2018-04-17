from gensim.models.word2vec import *
from gensim.utils import SaveLoad
import logging

def convertSentencesType(sentences):
	if sentences is None:
		return sentences
	if type(sentences) is list:
		return sentences
	corpus=[]
	for sent_idx, sentence in enumerate(sentences):
		sen=[]
		for word_idx, word in enumerate(sentence):
			sen.append(word)
		corpus.append(sen)
	return corpus

class PythonWord2VecWrapper():
	def __init__(self, sentences=None, size=100, alpha=0.025, window=5, min_count=5, max_vocab_size=None, sample=1e-3, seed=1, workers=3, min_alpha=0.0001, sg=0, hs=0, negative=5, cbow_mean=1, hashfxn=hash, iter=5, null_word=0, trim_rule=None, sorted_vocab=1, batch_words=MAX_WORDS_IN_BATCH):
		"""
        Initialize the model from an iterable of `sentences`. Each sentence is a
        list of words (unicode strings) that will be used for training.
        The `sentences` iterable can be simply a list, but for larger corpora,
		consider an iterable that streams the sentences directly from disk/network.
        See :class:`BrownCorpus`, :class:`Text8Corpus` or :class:`LineSentence` in
        this module for such examples.
        If you don't supply `sentences`, the model is left uninitialized -- use if
        you plan to initialize it in some other way.
        `sg` defines the training algorithm. By default (`sg=0`), CBOW is used.
        Otherwise (`sg=1`), skip-gram is employed.

        `size` is the dimensionality of the feature vectors.

        `window` is the maximum distance between the current and predicted word within a sentence.

        `alpha` is the initial learning rate (will linearly drop to `min_alpha` as training progresses).

        `seed` = for the random number generator. Initial vectors for each
        word are seeded with a hash of the concatenation of word + str(seed).

        Note that for a fully deterministically-reproducible run, you must also limit the model to
        a single worker thread, to eliminate ordering jitter from OS thread scheduling. (In Python
        3, reproducibility between interpreter launches also requires use of the PYTHONHASHSEED
        environment variable to control hash randomization.)

        `min_count` = ignore all words with total frequency lower than this.

        `max_vocab_size` = limit RAM during vocabulary building; if there are more unique
        words than this, then prune the infrequent ones. Every 10 million word types
        need about 1GB of RAM. Set to `None` for no limit (default).

        `sample` = threshold for configuring which higher-frequency words are randomly downsampled;
            default is 1e-3, useful range is (0, 1e-5).

        `workers` = use this many worker threads to train the model (=faster training with multicore machines).

        `hs` = if 1, hierarchical softmax will be used for model training.

        If set to 0 (default), and `negative` is non-zero, negative sampling will be used.

        `negative` = if > 0, negative sampling will be used, the int for negative
        specifies how many "noise words" should be drawn (usually between 5-20).

        Default is 5. If set to 0, no negative samping is used.

        `cbow_mean` = if 0, use the sum of the context word vectors. If 1 (default), use the mean.
        Only applies when cbow is used.

        `hashfxn` = hash function to use to randomly initialize weights, for increased
        training reproducibility. Default is Python's rudimentary built in hash function.

        `iter` = number of iterations (epochs) over the corpus. Default is 5.

        `trim_rule` = vocabulary trimming rule, specifies whether certain words should remain
        in the vocabulary, be trimmed away, or handled using the default (discard if word count < min_count).
        Can be None (min_count will be used), or a callable that accepts parameters (word, count, min_count) and
        returns either `utils.RULE_DISCARD`, `utils.RULE_KEEP` or `utils.RULE_DEFAULT`.
        Note: The rule, if given, is only used to prune vocabulary during build_vocab() and is not stored as part
        of the model.

        `sorted_vocab` = if 1 (default), sort the vocabulary by descending frequency before
        assigning word indexes.

        `batch_words` = target size (in words) for batches of examples passed to worker threads (and
        thus cython routines). Default is 10000. (Larger batches will be passed if individual
        texts are longer than 10000 words, but the standard cython code truncates to that maximum.)
        """
		#sentences = [['anarchism', 'is', 'a', 'political', 'philosophy', 'that', 'advocates', 'self-governed', 'societies', 'based', 'on', 'voluntary', 'institutions'], ['anarchism', 'holds', 'the', 'state', 'to', 'be', 'undesirable', 'unnecessary', 'and', 'harmful']]
		#print 'Build Word2Vec'
		self.w2v = Word2Vec(sentences=convertSentencesType(sentences), size=size, alpha=alpha, window=window, min_count=min_count, max_vocab_size=max_vocab_size, sample=sample, seed=seed, workers=workers, min_alpha=min_alpha, sg=sg, hs=hs, negative=negative, cbow_mean=cbow_mean, iter=iter, null_word=null_word, sorted_vocab=sorted_vocab)

	def save(self, *args, **kwargs):
		self.w2v.save(*args, **kwargs)

	def load(self, *args, **kwargs):
		self.w2v = Word2Vec.load(*args, **kwargs)

	def build_vocab(self, sentences, progress_per=10000, update=False):
		"""
		Build vocabulary from a sequence of sentences (can be a once-only generator stream).
		Each sentence must be a list of unicode strings.
		"""
		self.w2v.build_vocab(convertSentencesType(sentences), progress_per=progress_per, update=update)

	def train(self, sentences, epochs=None, start_alpha=None, end_alpha=None, word_count=0, queue_factor=2, report_delay=1.0):
		"""
        Update the model's neural weights from a sequence of sentences (can be a once-only generator stream).
        For Word2Vec, each sentence must be a list of unicode strings. (Subclasses may accept other examples.)

        To support linear learning-rate decay from (initial) alpha to min_alpha, and accurate
        progres-percentage logging, either total_examples (count of sentences) or total_words (count of
        raw words in sentences) MUST be provided. (If the corpus is the same as was provided to
        `build_vocab()`, the count of examples in that corpus will be available in the model's
        `corpus_count` property.)

        To avoid common mistakes around the model's ability to do multiple training passes itself, an
        explicit `epochs` argument MUST be provided. In the common and recommended case, where `train()`
        is only called once, the model's cached `iter` value should be supplied as `epochs` value.
        """	
		return self.w2v.train(sentences=convertSentencesType(sentences), total_examples=self.w2v.corpus_count, epochs=self.w2v.iter)

	def build_vocab_file(self, fname, progress_per=10000, update=False):
		sentences = LineSentence(fname)
		self.build_vocab(sentences=sentences, progress_per=progress_per, update=update)		

	def train_file(self, fname, epochs=None, start_alpha=None, end_alpha=None, word_count=0, queue_factor=2, report_delay=1.0):
		sentences = LineSentence(fname)
		self.train(sentences, epochs=epochs, start_alpha=start_alpha, end_alpha=end_alpha, word_count=word_count, queue_factor=queue_factor, report_delay=report_delay)

	def build_vocab_file(self, fname,  min_count=None, progress_per=10000, update=False):
		sentences = LineSentence(fname)
		self.w2v.scan_vocab(sentences=sentences, progress_per=progress_per)  # initial survey
		self.w2v.scale_vocab(min_count=min_count, update=update)  # trim by min_count & precalculate downsampling
		self.w2v.finalize_vocab(update=update)

	def getVocabSize(self):
		return len(self.w2v.wv.vocab)

	def getVector(self, word):
		return self.w2v.wv[word].tolist()

	def getVocab(self):
		return [word for word in self.w2v.wv.vocab]

	def getVectorVocab(self):
		wv={}
		for word in self.w2v.wv.vocab:
			wv[word] = self.w2v.wv[word].tolist()
		return wv

	def isWordInVocab(self, word):
		return word in self.w2v.wv.vocab

