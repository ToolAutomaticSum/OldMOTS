#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import xml.etree.ElementTree as ET
import os
import itertools
import re


if __name__ == '__main__':
    if len(sys.argv) != 4:
        print("USAGE: python transcript_stemmed_to_original.py <stories_dir> \
              <MOTS_dir> <output_dir>")
        sys.exit()
    path_1 = sys.argv[1]
    path_2 = sys.argv[2]
    path_output = sys.argv[3]


    files_pg = [f for f in os.listdir(path_1) if os.path.isfile(os.path.join(path_1, f))]
    files_mots = [f for i, f in enumerate(os.listdir(path_2)) if i > 74083 if
                  os.path.isfile(os.path.join(path_2, f))]

    if len(files_pg) != len(files_mots):
        print("Pointer generator got " + str(len(files_pg)) + "files while \
              mots got " + str(len(files_mots)) + "files.")
        # sys.exit()

    for path_pg, path_mots in zip(files_pg, files_mots):
        f_pg = open(os.path.join(path_1, path_pg), 'r')
        f_output = open(os.path.join(path_output, path_mots), 'w')
        tree = ET.parse(os.path.join(path_2, path_mots))
        doc = tree.getroot()

        for sentence_pg, sentence_mots in zip(f_pg, doc):
            for node in sentence_mots:
                if node.tag == 'stemmed':
                    stemmed = re.sub('%%', '', node.text.strip())
                elif node.tag == 'original':
                    original = node.text.strip()
            if stemmed == sentence_pg:
                f_output.write(original)
