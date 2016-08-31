#!/usr/bin/env python2
# -*- coding: utf-8 -*-
# WordCount with distributed Top10 design pattern.

import mrjob,os
from mrjob.job import MRJob
from mrjob.step import MRStep
import heapq
import re
from lxml import etree
import StringIO
#WORD_RE = re.compile(r"[\w]+")
pattern1 = re.compile("<page>")
pattern2 = re.compile("</page>")


WORD_RE = re.compile(r"[\w]+")


TOPN=100
class WordCountTopN(MRJob):
    # Step 1:
    # Mapper strips the input and outputs  <word, 1>
    def mapper_init(self):
        self.content='' 
        self.flag=0

    def mapper1(self, _, line):
        if '<page>' in line:
            self.flag=1
            self.content+=line
        elif '</page>' in line:
            self.flag = 0
            self.content+=line
            content=self.content
            self.content=''
	    root = etree.fromstring(content)
            text = root.find("revision").find("text")
            if text.text:
               	for word in WORD_RE.findall(text.text):
                   yield (word.lower(),1)
        elif (self.flag==1):
            self.content+=line      

    def mapper(self, _, line):
  	if ('<page>' in line) :
  		self.flag=1
  		self.content+=line
  	elif ('</page>' in line) and self.flag==1:
  		self.flag = 0
  		self.content+=line
  		content=self.content
  		self.content=''
  		root = etree.fromstring(content)
  		text = root.find("revision").find("text")
  		if text.text:
  			for word in WORD_RE.findall(text.text):
  				yield (word,1)
  	elif (self.flag==1):
  		self.content+=line
  	else:
  		pass

  
    # The Combiner sums the local word counts from each node.
    # This is not a complete count for each word, however, as
    # words may come from multiple nodes.
    def combiner(self, word, counts):
        yield word, sum(counts)

    # Multiple reducers run. However, each reducer sees all of the counts
    # for any given word. Therefore the reducer can compute a local Top-10.
    # This is maintained in the variable self.heap (an array).
    # The array is maintained with the python heapq module.
    # reducer_init() runs before the reducer runs. It sets up the heap.
    def reducer_init(self):
        self.heap = []

    # reducer() runs for each of the words. It sees the word and *all the counts*
    # for that word. Instead of emitting the count, it adds it to the heap. If the
    # heap has more than TOPN elements, the smallest element is removed.
    def reducer(self, word, counts):
        heapq.heappush(self.heap,(sum(counts),word))
        if len(self.heap) > 10*TOPN:
            heapq.heappop(self.heap)

    # reducer_final() runs at the end of all the reducers. It dumps the heap,
    # which has the local tip 10.
    def reducer_final(self):
        for (count,word) in self.heap:
            yield (word,count)

    # Step 2 — The global TopN needs to run.
    # The mapper outputs "TopN" as the key and (count,word) as the value.
    # We put the count first so that it can be used directly as input to heapq.nlargest()
    def globalTopN_mapper(self,word,count):
        yield "Top"+str(TOPN), (word,count)

    # The reducer ignores the key ("TopN"), and just finds the largest of the values.
    def globalTopN_reducer(self,_,countsAndWords):
        for countAndWord in heapq.nlargest(TOPN,countsAndWords):
            yield (countAndWord)
        
    # Steps causes mrjob to run multiple jobs.
    def steps(self):
        return [
            MRStep(mapper_init=self.mapper_init,
		   mapper=self.mapper,
                   combiner=self.combiner,
                   reducer_init=self.reducer_init,
                   reducer=self.reducer,
                   reducer_final=self.reducer_final
                   ),

            MRStep(mapper=self.globalTopN_mapper,
                   reducer=self.globalTopN_reducer) ]

if __name__=="__main__":
    WordCountTopN.run()

