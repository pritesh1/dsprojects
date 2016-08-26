from __future__ import print_function

import sys
import os
from operator import add
from pyspark import SparkContext
from lxml import etree
import datetime, dateutil.parser

def localpath(path):
    return 'file://' + str(os.path.abspath(os.path.curdir)) + '/' + path

class Post(object):
    def __init__(self, PId, AccId, atTime):
        self.PId    = PId
        self.AccId  = AccId
        self.atTime = atTime

def postFilter(line):
    if "<row" not in line:
        return False
    else:
        try:
            indata = etree.XML(line)
        except:
            return False
        
        if 'CreationDate' in indata.attrib :
            return True
        else:
            return False

def postParse(line):
    indata = etree.XML(line)
    pid  = indata.attrib['Id']
    time = dateutil.parser.parse(indata.attrib['CreationDate'])
    try:
        accId = indata.attrib['AcceptedAnswerId']
    except:
        accId = None
    return  Post(pid, accId, time)
    

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: spark-submit this_file.py posts_dir", file=sys.stderr)
        exit(-1)

    sc = SparkContext(appName="PythonQuickCount")    
    pdata = sc.textFile(localpath(sys.argv[1]+"/part-*.xml.gz")) \
              .filter(lambda x: postFilter(x)) \
              .map(postParse)

    # join AcceptedAnswerId and Id with computing the time difference
    adata = pdata.filter(lambda x: x.AccId is not None)\
                 .map(lambda x: (x.AccId, x.atTime)) \
                 .join(pdata.map(lambda p : (p.PId, p.atTime))) \
                 .map(lambda (id, (tstart, tend)): (tstart.hour, (tend-tstart).days*24*60+(tend-tstart).seconds//60))

    # post total
    ptotal = adata.map(lambda (x, y): (x, 1)).reduceByKey(add)
    # post quick
    pquick = adata.map(lambda (x, y): (x, 1) if y <= 180 else (x, 0)).reduceByKey(add)


    print("total %d, quick %d" % (sum([num for id, num in ptotal.collect()]), sum([num for id, num in pquick.collect()])))
    prate = ptotal.join(pquick) \
                  .map(lambda (id, (nt, nq)): (int(id), nq/float(nt))) \
                  .collect()

    prate = sorted(prate, key=lambda x : x[0])
    #print(prate)
    prate = [rate for hour, rate in prate]
    print(prate)
