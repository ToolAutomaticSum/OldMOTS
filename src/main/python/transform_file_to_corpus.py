#!/usr/bin/env python
# -*- coding: utf-8 -*-


import argparse
import xml.etree.ElementTree as ET
from xml.dom import minidom
import os


def write_html(_input, _summary_path, _output):
    # create the file structure
    config = ET.Element('CONFIG')
    task = ET.SubElement(config, 'TASK')
    task.set('ID','0')
    multicorpus = ET.SubElement(task, 'MULTICORPUS')
    multicorpus.set('ID','0')

    i = 0
    for root, dirs, files in os.walk(_input):
        for filename in files:
            if i%1000 == 0:
                print(str(i) + 'th file processed.')
            corpus = ET.SubElement(multicorpus, 'CORPUS')
            corpus.set('ID', str(i))
            input_path = ET.SubElement(corpus, 'INPUT_PATH')
            input_path.text = _input
            document = ET.SubElement(corpus, 'DOCUMENT')
            document.set('ID', '0')
            document.text = filename
            summary_path = ET.SubElement(corpus, 'SUMMARY_PATH')
            summary_path.text = _summary_path
            summary = ET.SubElement(corpus, 'SUMMARY')
            summary.set('ID', '0')
            summary.text = filename.split('.')[0] + '.sum'
            i+=1

    # create a new XML file with the results
    xmlstr = minidom.parseString(ET.tostring(config)).toprettyxml(indent='\t')
    with open(_output, "w") as f:
        f.write(xmlstr)


def parse_args():
    parser = argparse.ArgumentParser(description='Generate conf file for a \
                                     list of file in a folder as a list of \
                                     corpus.')
    parser.add_argument('-i', '--input', dest='input',  required=True,
                        help='Folder containing files to clean.')
    parser.add_argument('-o', '--output', dest='output', default='output',
                        help='Output folder.')
    parser.add_argument('-s', '--summary', dest='summary', required=True,
                        help='Folder containing summary files associate to \
                        input files.')
    return parser.parse_args()


if __name__ == '__main__':
    args = parse_args()

    write_html(args.input, args.summary, args.output)

