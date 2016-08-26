from mrjob.job import MRJob
from mrjob.step import MRStep
from lxml import etree
import mwparserfromhell
import re


def to_unicode(obj, encoding='utf-8'):
    if isinstance(obj, basestring):
        if not isinstance(obj, unicode):
            obj = unicode(obj, encoding, errors='replace')
    return obj

def remove(text):

    return ''.join(i for i in text if ord(i)<128)

class LinkCounter(MRJob):

    def mapper_init(self):
        self.page_contents = ''
        self.in_page = False

    def mapper1(self, _, line):
        line = line.strip()
        if line == '<page>':
            self.increment_counter('group', 'pages')
            self.in_page = True
        if self.in_page:
            self.page_contents += line + ' '
            if line == '</page>':
                yield self.page_contents, None
                self.page_contents = ''
                self.in_page = False


    def mapper(self, _, line):
        line = line.strip()
        if line == '<page>':
            self.increment_counter('group', 'pages')
            self.in_page = True
        if self.in_page:
            self.page_contents += line + ' '
            if line == '</page>':
                A1=etree.fromstring(self.page_contents)
                Links=  mwparserfromhell.parse(A1.find('revision').find('text').text).filter_wikilinks() 
                Title= (A1.find('title').text)
                Links1=[]
                Title1=[]
                for items in Links:
                    if re.findall(r'\w+:',items.encode('utf8')) or re.findall(r'\w+:',Title):
                        pass
                    else:
                        items=items.replace("[[","")
                        items=items.replace("]]","")
                        items=items.encode('utf8')
                        items=items.decode('unicode_escape').encode('ascii','ignore')
                        Links1.append(items.lower())
                if Links1:
                    Title1.append(Title.lower())
                if Title1 and Links1:
                    self.increment_counter('group1', 'pages')        
                    yield ((Title1,Links1),float(1)/(len(Links1)+10))

                self.page_contents = ''
                self.in_page = False            

    def mapper_final1(self, page_contents, _):
        amount = len(set([unicode(link) for link in \
                mwparserfromhell.parse(etree.fromstring(page_contents).find('revision').find('text').text).filter_wikilinks()]))
        self.increment_counter('group', 'sum_num_links', amount)
        #self.increment_counter('group', 'sum_num_links_squared', amount**2)
        #yield None,(amount,1)
        print page_contents

    def mapper_final(self, page_contents, _):
        A1=etree.fromstring(page_contents)
        Links=  mwparserfromhell.parse(A1.find('revision').find('text').text).filter_wikilinks() 
        Title= (A1.find('title').text)
        Links1=[]
        for items in Links:
            if re.findall(r'\w+:',items.encode('utf8')):
                pass
            else:
                items=items.replace("[[","")
                items=items.replace("]]","")
                items=items.encode('utf8')
                items=items.decode('unicode_escape').encode('ascii','ignore')
                Links1.append(items.lower())    
        yield (Title.lower(),Links1),float(1)/(len(Links1)+10)

    


        
    def reducer_matrix_init(self):
        self.link={}
        self.output_num = 100

    def reducer(self,key,value):
        yield None,(key,max(value))

    def reducer_matrix1(self, _, links):
        for keys, weight in links:
            key1,key2=keys 
            if key1 and key2:
                for key_2 in key2:
                    if key1[0] in self.link:
                        if key_2 in self.link[key1[0]]:
                            self.link[key1[0]][key_2] += weight
                        else:
                            self.link[key1[0]][key_2] = weight
                    else:
                        self.link[key1[0]] = {}
                        self.link[key1[0]][key_2] = weight

        #for k, v in self.link.iteritems():
            #print k, v

        # Matrix multiplication
        out_dict = {}
        for key1  in self.link:
            for key2 in self.link[key1]:
                weight1 = self.link[key1][key2]                
                if key2 in self.link:
                    for key3 in self.link[key2]:
                        weight2 = self.link[key2][key3]
                        if key1 < key3:
                            A = key1
                            C = key3
                        else:
                            A = key3
                            C = key1
                        if (A, C) in out_dict:
                            out_dict[(A, C)] += weight1*weight2
                        else:
                            out_dict[(A, C)] = weight1*weight2

        keys = sorted(out_dict, key=out_dict.get, reverse=True)        
        for key in keys[0:self.output_num]:
            A, C = key
            print (remove(A.encode('utf8')), remove(C.encode('utf8')), out_dict[key])
 




    def reducer_matrix(self, _, links):
        for keys, weight in links:
            (key1, key2) = keys
            if key1 and key2:
                if key1 in self.link:
                    if key2 in self.link[key1]:
                        self.link[key1][key2] += weight
                    else:
                        self.link[key1][key2] = weight
                else:
                    self.link[key1] = {}
                    self.link[key1][key2] = weight

        # Matrix multiplication
        out_dict = {}
        for key1  in self.link:
            for key2 in self.link[key1]:
                weight1 = self.link[key1][key2]                
                if key2 in self.link:
                    for key3 in self.link[key2]:
                        weight2 = self.link[key2][key3]
                        if key1 < key3:
                            A = key1
                            C = key3
                        else:
                            A = key3
                            C = key1
                        if (A, C) in out_dict:
                            out_dict[(A, C)] += weight1*weight2
                        else:
                            out_dict[(A, C)] = weight1*weight2

        keys = sorted(out_dict, key=out_dict.get, reverse=True)        
        for key in keys[0:self.output_num]:
            A, C = key
            print ((str(unicode(A)), str(unicode(C))), out_dict[key])




    def steps(self):
        return [
            MRStep(mapper_init=self.mapper_init,
                   mapper=self.mapper,
                   #reducer_init=self.reducer_matrix_init,
                   reducer=self.reducer
                 ),
             MRStep(reducer_init=self.reducer_matrix_init,
                   reducer=self.reducer_matrix1)

            ]



if __name__ == '__main__':
    LinkCounter.run()