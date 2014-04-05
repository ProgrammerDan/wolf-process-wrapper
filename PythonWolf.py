import sys, random

# init
def init(initStr):
	sys.stdout.write('K%s\n'%initStr[:2])
	sys.stdout.flush()

def attack(atkStr):
	if (atkStr[2] == 'L'):
		atk='S'
	elif (atkStr[2] == 'S'):
		atk='P'
	elif (atkStr[2] == 'B'):
		atk='S'
	elif (atkStr[2] == 'W'):
		atk='R'
	sys.stdout.write('%s%s\n'%(atk, atkStr[:2]))
	sys.stdout.flush()

def move(mvStr):
	moves = 'HUDRL'
	sys.stdout.write('%s%s\n'%(moves[random.randint(0,4)],mvStr[:2]))
	sys.stdout.flush()


def dispatch():
	ftr = sys.stdin.readline()
	if ftr:
		cmd = ftr[0]
		if cmd == 'S':
			init(ftr[1:])
		elif cmd == 'A':
			attack(ftr[1:])
		elif cmd == 'M':
			move(ftr[1:])

while(True):
	dispatch()
