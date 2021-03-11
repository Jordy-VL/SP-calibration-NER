import numpy as np
from collections import OrderedDict
from scipy.special import softmax

class StructuredLogits(object):

    """Object to store required information for calibrating a scoring function
    
    Attributes:
        f_x (ndarray(dtype=float, ndim=>2)): unnormalized logits
        y_true (ndarray(dtype=str, ndim=1)): true labels
        probas (ndarray(dtype=float, ndim=>2)): normalized probabilities, summing each row to 1 
        y_hat (ndarray(dtype=str, ndim=1)): predicted labels
        y_hat_idx (ndarray(dtype=int, ndim=1)): applies if not nested
        c (ndarray(dtype=bool/int, ndim=1)): True/False at the marginal level
        tokenized (ndarray(dtype=str, ndim=1)): contains a flattened sequence of words
        document_masks (ndarray(dtype=int, ndim=2)): contains an array with the index sequence for each document
    """

    def __init__(self, f_x, y_true, y_hat=None, probas=None, c=None, tokenized=None, document_masks=None, idx2label=None): 

        self.f_x = f_x #actual logits
        self.support = f_x.shape[0] #N
        self.output_space = f_x.shape[1:] #|Y| 

        self.y_true = y_true #labels format!

        if probas is None:
            self.probas = softmax(f_x)

        if y_hat is not None:
            #predicted values -> could obtain from f_x depending on task & label2idx
            self.y_hat = y_hat
        else:
            if idx2label:
                self.logits_to_pred_idx(idx2label) 

        if c is not None:
            self.c = c
        else:
            if idx2label:
                self.correctness()

        if tokenized is not None:
            self.tokenized = tokenized

        if document_masks is not None:
            self.document_masks = document_masks

    def pred_to_idx(self, label2idx, y_hat=None):
        y_hat = self.y_hat if y_hat is None else y_hat 
        self.y_hat_idx = np.vectorize(label2idx.get)(y_hat)
        self.y_true_idx = np.vectorize(label2idx.get)(self.y_true)


    def idx_to_pred(self, idx2label, y_hat_idx):
        y_hat_idx = self.y_hat_idx if y_hat_idx is None else y_hat_idx
        self.y_hat = np.vectorize(idx2label.get)(y_hat_idx)

    def logits_to_pred_idx(self, idx2label=None):
        self.y_hat_idx = np.argmax(self.f_x,-1) #assumes viterbi decoding
        if idx2label:
            self.idx_to_pred(idx2label, self.y_hat_idx)

    def correctness(self):
        self.c = np.array([int(y_hat_i == y_i) for y_i, y_hat_i in zip(self.y_true, self.y_hat)])
         #some notion of correction at the marginal level 

    def reshape_to_document_level(self):
        raise NotImplementedError

    def to_dict(self):
        return self.__dict__



class StructuredLogitsStore(object):
    """Main class responsible for keeping predictions in logits form for a model
    To be used for calibration

    #text (tokenized), idx2label/decoding function, logits (predicted), gold; generic evaluation
    #some way to obtain a TouristLeMC object
    """

    def __init__(self, modelref, idx2label=None, label2idx=None, hierarchical=False, nested=False, *kwargs):
        self.modelref = modelref
        if label2idx is None:
            self.idx2label = idx2label
            self.label2idx = {v:k for k,v in idx2label.items()}
        else:
            self.label2idx = label2idx
            self.idx2label = {v:k for k,v in label2idx.items()}

        self.dev = None
        self.test = None
        self.hierarchical = hierarchical
        self.nested = nested

    def create_labelset(self, structlogits, labelset):
        if labelset == "dev":
            self.dev = structlogits
        if labelset == "test":
            self.test = structlogits

    def complete_labelsets(self):
        # define all required elements, iterate and call required functions
        raise NotImplementedError

    def score_set(self, labelset):
        # score accuracy, calibration of each set
        obj = self.dev if labelset == "dev" else self.test if labelset =="test" else None
        accuracy = sum(obj.c)/len(obj.c)
        print(f"exact acc: {accuracy}")





def test_structuredlogits():
    tokenized = "I am Jordy VL".split()
    y_true = ["O", "O", "B-PER", "I-PER"]
    
    y_pred = np.array([[0.8, 0.1, 0.1], [0.7, 0.1, 0.2], [0.2, 0.6, 0.2], [0.8, 0.1, 0.1]])
    idx2label = {0: "O", 1: "B-PER", 2: "I-PER"}
    label2idx = {v: k for k, v in idx2label.items()}
    y_true_idx = np.vectorize(label2idx.get)(y_true)


    logits = StructuredLogits(f_x=y_pred, y_true=y_true, y_hat=None, probas=y_pred, c=None, tokenized=tokenized, document_masks=[list(range(len(y_true)))])
    logits.logits_to_pred_idx(idx2label)

#test_structuredlogits()