import subprocess

nValues = [1500000, 3000000, 6000000]
fileName = ["randomEdges1M","randomEdges3M","randomEdges6M"]
mValues = [30000,50000,70000]

with open("results.txt","w") as outputFile:
    for n in zip(nValues,fileName):
        for i in range(1):
            command = "java -server Main " + str(n[1]) + " " + str(n[0]) + " DPUnsafe"
            print command
            result = subprocess.check_output(command, shell=True)
            print "\t " + result
            outputFile.write(command + "\t" + result + "\n")

    for n in zip(nValues,fileName):
        for m in mValues:
            for i in range(1):
                command = "java -server Main " + str(n[1]) + " " + str(n[0]) + " TFP " + str(m)
                print command
                result = subprocess.check_output(command, shell=True)
                print "\t " + result
                outputFile.write(command + "\t" + result + "\n")



