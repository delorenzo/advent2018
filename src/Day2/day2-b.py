#!/usr/bin/env python
import re
import sys

#abc
#- bc
#a - c
#ab -

#abcd
#- bcd
#a - cd
#ab - d
#abc -
input = open("input.txt", "rb")
split = {}
split[""] = []
for line in input:
	line = line.strip()
	size = len(line)
	for i in range(size):
		if i == 0:
			tail = line[1:size]
			print "start:" + tail
			if tail in split[""]:
				print "The answer is " + tail
				sys.exit()
			else:
				split[""] = [tail]
		elif i == size-1:
			head = line[:size-1]
			print "end:" + head
			if head in split:
				print "The answer is " + head
				sys.exit()
			else:
				split[head] = []
		else:
			head = line[0:i]
			tail = line[i+1:size]
			print "head: " + head + " tail: " + tail
			if (head in split) and (tail in split[head]):
				print "The answer is " + head + tail
				sys.exit()
			else:
				if head in split:
					headList = split[head]
					headList.append(tail)
					split[head] = headList
				else:
					split[head] = [tail]
input.close()