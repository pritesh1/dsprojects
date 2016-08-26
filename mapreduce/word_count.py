
import mrjob,os
from mrjob.job import MRJob
from mrjob.step import MRStep
import heapq
import re

WORD_RE = re.compile(r"[\w]+")


N=100

class MRWordCount(MRJob):

  def mapper(self, _, line):
    for word in WORD_RE.findall(line):
      yield (word.lower(), 1)

  def combiner(self,word,counts):
  	yield (word, sum(counts))    

  def reducer(self, word, counts):
  	yield (word, sum(counts))

  def mapper_global_init(self):
  	#self.line=[]
  	self.heap=[]

  def mapper_global(self,word,counts):
  	self.increment_counter("mapper_global_heapify", "number of words", amount=1)
  	#yield (word,counts)
  	heapq.heappush(self.heap,(counts,word))
        if len(self.heap) > N:
            heapq.heappop(self.heap)

  def mapper_global_final(self):
  	for (count,word) in self.heap:
            yield (word,count)          





  def reducer_global(self,word,counts):
  	self.increment_counter("reducer_global", "number of words", amount=1)
  	yield (word,sum(counts))	


  def globalTopN_mapper(self,word,count):
  	yield "Top"+str(N), (word,count)

  # The reducer ignores the key ("TopN"), and just finds the largest of the values.
  def globalTopN_reducer(self,_,countsAndWords):
  	for countAndWord in heapq.nlargest(N,countsAndWords):
  		yield (countAndWord)

  #def reducer_final(self,countsAndWords):
  	#for countAndWord in heapq.nlargest(N,countsAndWords):
            #yield (countAndWord)

  def steps(self):
        return [
            MRStep(
            		mapper=self.mapper,
            		combiner=self.combiner,
                    reducer=self.reducer
                   ),
            MRStep(
            		mapper_init=self.mapper_global_init,
            		mapper=self.mapper_global,
            		mapper_final=self.mapper_global_final,
            		reducer=self.reducer_global
            		#reducer_final=self.reducer_final
            	  ),
            MRStep(mapper=self.globalTopN_mapper,
                   reducer=self.globalTopN_reducer)


            ]

if __name__ == '__main__':
  	MRWordCount.run()