import networkx as nx
from matplotlib import pyplot as plt
import random
import sys
###
if (len(sys.argv) != 3):
  print "Usage: \n > python graph.py nodeFileName edgeFileName"
  exit()
###

G = nx.Graph()
nodesFile = open(str(sys.argv[1]),"r")
edgesFile = open(str(sys.argv[2]),"r")
weights = [0.4,0.5,0.6,0.7,0.8,0.9,1,2]
nodes = {}
for line in nodesFile:
  line = [int(x) for x in line.split()]
  nodes[line[0]] = [line[1],line[2]]
  G.add_node(line[0])

for line in edgesFile:
  origin = [int(x) for x in line.split()]
  dest = origin[1]
  origin = origin[0]
  weight = weights[random.randint(0,len(weights)-1)] #random for testing
  #weight = line[2] #if weight is in file
  G.add_edge(origin,dest,weight=weight)


#pos=nx.random_layout(G) # positions for all nodes

ew = []

for i in weights:
  ew.append([(u,v) for (u,v,d) in G.edges(data=True) if d['weight'] == i])


#for i in range(len(G.edges())):
#  ew[G.edges[i]]



for i in zip(ew,weights):
  nx.draw_networkx_edges(G,pos=nx.spring_layout(G,iterations = 0,weight=1,pos=nodes),edgelist=i[0],width=i[1])

# labels
#nx.draw_networkx_labels(G,pos=nx.spring_layout(G,iterations = 0,k=500,weight=1,pos=nodes),font_size=10,font_family='sans-serif')

#plt.show()
plt.savefig("path.pdf")
print "figure saved as: path.pdf"
