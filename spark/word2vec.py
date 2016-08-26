from __future__ import print_function

import sys
import os
import re
from lxml import etree
from operator import add
from pyspark import SparkContext
from pyspark.ml.feature import Word2Vec
from pyspark.sql import SQLContext

WORD_RE = re.compile(r"\<([\w.-]+)\>")

def localpath(path):
    return 'file://' + str(os.path.abspath(os.path.curdir)) + '/' + path

def postFilter(line):
    if "<row" not in line:
        return False
    else:
        try:
            indata = etree.XML(line)
        except:
            return False
        
        if 'Tags' in indata.attrib :
            return True
        else:
            return False

def postParse(line):
    indata = etree.XML(line)
    tags = WORD_RE.findall(indata.attrib['Tags'])
    return  tags
    

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: spark-submit this_file.py posts_dir", file=sys.stderr)
        exit(-1)

    Nout = 25
    sc = SparkContext(appName="PythonWord2VecCount")    
    sqlContext = SQLContext(sc)
    documentDF = sc.textFile(localpath(sys.argv[1]+"/part-*.xml.gz")) \
                   .filter(lambda x: postFilter(x)) \
                   .map(postParse).filter(lambda x: len(x) > 0) \
                   .map(lambda x: (x, 1)).toDF(['text','score'])
        
    # print(documentDF.show())
    # Learn a mapping from words to Vectors.
    word2Vec = Word2Vec(vectorSize=100, minCount=0, inputCol="text", outputCol="result")
    model = word2Vec.fit(documentDF)
    result = model.transform(documentDF)

    ggplot2_syn = model.findSynonyms('ggplot2',Nout)
    #print(ggplot2_syn.take(Nout))
    #print(ggplot2_syn.show())
    #print(ggplot2_syn.select("similarity").show())
    outdict = ggplot2_syn.toPandas().to_dict()
    outlist = []
    for i in xrange(Nout):
        outlist.append((str(outdict['word'][i]), outdict['similarity'][i]))

    print(outlist)

    """
    print("features")
    for feature in result.select("result").take(10):
        print(feature)

    print("ggplot2 vector")
    vectors = model.getVectors().map(lambda x: (x.word, x.vector))
    print(vectors.lookup('ggplot2'))
    """
