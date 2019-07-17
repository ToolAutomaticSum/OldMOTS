#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import os
from shutil import copy
import xml.etree.ElementTree as ET


def read_conf_file(conf_file, output_dir):
    tree = ET.parse(conf_file)
    root = tree.getroot()

    for task in root:
        for multicorpus in task:
            size = len(multicorpus)
            print(size)
            i = 0
            for corpus in multicorpus:
                input_path = corpus.find('SUMMARY_PATH').text
                doc = corpus.find('SUMMARY').text
                input_file = os.path.join(input_path, doc)
                if not os.path.isfile(os.path.join(output_dir, doc)):
                   copy(input_file, output_dir)
                i+=1
                if i%1000 == 0:
                    print("{0:.2f}% processed files.".format(float(i)*100/size))
                    # print(str(float(i)*100/size) + "% processed files.")


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print("USAGE: python read_conf_file.py <conf_file> <output_dir>")
        sys.exit()
    conf_file = sys.argv[1]
    output_dir = sys.argv[2]

    read_conf_file(conf_file, output_dir)


