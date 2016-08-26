from __future__ import print_function

import sys
import os
from operator import add
from pyspark import SparkContext
from lxml import etree

def localpath(path):
    return 'file://' + str(os.path.abspath(os.path.curdir)) + '/' + path

def isBody(line):
    return "<row" in line

class Post(object):
    def __init__(self, Id, PostTypeId):
        self.Id = Id
        self.PostTypeId = PostTypeId

def postFilter(line):
    if "<row" not in line:
        return False
    else:
        try:
            indata = etree.XML(line)
        except:
            return False
        
        if set(('OwnerUserId','PostTypeId')) <=   set(indata.attrib):
            return True
        else:
            return False

def postParse(line):
    indata = etree.XML(line)
    uid  = indata.attrib['OwnerUserId']
    pid = int(indata.attrib['PostTypeId'])
    return  Post(uid, pid)
    

class User(object):
    def __init__(self, Id, Reputation):
        self.Id = Id
        self.Reputation = Reputation

def userFilter(line):
    if "<row" not in line:
        return False
    else:
        try:
            indata = etree.XML(line)
        except:
            return False
        
        if set(('Id','Reputation')) <=   set(indata.attrib):
            return True
        else:
            return False

def userParse(line):
    indata = etree.XML(line)
    uid  = indata.attrib['Id']
    rep = int(indata.attrib['Reputation'])
    return  User(uid, rep)
    

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: spark-submit this_file.py posts_dir users_dir", file=sys.stderr)
        exit(-1)

    sc = SparkContext(appName="PythonRepCount")    
    # read posts data
    pdata = sc.textFile(localpath(sys.argv[1]+"/part-*.xml.gz")) \
              .filter(lambda x: postFilter(x)) \
              .map(postParse)

    # read users data
    udata = sc.textFile(localpath(sys.argv[2]+"/part-*.xml.gz")) \
              .filter(lambda x: userFilter(x)) \
              .map(userParse)

    # count question and answer
    pque = pdata.filter(lambda p: p.PostTypeId == 1 or p.PostTypeId == 2) \
                .map(lambda p: (p.Id, 1) if p.PostTypeId == 1 else (p.Id, 0)) \
                .reduceByKey(add) 

    pans = pdata.filter(lambda p: p.PostTypeId == 1 or p.PostTypeId == 2) \
                .map(lambda p: (p.Id, 1) if p.PostTypeId == 2 else (p.Id, 0)) \
                .reduceByKey(add) 

    tque = sum([num for id, num in pque.collect()])
    tans = sum([num for id, num in pans.collect()])
    print("total ans=%d, total ques=%d" % (tans,tque))  
    
    prate = pans.join(pque) \
                .map(lambda (uid, (ans, que)) : (uid, ans/float(ans+que)) if (ans+que) != 0 else (uid, 0))
    
    urep = udata.map(lambda u: (u.Id, u.Reputation)) \
                .join(prate) \
                .map(lambda (uid , (rep, rate)): (rep, uid, rate)) \
                .collect()

    urep = sorted(urep, key=lambda x: x[0], reverse=True)

    outdata = []
    for val in urep:
        rep, id, rate = val
        outdata.append((int(id), rate))
    outdata = outdata[0:99]
    outdata.append((-1, tans/float(tans+tque)))
    print(outdata)
