# Overview

StackOverflow is a collaboratively edited question-and-answer site originally
focused on programming topics. Because of the variety of features tracked,
including a variety of feedback metrics, it allows for some open-ended analysis
of user behavior on the site.


### PySpark workflow
Class definitions need to be written in a separate file and then included
at runtime.

1. Edit source code in your main.py file, classes in a separate classes.py
2. Run locally using eg.
`$SPARK_HOME/bin/spark-submit --py-files src/classes.py src/main.py data/stats results/stats/`

### Scala workflow
1. Edit source code in Main.scala
2. Run the command `sbt package` from the root directory of the project
3. Use
   [spark-submit](https://spark.apache.org/docs/latest/submitting-applications.html)
   locally: this means adding a flag like `--master local[2]` to the
   spark-submit command.


Each post on StackExchange can be upvoted, downvoted, and favorited. One
"sanity check" we can do is to look at the ratio of upvotes to downvotes
(referred to as "UpMod" and "DownMod" in the schema) as a function of
how many times the post has been favorited.  Using post favorite counts
as the keys for your mapper, calculate the average percentage of upvotes
(upvotes / (upvotes + downvotes)) for the first 50 keys (starting from
the least favorited posts).

We Do the analysis on the stats.stackexchange.com dataset.


Investigate the correlation between a user's reputation and the kind of posts
they make. For the 99 users with the highest reputation, single out posts which
are either questions or answers and look at the percentage of these posts that
are answers: (answers / (answers + questions)).  Return a tuple of their user
ID and this fraction.

How long do you have to wait to get your question answered? Look at the set of
ACCEPTED answers which are posted less than three hours after question
creation. What is the average number of these "quick answers" as a function of
the hour of day the question was asked?  You should normalize by how many total
accepted answers are garnered by questions posted in a given hour, just like
we're counting how many quick accepted answers are garnered by questions posted
in a given hour, eg. (quick accepted answers when question hour is 15 / total
accepted answers when question hour is 15).

## identify_veterans_from_first_post_stats
It can be interesting to think about what factors influence a user to remain
active on the site over a long period of time.  In order not to bias the
results towards older users, we'll define a time window between 100 and 150
days after account creation. If the user has made a post in this time, we'll
consider them active and well on their way to being veterans of the site; if
not, they are inactive and were likely brief users.

