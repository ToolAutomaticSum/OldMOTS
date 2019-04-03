#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import os
import struct

import collections

from tensorflow.core.example import example_pb2

# We use these to separate the summary sentences in the .bin datafiles
SENTENCE_START = '<s>'
SENTENCE_END = '</s>'


VOCAB_SIZE = 150000

# finished_files_dir = os.path.join('temp', 'serialized')
finished_files_dir = 'serialized'

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


def write_to_bin(story_folder, out_file, makevocab=False):
    story_fnames = [f for f in os.listdir(story_folder) if os.path.isfile(os.path.join(story_folder, f))]
    num_stories = len(story_fnames)

    if makevocab:
        vocab_counter = collections.Counter()

    with open(out_file, 'wb') as writer:
        for idx, story_file in enumerate(story_fnames):
            if idx % 1000 == 0:
                print("Writing story %i of %i; %.2f percent done" % (idx, num_stories, float(idx)*100.0/float(num_stories)))

            # Get the strings to write to .bin file
            article, abstract = get_art_abs(os.path.join(story_folder, story_file))

            # Write to tf.Example
            tf_example = example_pb2.Example()
            tf_example.features.feature['article'].bytes_list.value.extend([article.encode()])
            tf_example.features.feature['abstract'].bytes_list.value.extend([abstract.encode()])
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

    print("Finished writing file %s\n" % out_file)

    # write vocab to file
    if makevocab:
        print("Writing vocab file...")
        with open(os.path.join(finished_files_dir, "vocab"), 'w') as writer:
            for word, count in vocab_counter.most_common(VOCAB_SIZE):
                writer.write(word + ' ' + str(count) + '\n')
        print("Finished writing vocab file")


if __name__ == '__main__':
    if len(sys.argv) != 2:
        print("USAGE: python serialize_data.py <stories_dir>")
        sys.exit()
    stories_dir = sys.argv[1]
    # stories_dir = sys.argv[2]

    # Create some new directories
    if not os.path.exists(finished_files_dir): os.makedirs(finished_files_dir)

    # Read the tokenized stories, do a little postprocessing then write to bin files
	# write_to_bin(stories_dir, os.path.join(finished_files_dir, "test.bin"), makevocab=False)
    write_to_bin(stories_dir, os.path.join(finished_files_dir,
                                           "train.bin"), makevocab=True)
