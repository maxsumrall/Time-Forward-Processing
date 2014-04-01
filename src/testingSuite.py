import subprocess

nValues = [3000000,6000000,9000000,12000000,15000000]
mValues = [10000]

outputFile = open("results.txt","w")

for n in nValues:
  result = subprocess.check_output("java -server -d64 Main " + str(n) + " " + str(mValues[0]) + " b", shell=True)
  print str(n) + ": " + result
  outputFile.write("N = " + str(n) + ":" + result + "\n")

outputFile.close()
