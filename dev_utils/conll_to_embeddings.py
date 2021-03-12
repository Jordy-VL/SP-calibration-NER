# -*- coding: utf-8 -*-
"""
Flair character-LM word embeddings - pipeline
This implementation borrows [1]


References
----------
- [1]Akbik, Alan, et al. "FLAIR: An easy-to-use framework for state-of-the-art NLP." Proceedings of the 2019 Conference of the North American Chapter of the Association for Computational Linguistics (Demonstrations). 2019.
"""
"""
To install flair in poetry:

flair = "^0.6.0"
"""

# Yield successive n-sized 
# chunks from l. 
def divide_chunks(l, n): 
      
    # looping till length l 
    for i in range(0, len(l), n):  
        yield l[i:i + n] 
  
import os
import sys
import numpy as np
import pandas as pd
import csv
csv.field_size_limit(sys.maxsize)
from flair.data import Sentence
from flair.embeddings import WordEmbeddings, FlairEmbeddings, ELMoEmbeddings, TransformerWordEmbeddings #RoBERTaEmbeddings

def infer_split(filename):
    filename = os.path.basename(filename)
    if "train" in filename:
        return "train"
    elif "dev" in filename or "val" in filename:
        return "dev"
    elif "test" in filename:
        return "test"

def flair_embeddings(sentences, output_file=None):
    if output_file:
        f = open(output_file, 'w')

    embedder = FlairEmbeddings("multi-forward") #multilingual; you also have nl-forward; no french model though
    
    document_embedding = []
    for i, sent in enumerate(sentences):
        print("Encoding the {}th input sentence!".format(i))
        # create a sentence
        sentence = Sentence(" ".join(sent))

        # embed words in sentence
        embedder.embed(sentence)
        sentence_embedding = np.mean([token.embedding.cpu().numpy() for token in sentence],axis=0) #have to go from CUDA tensor to cpu tensor
        document_embedding.append(sentence_embedding)

        if output_file:
            for token in sentence:
                f.write(token.text + "\t" + "\t".join([str(num) for num in token.embedding.tolist()]) + '\n')
    document_embedding = np.mean(document_embedding, axis=0)
    return document_embedding

"""
Simple embedding functionality
"""
"""
text = "Ik vind dit echt wel een leuke manier om tekst te encoderen."
embedded = flair_embeddings([text.split()], output_file=None)

print(embedded.shape)
print(embedded)
"""

def main(directory, embeddings, strategy):
    # 1. find corpora in data directory
    corpora = {"train": None, "dev": None, "test": None}
    for labelset in corpora:
        for file in sorted(os.listdir(directory)):
            if infer_split(file) == labelset:
                corpora[labelset] = pd.read_csv(os.path.join(directory, file), sep="\t", names=["text","pos","lemma","label"], engine="python", error_bad_lines=False, quoting=csv.QUOTE_NONE).fillna("")
                break

    if embeddings == "elmo":
        embedder = ELMoEmbeddings("original")
    elif embeddings == "flair":
        embedder = FlairEmbeddings("news-forward")  
    elif embeddings == "bert":
        embedder = TransformerWordEmbeddings('bert-base-cased')

    embeddings_dir = os.path.join(directory, embeddings+ "_embeddings")
    if not os.path.exists(embeddings_dir):
        os.makedirs(embeddings_dir, exist_ok=True)

    strategy = np.mean if strategy == "mean" else np.max if strategy == "max" else np.sum if strategy == "sum" else None

    for labelset, corpus in corpora.items():
        if corpus is None:
            print(f"empty corpus: {labelset}")
            continue
        voc = sorted(corpus["text"].unique())
        print(f"Unique tokens: {len(voc)}")

        with open(os.path.join(embeddings_dir, labelset+".w2v"), "w") as f:
            for word in voc:
                sentence = Sentence(word)
                if len(sentence) == 0:
                    continue
                embedder.embed(sentence)
                token_embedding = strategy([token.embedding.cpu().numpy() for token in sentence],axis=0) 
                f.write(word + " " + " ".join([str(num) for num in token_embedding.tolist()]) + '\n')


if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser("""Convert ConLL format data to token-level embeddings""")
    parser.add_argument("directory", default="/home/jordy/code/SP-calibration-NER/acl2019_nested_ner/data/ace2004", type=str)
    parser.add_argument(
        "-e",
        dest="embeddings",
        type=str,
        choices=["elmo", "flair", "bert"],
    )
    parser.add_argument(
        "-s",
        dest="strategy",
        type=str,
        default="mean",
        choices=["mean", "max", "sum"],
    )
    args = parser.parse_args()

    main(args.directory, args.embeddings, args.strategy)
