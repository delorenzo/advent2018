#!/usr/bin/env python
import re
import sys

input = open("input.txt", "rb")
twiceCount = 0
thriceCount = 0
dict = {}
for line in input:
	twiceCounted = False
	thriceCounted = False
	print line
	for letter in line:
		if letter in dict:
			dict[letter] += 1
		else:
			dict[letter] = 1
	for letter, count in dict.items():
		if count == 2 and not twiceCounted:
			twiceCount += 1
			twiceCounted = True
			print "Twicecount++"
		elif count == 3 and not thriceCounted:
			thriceCount += 1
			thriceCounted = True
			print "Thricecount++"
	dict.clear()
print twiceCount * thriceCount
input.close()