from __future__ import print_function

import sys
import os
from operator import add
from pyspark import SparkContext
from lxml import etree

def localpath(path):
    return 'file://' + str(os.path.abspath(os.path.curdir)) + '/' + path

class Vote(object):
    def __init__(self, Id, Type):
        self.Id = Id
        self.Type = Type

def voteFilter(line):
    if "<row" not in line:
        return False
    else:
        try:
            indata = etree.XML(line)
        except:
            return False
        
        if set(('PostId','VoteTypeId')) <=   set(indata.attrib):
            return True
        else:
            return False

def voteParse(line):
    indata = etree.XML(line)
    pid  = indata.attrib['PostId']
    vtype = int(indata.attrib['VoteTypeId'])
    return  Vote(pid, vtype)
    


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: spark-submit this_file.py votes_dir", file=sys.stderr)
        exit(-1)

    sc = SparkContext(appName="PythonVoteCount")
    # parse votes data
    vdata = sc.textFile(localpath(sys.argv[1])+"/part-0000*xml.gz") \
              .filter(lambda x: voteFilter(x)) \
              .map(voteParse)

    vup    =  vdata.filter(lambda p: p.Type == 2 or p.Type == 3) \
                   .map(lambda p: (p.Id, 1) if p.Type == 2 else (p.Id, 0)) \
                   .reduceByKey(add)

    vdown  =  vdata.filter(lambda p: p.Type == 2 or p.Type == 3) \
                   .map(lambda p: (p.Id, 1) if p.Type == 3 else (p.Id, 0)) \
                   .reduceByKey(add)

    vfavor =  vdata.filter(lambda p: p.Type == 5) \
                   .map(lambda p: (p.Id, 1)) \
                   .reduceByKey(add)

    # sanity check - Count

    totalFavoriteCount = sum([count for key, count in vfavor.collect()])
    totalUp            = sum([count for key, count in vup.collect()])
    totalDown          = sum([count for key, count in vdown.collect()])
    print("Favorite:%d Up:%d, Down: %d" % (totalFavoriteCount, totalUp, totalDown))

    # compute UpVotes percentage
    uprate = vup.join(vdown) \
                .map(lambda (id, (up, down)) : (id, up/float(up+down))
                     if (up+down) != 0 else (id, 0.0))

    FavCount = vfavor.join(uprate) \
                     .map(lambda (id, (Fcount, score)): (Fcount, score)) \
                     .combineByKey(lambda value: (value, 1),
                                   lambda x, value: (x[0] + value, x[1] + 1),
                                   lambda x, y: (x[0] + y[0], x[1] + y[1])) \
                     .map(lambda (label, (value_sum, count)): (label, value_sum / float(count))) \
                     .collect()
    
    #print(sorted(FavCount, key=lambda x: x[0])[len(FavCount)-51:len(FavCount)-1])
    print(sorted(FavCount, key=lambda x: x[0])[0:50])
