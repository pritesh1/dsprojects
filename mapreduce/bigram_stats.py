#!/usr/bin/env python2
# -*- coding: utf-8 -*-
# WordCount with distributed Top10 design pattern.

import mrjob,os
from mrjob.job import MRJob
from mrjob.step import MRStep
import heapq
import re
import re
from lxml import etree
import StringIO
from StringIO import StringIO
from lxml import etree
WORD_RE = re.compile(r"[\w]+")
pattern1 = re.compile("<page>")
pattern2 = re.compile("</page>")
import mwparserfromhell
import numpy as np
#WORD_RE = re.compile(r"[\w]+")


TOPN=100
class WordCountTopN(MRJob):
    # Step 1:
    # Mapper strips the input and outputs  <word, 1>
    def mapper_init(self):
        self.page_contents = ''
        self.in_page = False
        self.cache1={}
        self.cache2={} 
        self.cache3={} 


    def mapper(self, _, line):
        line = line.strip()
        if line == '<page>':
            self.increment_counter('group', 'pages')
            self.in_page = True
        if self.in_page:
            self.page_contents += line + ' '
            if line == '</page>':
                #yield self.page_contents, None
                root = etree.fromstring(self.page_contents)
                text = root.find("revision").find("text")
                wikicode = mwparserfromhell.parse(text.text)
                text1 = " ".join(" ".join(fragment.value.split())for fragment in wikicode.filter_text())
                for word in text1.split():
                        for char in word:
                            if char in self.cache1:
                                self.cache1[char]+=1
                            else:
                                self.cache1[char]=1    
                    

                    
                        for i in range(len(word)-1):
                            out1=word[i]+word[i+1]
                            if out1 in self.cache2:
                                self.cache2[out1]+=1
                            else:
                                self.cache2[out1]=1

                    
                        for i in range(len(word)-2):
                            out2=word[i]+word[i+1]+word[i+2]
                            if out2 in self.cache3:
                                self.cache3[out2]+=1
                            else:
                                self.cache3[out2]=1

                self.page_contents = ''
                self.in_page = False     

        for k, v in self.cache1.iteritems():
            yield k, v 

        for k, v in self.cache2.iteritems():
            yield k, v  
            
        for k, v in self.cache3.iteritems():
            yield k, v 



    def mapper111(self, _, line):
        if ('<page>' in line) and (self.flag==0):
            self.flag=1
            self.content+=line
        elif '</page>' in line and self.flag==1:
            self.flag = 0
            self.content+=line
            content=self.content
            self.content=''
            #yield (content[2:],1)
            if (content[2] == '<'):
                root = etree.fromstring(content[2:])
                text = root.find("revision").find("text")
                if text.text:
                    wikicode = mwparserfromhell.parse(text.text)
                    text1 = " ".join(" ".join(fragment.value.split())for fragment in wikicode.filter_text())
                    #yield (text,1)
                    for word in text1.split():
                        for char in word:
                            if char in self.cache1:
                                self.cache1[char]+=1
                            else:
                                self.cache1[char]=1    
                    

                    
                        for i in range(len(word)-1):
                            out1=word[i]+word[i+1]
                            if out1 in self.cache2:
                                self.cache2[out1]+=1
                            else:
                                self.cache2[out1]=1

                    
                        for i in range(len(word)-2):
                            out2=word[i]+word[i+1]+word[i+2]
                            if out2 in self.cache3:
                                self.cache3[out2]+=1
                            else:
                                self.cache3[out2]=1


                    #for word in text.split():
                        #for i in range(len(word)-2):
                            #out=word[i]+word[i+1]+word[i+2]
                            #yield (out,1)        
                            

                    #yield (element.text,1)
                    #for word in WORD_RE.findall(element.text):
                        #yield (word.lower(), 1)          

        elif (self.flag==1):
            self.content+=line


        for k, v in self.cache1.iteritems():
            yield k, v 

        for k, v in self.cache2.iteritems():
            yield k, v  
            
        for k, v in self.cache3.iteritems():
            yield k, v         


    def init_string(self):
        self.content = "<root>"
        self.cache1={}
        self.cache2={}
        self.cache3={}

    def mapper_get_chunk(self, _, line):
        self.content += line

    def mapper_text(self):
        self.content += "</root>"
        for event, element in etree.iterparse(StringIO(self.content), recover=True):
            if element.tag == 'text':
                    wikicode = mwparserfromhell.parse(element.text)
                    text = " ".join(" ".join(fragment.value.split())for fragment in wikicode.filter_text())
                    #yield (text,1)
                    for word in text.split():
                        for char in word:
                            if char in self.cache1:
                                self.cache1[char]+=1
                            else:
                                self.cache1[char]=1    
                    

                    for word in text.split():
                        for i in range(len(word)-1):
                            out1=word[i]+word[i+1]
                            if out1 in self.cache2:
                                self.cache2[out1]+=1
                            else:
                                self.cache2[out1]=1

                    for word in text.split():
                        for i in range(len(word)-2):
                            out2=word[i]+word[i+1]+word[i+2]
                            if out2 in self.cache3:
                                self.cache3[out2]+=1
                            else:
                                self.cache3[out2]=1


                    #for word in text.split():
                        #for i in range(len(word)-2):
                            #out=word[i]+word[i+1]+word[i+2]
                            #yield (out,1)        
                            

                    #yield (element.text,1)
                    #for word in WORD_RE.findall(element.text):
                        #yield (word.lower(), 1) 


        for k, v in self.cache1.iteritems():
            yield k, v 

        for k, v in self.cache2.iteritems():
            yield k, v  
            
        for k, v in self.cache3.iteritems():
            yield k, v                                
        
    # The Combiner sums the local word counts from each node.
    # This is not a complete count for each word, however, as
    # words may come from multiple nodes.
    def combiner(self, word, counts):
        yield word, sum(counts)

    def reducer_1(self,word,counts):
        yield (None,((sum(counts),word)))

    # Multiple reducers run. However, each reducer sees all of the counts
    # for any given word. Therefore the reducer can compute a local Top-10.
    # This is maintained in the variable self.heap (an array).
    # The array is maintained with the python heapq module.
    # reducer_init() runs before the reducer runs. It sets up the heap.
    def reducer(self,_,pairs):
        import numpy as np
        sum1=np.zeros(3,dtype=np.float64)
        ent1=np.zeros(3,dtype=np.float64)
        N=np.zeros(3,dtype=np.float64)
        for count,name in pairs:
            if len(name)>0 and len(name)<4:
                sum1[len(name)-1]+=count*np.log2(count)
                N[len(name)-1]+=count    
        for i in range(0,len(sum1)):
            ent1[i]=np.log2(float(N[i]))-(float(1/float(N[i]))*sum1[i])
        print ent1

    def reducer_entropy(self, _,token_count_pairs):
        Ntot= np.zeros(3,dtype=np.float64)              
        sumn=np.zeros(3,dtype=np.float64)

        for count,name in token_count_pairs:
            if count>0:
                ngram = len(name)
                if ngram>0 and ngram <=3:
                    Ntot[ngram-1] +=count
                    sumn[ngram-1] +=count*np.log2(count)
        entropies=[]
        for index in range (0,3):
            entropies.append(np.log2(Ntot[index])-sumn[index]/np.float(Ntot[index]))
        print entropies                

        
    # Steps causes mrjob to run multiple jobs.
    def steps(self):
        return [
            MRStep(mapper_init=self.mapper_init,
                   mapper=self.mapper,
                   #mapper_final=self.mapper_text,
                   reducer=self.reducer_1),
            MRStep(reducer=self.reducer_entropy)
                 ]

'''
        return [
            MRStep(mapper_init=self.init_string,
                   mapper=self.mapper_get_chunk,
                   mapper_final=self.mapper_text,
                   reducer=self.reducer_1),
            MRStep(reducer=self.reducer_entropy)
                 ]
'''


if __name__=="__main__":
    WordCountTopN.run()