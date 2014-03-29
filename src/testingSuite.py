import subprocess

nValues = [1000,10000,100000,1000000,5000000,10000000,20000000,30000000]

mValues = [100000]

outputFile = open("results.txt","w")

for n in nValues:
  result = subprocess.check_output("java Main " + str(n) + " " + str(mValues[0]), shell=True)
  print str(n) + ": " + result
  outputFile.write("N = " + str(n) + ":" + result + "\n")

outputFile.close()
