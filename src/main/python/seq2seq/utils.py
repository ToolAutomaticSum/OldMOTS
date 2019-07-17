# -*- coding: utf-8 -*-

"""
__author__ : Valentin Nyzam
"""

import os
import string
import re
from collections import Counter
import xml.etree.ElementTree as ET

from tqdm import tqdm


MAX_INPUT_SEQ_LENGTH = 5000
MAX_TARGET_SEQ_LENGTH = 100
VOCAB_SIZE = 50000

SENTENCE_START = '<s>'
SENTENCE_END = '</s>'


def remove_punct(s):
    return s.translate(str.maketrans('', '', string.punctuation))

def fit_text(X, Y, stemmed=True, input_seq_max_length=None, target_seq_max_length=None):
    if stemmed:
        value = 1
    else:
        value = 0
    if input_seq_max_length is None:
        input_seq_max_length = MAX_INPUT_SEQ_LENGTH
    if target_seq_max_length is None:
        target_seq_max_length = MAX_TARGET_SEQ_LENGTH
    counter = Counter()
    # input_counter = Counter()
    # target_counter = Counter()
    max_input_seq_length = 0
    max_target_seq_length = 0

    clean_X = []
    clean_Y = []

    for article_lines in X:
        # Make article into a single string
        article = ' '.join([remove_punct(sent[value]).lower() for sent in article_lines])
        clean_X.append(article)

        text = [word.lower() for word in article.split(' ')]
        seq_length = len(text)
        if seq_length > input_seq_max_length:
            text = text[0:input_seq_max_length]
            seq_length = len(text)
        for word in text:
            counter[word] += 1
            # input_counter[word] += 1
        max_input_seq_length = max(max_input_seq_length, seq_length)

    for highlights in Y:
        # Make abstract into a single string, putting <s> and </s> tags around the sentences
        abstract = SENTENCE_START + ' ' + ' '.join([remove_punct(sent[value]).lower()
                                                    for sent in highlights]) \
                   + ' ' + SENTENCE_END
        # abstract = ' '.join(["%s %s %s" % (SENTENCE_START, sent[value].lower(),
                                        # SENTENCE_END) for sent in
                             # highlights])
        clean_Y.append(abstract)

        text = [word for word in abstract.split(' ')]
        seq_length = len(text)
        if seq_length > target_seq_max_length:
            text = text[0:target_seq_max_length]
            seq_length = len(text)
        for word in text:
            counter[word] += 1
            # target_counter[word] += 1
            max_target_seq_length = max(max_target_seq_length, seq_length)

    word2id = dict()
    for idx, word in enumerate(counter.most_common(VOCAB_SIZE)):
        word2id[word[0]] =idx + 2
    word2id['PAD'] = 0
    word2id['UNK'] = 1
    id2word = dict([(idx, word) for word, idx in word2id.items()])

    # input_word2idx = dict()
    # for idx, word in enumerate(input_counter.most_common(MAX_INPUT_VOCAB_SIZE)):
        # input_word2idx[word[0]] = idx + 2
    # input_word2idx['PAD'] = 0
    # input_word2idx['UNK'] = 1
    # input_idx2word = dict([(idx, word) for word, idx in input_word2idx.items()])

    # target_word2idx = dict()
    # for idx, word in enumerate(target_counter.most_common(MAX_TARGET_VOCAB_SIZE)):
        # target_word2idx[word[0]] = idx + 1
    # target_word2idx['UNK'] = 0

    # target_idx2word = dict([(idx, word) for word, idx in target_word2idx.items()])

    # num_input_tokens = len(input_word2idx)
    # num_target_tokens = len(target_word2idx)

    config = dict()
    # config['input_word2idx'] = input_word2idx
    # config['input_idx2word'] = input_idx2word
    # config['target_word2idx'] = target_word2idx
    # config['target_idx2word'] = target_idx2word
    # config['num_input_tokens'] = num_input_tokens
    # config['num_target_tokens'] = num_target_tokens
    config['word2id'] = word2id
    config['id2word'] = id2word
    config['vocab_size'] = len(word2id)
    config['max_input_seq_length'] = max_input_seq_length
    config['max_target_seq_length'] = max_target_seq_length

    return clean_X, clean_Y, config

def load_data(folder, nb_doc = 100000):
    files = [f for i, f in enumerate(os.listdir(folder)) if i < nb_doc if
             os.path.isfile(os.path.join(folder, f))]

    print("Loading %i files. " % len(files))

    X = []
    Y = []
    for name in tqdm(files):
        article_sentence = []
        highlights = []
        path = os.path.join(folder, name)

        tree = ET.parse(path)
        doc = tree.getroot()

        for sentence in doc:
            highlight = False
            for node in sentence:
                if node.text is not None:
                    if node.tag == 'stemmed':
                        stemmed = re.sub('%%', '', node.text.strip())
                    elif node.tag == 'original':
                        original = node.text.strip()
                    elif node.tag == 'label':
                        if node.text.strip() == 'highlight':
                            highlight = True
            if highlight:
                highlights.append((original, stemmed))
            else:
                article_sentence.append((original, stemmed))
        X.append(article_sentence)
        Y.append(highlights)
    return X, Y
