#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import argparse

def read_text_file(text_file):
	lines = []
	with open(text_file, "r") as f:
		for line in f:
			lines.append(line.strip())
	return lines

def generate_abstract(_input, _output, story_file):
    lines = read_text_file(os.path.join(_input, story_file))

    # Lowercase everything
    lines = [line.lower() for line in lines]

    highlights = []
    next_is_highlight = False
    for idx,line in enumerate(lines):
        if line == "":
            continue # empty line
        elif line.startswith("@highlight"):
            next_is_highlight = True
        elif next_is_highlight:
            highlights.append(line)

    abstract = '\n'.join([sent for sent in highlights])
    with open(os.path.join(_output, story_file.split('.')[0] + '.sum'), "w") as w:
        w.write(abstract)

def parse_args():
    parser = argparse.ArgumentParser(description='Generate summary from a \
                                     list of highlighted sentences.')
    parser.add_argument('-i', '--input', dest='input',  required=True,
                        help='Folder containing files to clean.')
    parser.add_argument('-o', '--output', dest='output', default='output',
                        help='Folder containing summary files associate to \
                        input files.')
    return parser.parse_args()


if __name__ == '__main__':
    args = parse_args()

    i = 0
    for root, dirs, files in os.walk(args.input):
        for filename in files:
            if i%1000 == 0:
                print(str(i) + 'th file processed.')
            generate_abstract(args.input, args.output, filename)
            i+=1
