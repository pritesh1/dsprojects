from mrjob.job import MRJob
from mrjob.step import MRStep
from lxml import etree
import mwparserfromhell

class LinkCounter(MRJob):

    def mapper_init(self):
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
                yield self.page_contents, None
                self.page_contents = ''
                self.in_page = False

    def mapper_final(self, page_contents, _):
        amount = len(set([unicode(link) for link in \
                mwparserfromhell.parse(etree.fromstring(page_contents).find('revision').find('text').text).filter_wikilinks()]))
        self.increment_counter('group', 'sum_num_links', amount)
        #self.increment_counter('group', 'sum_num_links_squared', amount**2)
        yield None,(amount,1)

    def reducer(self,_,amount):
        sum1=0
        count=0
        import numpy as np
        A=np.array([])
        for key,value in amount:
            A=np.append(key,A)
            

        print np.sum(A),np.mean(A),np.std(A),np.percentile(A,5),np.percentile(A,25),np.median(A),np.percentile(A,75),np.percentile(A,95)  
                



    def steps(self):
        return [
            MRStep(mapper_init=self.mapper_init,
                   mapper=self.mapper,
                   #mapper_final=self.mapper_text,
                   reducer=self.mapper_final),
            MRStep(reducer=self.reducer)
                 ]


if __name__ == '__main__':
    LinkCounter.run()
