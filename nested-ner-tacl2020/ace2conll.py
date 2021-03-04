import os 
import sys 
from tqdm import tqdm
import regex as re


filepath = sys.argv[1]

with open(filepath, "r") as f:
    text = f.read()

documents = text.split("( End )")
print(f"document: {len(documents)}")

fallback_value = (-1, -1, "dummy-label")

def process_labels(labels):
    #0,5 PER|5,6 PER|20,22 PER|22,43 PER|23,24 ORG|26,27 ORG|
    #26,28 ORG|30,31 PER|33,35 WEA|37,40 VEH|41,42 PER|41,43 VEH
    if not labels:    return [fallback_value]
    tupled = []
    for nested in labels.split("|"):
        matched = re.split("[ ,]", nested)
        start,end,label = matched[0], matched[1], matched[2]
        tupled.append((int(start),int(end),str(label)))
    return tupled

def label_tokenized(tupled, tokenized):
    if tupled[0] == fallback_value:
        return ["O"] * len(tokenized)
    collector = []
    for i in range(len(tokenized)):
        current = []
        for s,e, l in tupled:
            if i == s:
                current.append("B-"+l)
            elif i in range(s,e): #+1 for e?
                current.append("I-"+l)
            # elif i == e:
            #     current.append("E-")
        if not current:
            current = ["O"]
        collector.append("|".join(sorted(set(current)))) #to avoid: 'B-PER|B-PER'
    return collector


"""
The input format is a CoNLL format, with one token per line, sentences
delimited by empty line. For each token, columns are separated by tabs. First
column is the surface token, second column is lemma, third column is a POS tag
and fourth column is the BILOU encoded NE label.
"""

sent_collector = []
for i, document in tqdm(enumerate(documents)):
    lines = [line for line in document.split("\n") if line]
    for j in tqdm(range(len(lines))):
        if j % 2 == 1:
            if not "|" in lines[j] and not re.match("\d+,\d+ [A-Z]+", lines[j]):
                pass #probably missing annotations
            else:
                continue
        sentence = lines[j]
        tokenized = sentence.split(" ")

        if j+1 >= len(lines): #should safely skip
            continue

        labels = lines[j+1]
        
        if not "|" in labels and not re.match("\d+,\d+ [A-Z]+", labels):
            labels = ""
        
        tupled = process_labels(labels)
        aligned = label_tokenized(tupled, tokenized)

        try:
            assert len(aligned) == len(tokenized)
        except AssertionError as e:
            import pdb; pdb.set_trace()  # breakpoint b8bac7ae //


        token_collector = []
        for t, w in enumerate(tokenized):
            token_collector.append("\t".join((w, "_", "_", aligned[t])))
        token_collector.append(" ")

        sent_collector.append("\n".join(token_collector))

print(f"sentences: {len(sent_collector)}")

with open(os.path.splitext(filepath)[0]+".conll", "w") as f:
    f.writelines(sent_collector)