
The StatNLP Mention Extraction System v0.2

Available at: http://statnlp.org/research/ie/

Author: Wei Lu (luwei@statnlp.com)


SYSTEM REQUIREMENT: 

This software was developed under:
- J2SDK 1.7.0_67
- Mac OSX 10.10.2 (14C109)

COMPILATION:

    mkdir bin
    javac -d bin @sources.txt


1. Joint Extraction of Mentions' Boundaries and Types

a) Training:
java -cp bin com.statnlp.ie.mention.main.MentionLearner <MENTION TYPE LIST> <MODEL FILE> <TRAINING DATA>

For example:
java -cp bin com.statnlp.ie.mention.main.MentionLearner data/typelist model data/sample-train.data

You can specify the number of iterations to run by setting the numIter system property, for example:
java -DnumIter=2000 -cp bin com.statnlp.ie.mention.main.MentionLearner data/typelist model data/sample-train.data


** Optionally, a held-out dev set can be provided to optimize F measure:
java -cp bin com.statnlp.ie.mention.main.MentionLearner <MENTION TYPE LIST> <MODEL FILE> <TRAINING DATA> <DEV DATA>

For example:
java -cp bin com.statnlp.ie.mention.main.MentionLearner data/typelist model data/sample-train.data data/sample-dev.data


b) Evaluation:
java -cp bin com.statnlp.ie.mention.main.MentionExtractor <MODEL FILE> <TEST DATA>

For example:
java -cp bin com.statnlp.ie.mention.main.MentionExtractor model data/sample-test.data

Optionally, you can save the predictions into a file by supplying the system parameter outputFile, as follows:
java -DoutputFile=<OUTPUT FILE> -cp bin com.statnlp.ie.mention.main.MentionExtractor <MODEL FILE> <TEST DATA>

For example:
java -DoutputFile=sample-test.result -cp bin com.statnlp.ie.mention.main.MentionExtractor model data/sample-test.data
This will print the predictions into the file "sample-test.result"

c) Re-evaluation:
If you already have the result files, you can run:
java -cp bin com.statnlp.ie.mention.main.MentionEvaluator <GOLD FILE> <RESULT FILE>

For example:
java -cp bin com.statnlp.ie.mention.main.MentionEvaluator data/sample-test.data sample-test.result


2. Joint Extraction of Mentions' Boundaries, Types and Heads

a) Training:
java -cp bin com.statnlp.ie.mention.main.MentionHeadLearner <MENTION TYPE LIST> <MODEL FILE> <TRAINING DATA>

For example:
java -cp bin com.statnlp.ie.mention.main.MentionHeadLearner data/typelist model-head data/sample-train.data


** Optionally, a held-out dev set can be provided to optimize F measure:
java -cp bin com.statnlp.ie.mention.main.MentionHeadLearner <MENTION TYPE LIST> <MODEL FILE> <TRAINING DATA> <DEV DATA>

For example:
java -cp bin com.statnlp.ie.mention.main.MentionLearner data/typelist model-head data/sample-train.data data/sample-dev.data


b) Evaluation:
java -cp bin com.statnlp.ie.mention.main.MentionHeadExtractor <MODEL FILE> <TEST DATA>

For example:
java -cp bin com.statnlp.ie.mention.main.MentionHeadExtractor model-head data/sample-test.data


3. Format of Mention Type File:

X:X{ X[FAC|GPE|LOC|ORG|PER|VEH|WEA] }

Here, the list FAC|GPE|LOC|ORG|PER|VEH|WEA specifies all the possible semantic classes the model can consider.


4. Format of Data File:

Each sentence/instance consists of exactly four lines:

LINE 1: SENTENCE: The natural language sentence (space delimited).
LINE 2: POS TAGS: The POS tags associated with each word in the sentence (space delimited).
LINE 3: MENTION INFORMATION: One mention is represented with 5 values. For example, 24,35,24,25 PER, where 24,35 are the left (inclusive) and right (exclusive) boundaries for the mention, 24,25 are the left (inclusive) and right (exclusive) boundaries for the mention's head, and PER is the mention's semantic class (type).
LINE 4: SPACE


5. Exact Splits for ACE2004/ACE2005 dataset

Due to license issue, we are unable to release the complete ACE2004/ACE2005 dataset here, but we provide the exact train/dev/test splits that we used for all our experiments on ACE2004/ACE2005 dataset. Please refer to the folder "data/ACE2004_split" and "data/ACE2005_split".

The data folder contains only sample data (which is NOT sufficient for learning a good mention extraction and classification system).

Note that the current software also uses the features specifically designed for ACE2004 and ACE2005 datasets. L2 regularization parameter is set to 0.01; max number of L-BFGS iterations is set to 500.


