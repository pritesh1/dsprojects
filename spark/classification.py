from __future__ import print_function

import sys
import os
import re
import numpy as np
from lxml import etree
from operator import add
from pyspark import SparkContext
from pyspark.ml import Pipeline
from pyspark.ml.classification import LogisticRegression
from pyspark.ml.feature import HashingTF, Tokenizer
from pyspark.sql import SQLContext
import dill

WORD_RE = re.compile(r"\<([\w.-]+)\>")
#re.sub(r'<p>|</p>', "", x.body.strip())

def to_unicode(obj, encoding='utf-8'):
    if isinstance(obj, basestring):
        if not isinstance(obj, unicode):
            obj = unicode(obj, encoding, errors='replace')
    return obj

def localpath(path):
    return 'file://' + str(os.path.abspath(os.path.curdir)) + '/' + path

class Post(object):
    def __init__(self, Id, PostTypeId, Body, Tags):
        self.Id   = Id
        self.PostTypeId = PostTypeId
        self.Body = Body
        self.Tags = Tags

def postFilter(line):
    if "<row" not in line:
        return False
    else:
        try:
            indata = etree.XML(line)
        except:
            return False
        
        if set(('Id', 'PostTypeId', 'Body')) <=  set(indata.attrib) :
            return True
        else:
            return False
        

def postParse(line):
    indata = etree.XML(line)
    pid = int(indata.attrib['Id'])
    tid = int(indata.attrib['PostTypeId'])
    body = to_unicode(indata.attrib['Body'])
    try:
        tags = indata.attrib['Tags']
    except:
        tags = ""
    return  Post(pid, tid, body, tags)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: spark-submit this_file.py posts_dir", file=sys.stderr)
        exit(-1)

    Ntag = 100
    Nbody = 5121
    sc = SparkContext(appName="PythonClassification")    
    sqlContext = SQLContext(sc)
    ## Check the collact link
    inData = sc.textFile(localpath(sys.argv[1]+"/part-*.xml.gz"))\
               .filter(lambda x: postFilter(x)) \
               .map(postParse) \
               .filter(lambda x: x.PostTypeId == 1) \
               .map(lambda x: (x.Id, (x.Body, x.Tags))) \
               .sortByKey(False)

    # First selecting top Ntag tags
    tagData = inData.flatMap(lambda (x, (body, tags)) : WORD_RE.findall(tags)) \
                    .filter(lambda x: x is not "") \
                    .map(lambda x: (x, 1)) \
                    .reduceByKey(add) \
                    .collect()
    tagData = sorted(tagData, key=lambda x: x[1], reverse=True)
    topTags = [tag for tag, count in tagData][0:Ntag]
    print(topTags)
    # Make features
    inData = inData.map(lambda (x, (body, tags)): (re.sub(r'<p>|</p>', "", body.strip()) , WORD_RE.findall(tags))).collect() 

    #print("Data Length:", len(inData))
    #print(inData)
    # making pipeline
    tokenizer = Tokenizer(inputCol="text", outputCol="words")
    hashingTF = HashingTF(inputCol=tokenizer.getOutputCol(), outputCol="features")
    logreg = LogisticRegression(maxIter=10, regParam=0.01)
    pipeline = Pipeline(stages=[tokenizer, hashingTF, logreg])

    ans=[]
    for topTag in topTags:
        inList =[(body, 0.0) if topTag in tags else (body, 1.0) for body, tags in inData]
        X = sqlContext.createDataFrame(inList, ["text", "label"])
        X_train, X_test = X.randomSplit([0.9, 0.1], 42)

        #if topTag == topTags[0]: print(X_test.toPandas().to_dict())
        model = pipeline.fit(X_train)             # training the model
        prediction = model.transform(X_test)      # predicting the model

        scores = list(prediction.select("prediction").toPandas().to_dict()['prediction'].values())
        ans.append((topTag, scores[0:5121]))
        
    print(ans)
    dill.dump(ans, open("my_classification_list.dill", "wb"))
