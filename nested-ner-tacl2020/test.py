from typing import Dict, List
import sys
import os
import numpy as np
import pickle
import torch
from torch import Tensor
import torch.cuda

from config import config
from model.sequence_labeling import BiRecurrentConvCRF4NestedNER
from training.logger import get_logger
from training.utils import pack_target, unpack_prediction
from util.utils import Alphabet, load_dynamic_config


#https://pytorch.org/tutorials/beginner/saving_loading_models.html?highlight=load

this_model_path = "./dumps/ace2004_model_210304_093526.pt"
model_state_dict = torch.load(this_model_path) #'.pt'

# misc info
misc_config: Dict[str, Alphabet] = pickle.load(open(config.config_data_path, 'rb'))
voc_iv_dict, voc_ooev_dict, char_dict, label_dict = load_dynamic_config(misc_config)
config.voc_iv_size = voc_iv_dict.size()
config.voc_ooev_size = voc_ooev_dict.size()
config.char_size = char_dict.size()
config.label_size = label_dict.size()

with open(config.embed_path, 'rb') as f:
    vectors: List[np.ndarray] = pickle.load(f)
    config.token_embed = vectors[0].size
    embedd_word: Tensor = Tensor(vectors)

config.if_gpu = config.if_gpu and torch.cuda.is_available()

print(config)  # print training setting

ner_model = BiRecurrentConvCRF4NestedNER(config.token_embed, config.voc_iv_size, config.voc_ooev_size,
                                         config.char_embed, config.char_size, config.num_filters, config.kernel_size,
                                         config.label_size, embedd_word,
                                         hidden_size=config.hidden_size, layers=config.layers,
                                         word_dropout=config.word_dropout, char_dropout=config.char_dropout,
                                         lstm_dropout=config.lstm_dropout)
if config.if_gpu:
    ner_model = ner_model.cuda()

ner_model.load_state_dict(model_state_dict)

"""
f = open(config.dev_data_path, 'rb')
dev_token_iv_batches, dev_token_ooev_batches, dev_char_batches, dev_label_batches, dev_mask_batches \
    = pickle.load(f)
f.close()
"""

# DATA

f = open(config.test_data_path, 'rb')
test_token_iv_batches, test_token_ooev_batches, test_char_batches, test_label_batches, test_mask_batches \
    = pickle.load(f)
f.close()

batch_zip = zip(test_token_iv_batches,
                test_token_ooev_batches,
                test_char_batches,
                test_label_batches,
                test_mask_batches)

for token_iv_batch, token_ooev_batch, char_batch, label_batch, mask_batch in batch_zip:
    token_iv_batch_var = torch.LongTensor(np.array(token_iv_batch))
    token_ooev_batch_var = torch.LongTensor(np.array(token_ooev_batch))
    char_batch_var = torch.LongTensor(np.array(char_batch))
    mask_batch_var = torch.ByteTensor(np.array(mask_batch, dtype=np.uint8))
    if config.if_gpu:
        token_iv_batch_var = token_iv_batch_var.cuda()
        token_ooev_batch_var = token_ooev_batch_var.cuda()
        char_batch_var = char_batch_var.cuda()
        mask_batch_var = mask_batch_var.cuda()

    # HAVE TO ADAPT THE PREDICT to get confidences! or just logits
    #K x B x L
    #7, 32, 91 #.label
    pred_sequence_entities, logits = ner_model.predict(token_iv_batch_var,
                                           token_ooev_batch_var,
                                           char_batch_var,
                                           mask_batch_var, get_logits=True)

    probs = torch.nn.functional.softmax(logits,dim=-1)
    import pdb; pdb.set_trace()  # breakpoint 929d7903 //

    pred_entities = unpack_prediction(ner_model, pred_sequence_entities) #SPANS
    #[(start, end, label)] for each example

    #-> can we take back the energies with those spans? #only for entities, not marginals :s
    p_a, p, r_a, r = evaluate(label_batch, pred_entities)
"""
"""

label_dict.instance2index
#{'FAC': 0, 'ORG': 1, 'LOC': 2, 'WEA': 3, 'GPE': 4, 'PER': 5, 'VEH': 6}

"""
What should I collect?
"""



