#!/usr/bin/env python
import re
import sys

CLOTH_SIZE = 1000
nonoverlap = []
inputParser = re.compile('#([0-9]+) @ ([0-9]+),([0-9]+): ([0-9]+)x([0-9]+)')
cloth = [[0 for x in range(CLOTH_SIZE)] for y in range(CLOTH_SIZE)]
doubleClaimed = 0
with open("input.txt", "r") as input:
	for line in input:
		overlap = False
		# get input 
		match = inputParser.match(line)
		claimId = int(match.group(1))
		leftDistance =int(match.group(2))
		topDistance = int(match.group(3))
		width = int(match.group(4))
		height = int(match.group(5))

		#record lines
		for x in range(leftDistance, leftDistance+width):
			for y in range(topDistance, topDistance+height):
				if cloth[x][y] >= 1:
					overlap = True
					if cloth[x][y] in nonoverlap:
						nonoverlap.remove(cloth[x][y])
					cloth[x][y] = 2
				else:
					cloth[x][y] = claimId
		if not overlap:
			nonoverlap.append(claimId)
for x in range(CLOTH_SIZE):
	line = ""
	for y in range(CLOTH_SIZE):
		line += str(cloth[x][y])
		if cloth[x][y] >= 2:
			doubleClaimed += 1
	print(line)
print("The square inches of double claimed fabric is %d" % doubleClaimed)
print("The number of nonoverlapping items is %d" % len(nonoverlap))
print("The non-overlapping cliaim is %d" % nonoverlap[0])



