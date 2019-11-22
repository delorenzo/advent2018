#!/usr/bin/env python
import re
import sys

input = open("input.txt", "rb")
output = 0
dict = {}
regex = r"^.*([+-])(\d+).*$"
first = True
while True:
	for line in input:
		match = re.search(regex, line)
		print match.group(1) + match.group(2)
		if match.group(1) == "+":
			output = output + int(match.group(2))
		else:
			output = output - int(match.group(2))
		if first:
			first = False
		elif output in dict:
			print "The answer is %d" %(output)
			sys.exit()	
		else:
			dict[output] = 1
	input.seek(0)
input.close()