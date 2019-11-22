#!/usr/bin/env python
import re
import sys

input = open("input.txt")
output = 0
regex = r"([+-])(\d+)"
for x in input:
	match = re.search(regex, x)
	if match.group(1) == "+":
		output = output + int(match.group(2))
	else:
		output = output - int(match.group(2))
input.close()
print output