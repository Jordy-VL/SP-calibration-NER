import numpy
from collections import OrderedDict

class StructuredLogits(object):

    """Main class responsible for keeping predictions in logits form for a model
    To be used for calibration

    #text (tokenized), idx2label/decoding function, logits (predicted), gold; generic evaluation
    #some way to obtain a TouristLeMC object
    """

    def __init__(self, modelref, idx2label, *kwargs):
        self.modelref = modelref
        self.idx2label = idx2label
        self.dev = None
        self.test = None

        #self.task = determine_task(self)

    def create_labelset(self, labelset):
        if labelset == "dev":
            pass
        #have some format in which to keep everything
        f_x = None #actual logits
        y_hat = None #predicted values -> could obtain from f_x depending on task
        y = None #gold values
        c = None #some notion of correction at the marginal level 
            #np.array([int(y_hat_i == y_i) for y_i, y_hat_i in zip(y, y_hat)])



def end_to_end_SCE():
    pass
    # can assume to have access to TouristLeMC style object; create on the basis of others
    # test_data =
    # scoring_function =
    # logits =
    # evaluator =
