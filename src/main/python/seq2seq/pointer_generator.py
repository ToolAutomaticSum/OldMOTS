# -*- coding: utf-8 -*-

"""
__author__ : Valentin Nyzam
"""

import utils

import numpy as np

import tensorflow as tf
from keras.layers import Input, Embedding, Bidirectional
from keras.layers import Dense, Lambda, Concatenate, Activation
from keras.layers import Add, Multiply, TimeDistributed
from keras.layers.recurrent import LSTM
from keras.models import Model
import keras.backend as K
from keras.preprocessing.sequence import pad_sequences

class PointerGenerator(object):
    def __init__(self, config):
        self.vocab_size = config['vocab_size']
        self.max_input_seq_length = config['max_input_seq_length']
        self.max_target_seq_length = config['max_target_seq_length']
        self.max_seq_length = max(self.max_input_seq_length,
                                  self.max_target_seq_length)
        self.word2id = config['word2id']
        self.id2word = config['id2word']

        nb_dim = 300
        hidden_units = 64
        print('Max input length  : %s\nMax target length : %s\nNb dim            : %s\nHidden units      : %s' % (self.max_input_seq_length,
                                         self.max_target_seq_length, nb_dim,
                                         hidden_units))

        emb_layer = Embedding(input_dim=self.vocab_size, output_dim=nb_dim,
                              # input_length=self.max_seq_length,
                              name='embedding')

        # Encoder layer
        enc_input = Input(shape=(self.max_input_seq_length, ), name='enc_input')
        print('Encoder input : %s' % enc_input.shape)
        enc_emb = emb_layer(enc_input)
        print('Encoder embedding : %s' % enc_emb.shape)

        enc_bilstm, fw_h, fw_c, bw_h, bw_c = Bidirectional(LSTM(hidden_units, return_state=True,
                                                                return_sequences=True),
                                                           input_shape=(nb_dim, 1),
                                                           merge_mode='concat',
                                                           name='enc_bilstm')(enc_emb)
        print('Encoder output : %s' % enc_bilstm.shape)

        # Decoder layer
        dec_input = Input(shape=(1, ), name='dec_input')
        print('Decoder input : %s' % dec_input.shape)
        dec_emb = emb_layer(dec_input)

        dec_lstm, dec_h, dec_c = LSTM(hidden_units, return_state=True, name='dec_lstm')(dec_emb, initial_state=[dec_init_h,
                                                                                                                dec_init_c])
        print('Decoder  output : %s' % dec_lstm.shape)
        dec_lstm = TimeDistributed(Dense(1))(dec_lstm)
        print('Decoder  output : %s' % dec_lstm.shape)

        # Attention layer
        dec_attention = tf.expand_dims(tf.expand_dims(dec_lstm, 1), 1)
        # print('Decoder attention : %s' % dec_attention.shape)
        enc_attention = tf.expand_dims(enc_bilstm, 2)
        # print('Encoder attention : %s' % enc_attention.shape)
        dec_attention = Dense(hidden_units, activation='linear',
                              use_bias=True,
                              name='dec_attention')(dec_attention)
        print('Decoder attention : %s' % dec_attention.shape)
        enc_attention = Dense(hidden_units, activation='linear',
                              use_bias=True,
                              name='enc_attention')(enc_attention)
        print('Encoder attention : %s' % enc_attention.shape)

        attention = Add()([enc_attention, dec_attention])
        # attention = RepeatVector(max_seq_length)(attention)
        # attention = Dense(1, activation='tanh')(enc_lstm)
        attention = Dense(hidden_units, activation='tanh')(attention)
        attention = Dense(hidden_units, activation='linear')(attention)
        attention = Dense(hidden_units, activation='softmax')(attention)
        # attention = Flatten()(attention)
        # attention = Permute([2, 1])(attention)
        print('Attention : %s' % attention.shape)

        # Context vector
        c_vector = Multiply()([attention, enc_bilstm])
        print('Context vector : %s' % c_vector.shape)
        c_vector = Lambda(lambda xin: K.sum(xin, axis=-2),
                          output_shape=(hidden_units,))(c_vector)
        print('Context vector : %s' % c_vector.shape)

        # Vocabulary distribution
        p_vocab = Concatenate()([c_vector, dec_lstm])
        p_vocab = Dense(hidden_units, activation='linear', use_bias=True)(p_vocab)
        p_vocab = Dense(self.vocab_size, activation='softmax')(p_vocab)
        print('Vocab dists : %s' % self.vocab_size)

        # Generation probability
        # p_gen = Add()([c_vector, dec_lstm])
        # p_gen = Dense(1, activation='sigmoid')(p_gen)

        self.model = Model([enc_input, dec_input], p_vocab)
        self.model.compile(optimizer='adagrad', loss='kullback_leibler_divergence')
        print(self.model.summary())


    def transform_text(self, texts, max_seq_length):
        temp = []
        for text in texts:
            x = []
            for word in text.lower().split():
                wid = 1
                if word in self.word2id:
                    wid = self.word2id[word]
                x.append(wid)
                if len(x) >= max_seq_length:
                    break
            temp.append(x)
        return pad_sequences(temp, maxlen=max_seq_length)


    def generate_data(self, X, Y):
        encoder_input_data = np.zeros((len(X), self.max_input_seq_length),
                                       dtype=np.float32)
        decoder_input_data = np.zeros((len(X), self.max_target_seq_length),
                                       dtype=np.float32)
        decoder_target_data = np.zeros((len(X), self.max_target_seq_length,
                                        self.vocab_size),
                                       dtype=np.float32)

        for i, (input_text, target_text) in enumerate(zip(X, Y)):
            for t, word in enumerate(input_text):
                encoder_input_data[i, t] = word
            for t, word in enumerate(target_text):
                decoder_input_data[i, t] = word

                if t > 0:
                    decoder_target_data[i, t-1, word] = 1
        return encoder_input_data, decoder_input_data, decoder_target_data


    def fit(self, X, Y, epochs=None, batch_size=None):

        Y = self.transform_text(Y, self.max_target_seq_length)
        X = self.transform_text(X, self.max_input_seq_length)

        encoder_input_data, decoder_input_data, decoder_target_data = self.generate_data(X, Y)

        history = self.model.fit([encoder_input_data, decoder_input_data],
                                 decoder_target_data,
                                 batch_size=batch_size,
                                 epochs=epochs,
                                 validation_split=0.2)


if __name__== '__main__':
    from sklearn.model_selection import train_test_split

    print('loading data...')
    X, Y = utils.load_data('/home/valnyz/MOTS/output/temp/stories_cnn/', 200)

    print('extract configuration from input texts ...')
    X, Y, config = utils.fit_text(X, Y, stemmed=False)

    X, Xtest, Y, Ytest = train_test_split(X, Y, test_size=0.2, random_state=42)

    # Training
    summarizer = PointerGenerator(config)

    history = summarizer.fit(X, Y, epochs=25, batch_size=20)


    # Summarization
    model_dir_path = './models'

    config = np.load(Seq2SeqSummarizer.get_config_file_path(model_dir_path=model_dir_path)).item()

    summarizer = Seq2SeqSummarizer(config)
    summarizer.load_weights(weight_file_path=Seq2SeqSummarizer.get_weight_file_path(model_dir_path=model_dir_path))

    print('start predicting ...')
    for i in range(20):
        x = Xvalidation[i]
        actual_headline = Yvalidation[i]
        headline = summarizer.summarize(x)
        print('Article: ', x)
        print('Generated Headline: ', headline)
        print('Original Headline: ', actual_headline)

