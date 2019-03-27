
from tensorflow.core.example import example_pb2

# We use these to separate the summary sentences in the .bin datafiles
SENTENCE_START = '<s>'
SENTENCE_END = '</s>'

def read_text_file(text_file):
	lines = []
	with open(text_file, "r") as f:
		for line in f:
			lines.append(line.strip())
	return lines

def get_art_abs(story_file):
	lines = read_text_file(story_file)
	
	# Lowercase everything
	lines = [line.lower() for line in lines]

	# Separate out article and abstract sentences
	article_lines = []
	highlights = []
	next_is_highlight = False
	for idx,line in enumerate(lines):
		if line == "":
			continue # empty line
		elif line.startswith("@highlight"):
		  	next_is_highlight = True
		elif next_is_highlight:
		  	highlights.append(line)
		else:
		  	article_lines.append(line)

  	# Make article into a single string
  	article = ' '.join(article_lines)

  	# Make abstract into a single string, putting <s> and </s> tags around the sentences
   	abstract = ' '.join(["%s %s %s" % (SENTENCE_START, sent, SENTENCE_END) for sent in highlights])

  	return article, abstract


def write_to_bin(story_file, makevocab=False):
	# Get the strings to write to .bin file
	article, abstract = get_art_abs(story_file)
	
	# Write to tf.Example
	tf_example = example_pb2.Example()
	tf_example.features.feature['article'].bytes_list.value.extend([article])
	tf_example.features.feature['abstract'].bytes_list.value.extend([abstract])
	tf_example_str = tf_example.SerializeToString()
	str_len = len(tf_example_str)
	writer.write(struct.pack('q', str_len))
	writer.write(struct.pack('%ds' % str_len, tf_example_str))
	
	# Write the vocab to file, if applicable
	if makevocab:
		art_tokens = article.split(' ')
		abs_tokens = abstract.split(' ')
		abs_tokens = [t for t in abs_tokens if t not in [SENTENCE_START, SENTENCE_END]] # remove these tags from vocab
		tokens = art_tokens + abs_tokens
		tokens = [t.strip() for t in tokens] # strip
		tokens = [t for t in tokens if t!=""] # remove empty
		vocab_counter.update(tokens)
