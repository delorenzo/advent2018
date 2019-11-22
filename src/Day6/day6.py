#!/usr/bin/python
import re
import sys
from string import ascii_uppercase
import operator

parser = re.compile("(\d+), (\d+)")

def prettyPrintMap( map ):
	for x in map:
		print(x)

def calcDistance(x1, y1, x2, y2):
	return abs(x2-x1) + abs(y2-y1)

def shortestLetter( map, letter_indices, x, y):
	shortestDistance = sys.maxsize
	shortestLetter = ""
	valid = False
	for key, value in letter_indices.items():
		distance = calcDistance(x, y, value[0], value[1])
		if distance == shortestDistance:
			valid = False
		elif (distance < shortestDistance):
			shortestDistance = distance
			shortestLetter = key
			valid = True
	if valid:
		return shortestLetter
	else: 
		return '.'

map = [['.']*10 for x in range(10)]
letter_index = 0
largest_area = 0
letter_indices={}
counts = {}
min = [sys.maxsize, sys.maxsize]
max = [0, 0]
with open("input.txt", "r") as input:
	for line in input:
		match = re.match(parser, line)
		y, x = int(match.group(1)), int(match.group(2))
		if x > max[0]:
			max[0] = x
		if y > max[1]:
			max[1] = y
		if (x > len(map)):
			for i in range(x - len(map) + 1):
				map.append(['.'] * len(map[0]))
		if (y > len(map[0])):
			for i in range(len(map)):
				for j in range(y - len(map[i]) + 1):
					map[i].append('.')
		#print("%d %d %d %d" % (x, y, len(map), len(map[x])))
		map[x][y] = letter_index
		letter_indices[letter_index] = [x,y]
		letter_index += 1
#prettyPrintMap(map)
for x in range(len(map)):
	for y in range(len(map[x])):
		if map[x][y] == '.':
			letter = shortestLetter(map, letter_indices, x, y)
			map[x][y] = letter
			if letter in counts:
				counts[letter] += 1
			else:
				counts[letter] = 2
print(min)
print(max)
del counts['.']
for x in range(0, len(map)):
	if map[x][0] in counts:
		letter = map[x][0]
		del counts[letter]
		print("Removing %s" % letter)
	if map[x][len(map[x])-1] in counts:
		letter = map[x][len(map[x])-1]
		del counts[letter]
		print("Removing %s" % letter)
for y in range(0, len(map[0])):
	if map[0][y] in counts:
		letter = map[0][y]
		del counts[letter]
		print("Removing %s" % letter)
	if map[len(map)-1][y] in counts:
		letter = map[len(map)-1][y]
		del counts[letter]
		print("Removing %s" % letter)
best = 0
for x in counts.values():
	if x > best:
		best = x
print(counts)
print(best)

size = 0
threshold = 10000
for x in range(0, len(map)):
	for y in range (0, len(map[0])):
		sum = 0
		for loc in letter_indices.values():
			sum += calcDistance(x, y, loc[0], loc[1])
		if sum < threshold:
			size += 1
print(size)