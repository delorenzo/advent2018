#!/usr/bin/env python
import re
import sys
from operator import itemgetter


inputParser = re.compile('\[[0-9]{4}\-([0-9]+\-[0-9]+) [0-9]+:([0-9]+)\] (.*)')
beginShiftParser = re.compile('Guard #([\d]+) begins shift')

def printSleep(sleep: [], date, guard):
	out = ''
	for x in sleep:
		out += x
	print("%s  #%04d  %s" % (date, guard, out))

log_tuples = []
sleep_times = {}
sleep_minutes = {}

with open("input.txt", "r") as input:
	print("Date   ID     Minute")
	print("              000000000011111111112222222222333333333344444444445555555555")
	print("              012345678901234567890123456789012345678901234567890123456789")
	for line in input:
		# get input
		match = re.match(inputParser, line)
		date, minute, action = match.group(1), int(match.group(2)), match.group(3)
		log_tuples.append((date, minute, action))
	log_tuples = sorted(log_tuples, key=itemgetter(0, 1))

current_guard = -2
sleep = ['.' for x in range(60)]
sleeping = False
lastMinute = 0
lastDate = 0
for tup in log_tuples:
	#print("%s %d %s" % (tup[0], tup[1], tup[2]))
	beginShift = re.match(beginShiftParser, tup[2])
	if beginShift:
		if current_guard != -2:
			printSleep(sleep, lastDate, current_guard)
		current_guard = int(beginShift.group(1))
		sleep = ['.' for x in range(60)]
		sleeping = False
	elif lastDate != tup[0]:
		printSleep(sleep, lastDate, current_guard)

	if sleeping:
		#print("%d:%d" % (lastMinute, tup[1]))
		for x in range(lastMinute, tup[1]):
			sleep[x] = '#'
			if current_guard in sleep_times:
				sleep_times[current_guard] += 1
			else:
				sleep_times[current_guard] = 1

			if current_guard in sleep_minutes:
				if x in sleep_minutes[current_guard]:
					sleep_minutes[current_guard][x] += 1
				else:
					sleep_minutes[current_guard][x] = 1
			else:
				sleep_minutes[current_guard] = {x: 1}
	if tup[2] == "falls asleep":
		sleeping = True
	else: 
		sleeping = False
	lastMinute = tup[1]
	lastDate = tup[0]
printSleep(sleep, lastDate, current_guard)

#sleepyGuard = max(sleep_times.items(), key=itemgetter(1))[0]
#print("The guard with the most time asleep is %d" % sleepyGuard)
sleepiestGuard = 0
sleepiestMinute = 0
for guard in sleep_minutes:
	sleepyMinute = max(sleep_minutes[guard].items(), key=itemgetter(1))[0]
	if sleepyMinute > sleepiestMinute:
		sleepiestMinute = sleepyMinute
		sleepiestGuard = guard
#sleepyMinute = max(sleep_minutes[sleepyGuard].items(), key=itemgetter(1))[0]
#print("The minute that guard was most often asleep was %s" % sleepyMinute)
#print("The answer is %d" % (sleepyGuard * sleepyMinute))
print("Guard %d slept at %d a bunch of times" % (sleepiestGuard, sleepiestMinute))
print("The answer is %d" % (sleepiestGuard * sleepiestMinute))
		


