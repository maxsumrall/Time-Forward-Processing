import subprocess
import time

startTime = time.time()
#subprocess.call("sort test25regular-edges -n -o \"trashsorted.txt\" -T /scratch2 ", shell=True)
subprocess.call("sort test500regular-edges -n -o trash", shell=True)
print "running time" 
print time.time()-startTime
subprocess.call("rm trash",shell=True)
