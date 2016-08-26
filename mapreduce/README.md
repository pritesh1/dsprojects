# Overview
We are going to be running mapreduce jobs on the wikipedia dataset.  The
dataset is available (pre-chunked)


## top100_words_simple_plain
 The first job is similar to standard wordcount but with a few tweaks.
   The data provided for wikipedia is in in \*.xml.bz2 format.  Mrjob will
   automatically decompress bz2.  We'll deal with the xml in the next question.
   For now, just treat it as text.  A few hints:
   - To split the words, use the regular expression "\w+".
   - Words are not case sensitive: i.e. "The" and "the" reference to the same
     word.  You can use `string.lower()` to get a single case-insenstive
     canonical version of the data.


## wikipedia_entropies of words
The [Shannon
entropy](https://en.wikipedia.org/wiki/Entropy_(information_theory) of a
discrete random variable with probability mass function p(x) is:

    $$ H(X) = - \sum p(x) \log_2 p(x) $$

You can think of the Shannon entropy as the number of bits needed to store
things if we had perfect compression.  It is also closely tied to the notion of
entropy from physics.

The Task is Estimating the Shannon entropy of different Simple English and Thai
based off of their Wikipedias. Do this with n-grams of characters, by first
calculating the entropy of a single n-gram and then dividing by n to get the
per-character entropy. Use n-grams of size 1, 2, 3.  How should our
per-character entropy estimates vary with n?  How should they vary by
the size of the corpus? How much data would we need to get reasonable entropy
estimates for each n?

Why Do I use Map Reduce

There are >300 million characters
in this dataset. How much memory would it take to store all `n`-grams as `n`
increases?


## double_link_stats_simple
Instead of analyzing single links, let's look at double links.  That is, pages
A and C that are connected through many pages B where there is a link
`A -> B -> C` or `C -> B -> A`. Find the list of all pairs `(A, C)` (you can
use alphabetical ordering to break symmetry) that have the 100 "most"
connections (see below for the definition of "most").  This should give us a
notion that the articles `A` and `C` refer to tightly related concepts.

1. This is essentially a Matrix Multiplication problem.  If the adjacency
   matrix is denoted $M$ (where $M_{ij}$ represents the link between $i$ an
   $j$), we are looking for the highest 100 elements of the matrix $M M$.

2. Notice that a lot of Category pages (denoted "Category:.*") have a high link
   count and will rank very highly according to this metric.  Wikipedia also
   has `Talk:` pages, `Help:` pages, and static resource `Files:`.  All such
   "non-content" pages (and there might be more than just this) and links to
   them should be first filtered out in this analysis.

3. Some pages have more links than others.  If we just counted the number of
   double links between pages, we will end up seeing a list of articles with
   many links, rather than concepts that are tightly connected.

   1. One strategy is to weight each link as $\frac{1}{n}$ where $n$ is the
      number links on the page.  This way, an article has to spread it's
      "influence" over all $n$ of its links.  However, this can throw off the
      results if $n$ is small.

   2. Instead, try weighting each link as $\frac{1}{n+10}$ where 10 sets the
      "scale" in terms of number of links above which a page becomes
      "significant".  The number 10 was somewhat arbitrarily chosen but seems
      to give reasonably relevant results.

   3. This means that our "count" for a pair A,C will be the products of the
      two link weights between them, summed up over all their shared
      connections.

4. Again, if there are multiple links from a page to another, have it only
   count for 1.  This keeps our results from becoming skewed by a single page
   that references the same page multiple times.

