import tensorflow as tf
from optparse import OptionParser
from utils import embedding_utils, data_utils, pkl_utils
import config
import os
import sys
import numpy as np
import pandas as pd
import pickle

#DEV_utils = os.path.realpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', "dev_utils"))
DEV_utils = os.path.realpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '../..', "gordon/arkham/arkham/calibration"))
sys.path.append(DEV_utils)
print(DEV_utils)
from evaluator import StructuredLogits, StructuredLogitsStore

def parse_args(parser):
    parser.add_option("-m", "--model", dest="model_name", type="string")
    parser.add_option("--input", dest="input_file", type="string", default="")
    parser.add_option("--dev", dest="dev_file", type="string", default="")
    parser.add_option("--output", dest="output_file", type="string")
    parser.add_option("-e", dest="embedding", default=False, action="store_true")
    options, args = parser.parse_args()
    return options, args

def create_labelset_input(words, mentions, positions, labels, embedding):
    textlen = np.array([embedding.len_transform1(x) for x in words])
    tokenized = words
    words = np.array([embedding.text_transform1(x) for x in words])
    mentionlen = np.array([embedding.len_transform2(x) for x in mentions])
    mentions = np.array([embedding.text_transform2(x) for x in mentions])
    positions = np.array([embedding.position_transform(x) for x in positions])
    label_set = list(zip(words, textlen, mentions, mentionlen, positions, labels))
    return label_set, labels, tokenized

def get_types(model_name, input_file, dev_file, output_file, options):

    checkpoint_file = os.path.join(config.CHECKPOINT_DIR, model_name)
    type2id, typeDict = pkl_utils._load(config.WIKI_TYPE)
    id2type = {type2id[x]:x for x in type2id.keys()}

    #different way? -> data is different!
    # words, mentions, positions, labels = data_utils.load(input_file)
    # n = len(words)

    embedding = embedding_utils.Embedding.restore(checkpoint_file)

    test_set, test_labels, test_tokenized = create_labelset_input(*data_utils.load(input_file), embedding)
    dev_set, dev_labels, dev_tokenized = create_labelset_input(*data_utils.load(dev_file),embedding)

    store = StructuredLogitsStore(model_name, idx2label=id2type, hierarchical=True if "hier" in model_name else False, nested=False)


    graph = tf.Graph()
    with graph.as_default():
        sess = tf.Session()
        saver = tf.train.import_meta_graph("{}.meta".format(checkpoint_file))
        saver.restore(sess, checkpoint_file)

        # DEFINE operations
        input_words = graph.get_operation_by_name("input_words").outputs[0]
        input_textlen = graph.get_operation_by_name("input_textlen").outputs[0]
        input_mentions = graph.get_operation_by_name("input_mentions").outputs[0]
        input_mentionlen = graph.get_operation_by_name("input_mentionlen").outputs[0]
        input_positions = graph.get_operation_by_name("input_positions").outputs[0]
        phase = graph.get_operation_by_name("phase").outputs[0]
        dense_dropout = graph.get_operation_by_name("dense_dropout").outputs[0]
        rnn_dropout = graph.get_operation_by_name("rnn_dropout").outputs[0]

        pred_op = graph.get_operation_by_name("output/predictions").outputs[0]
        #proba_op = graph.get_operation_by_name("output/proba").outputs[0] #proba
        logit_op = graph.get_operation_by_name("output/scores").outputs[0] #proba
        tune_op = graph.get_operation_by_name("tune").outputs[0] # K x K
        # results_op = graph.get_operation_by_name("results").outputs[0] # require labels

        # DO THE SAME FOR DEV set!

        test_batches = data_utils.batch_iter(test_set, 512, 1, shuffle=False)

        all_predictions = []
        all_logits = []
        for batch in test_batches:
            words_batch, textlen_batch, mentions_batch, mentionlen_batch, positions_batch, labels_batch = zip(*batch)
            feed = {
                input_words: words_batch,
                input_textlen: textlen_batch,
                input_mentions: mentions_batch,
                input_mentionlen: mentionlen_batch,
                input_positions: positions_batch,
                phase: False,
                dense_dropout: 1.0,
                rnn_dropout: 1.0
            }
            batch_predictions = sess.run(pred_op, feed_dict=feed)
            all_predictions = np.concatenate([all_predictions, batch_predictions])

            #probas = sess.run(logit_op, feed_dict=feed)
            logit_predictions = sess.run(logit_op, feed_dict=feed)

            if all_logits == []:
                all_logits = logit_predictions
            else:
                all_logits = np.concatenate([all_logits, logit_predictions])

        store.create_labelset(StructuredLogits(f_x=all_logits, y_true=test_labels, tokenized=test_tokenized, y_hat=None, probas=None, c=None, document_masks=None, idx2label=id2type),"test")
        store.score_set("test")
        import pdb; pdb.set_trace()  # breakpoint 6700594a //
        

        dev_batches = data_utils.batch_iter(dev_set, 512, 1, shuffle=False)

        all_predictions = []
        all_logits = []
        for batch in dev_batches:
            words_batch, textlen_batch, mentions_batch, mentionlen_batch, positions_batch, labels_batch = zip(*batch)
            feed = {
                input_words: words_batch,
                input_textlen: textlen_batch,
                input_mentions: mentions_batch,
                input_mentionlen: mentionlen_batch,
                input_positions: positions_batch,
                phase: False,
                dense_dropout: 1.0,
                rnn_dropout: 1.0
            }
            batch_predictions = sess.run(pred_op, feed_dict=feed)
            all_predictions = np.concatenate([all_predictions, batch_predictions])

            #probas = sess.run(logit_op, feed_dict=feed)
            logit_predictions = sess.run(logit_op, feed_dict=feed)

            if all_logits == []:
                all_logits = logit_predictions
            else:
                all_logits = np.concatenate([all_logits, logit_predictions])

        store.create_labelset(StructuredLogits(f_x=all_logits, y_true=dev_labels, tokenized=dev_tokenized, y_hat=None, probas=None, c=None, document_masks=None, idx2label=id2type),"dev")
        store.score_set("dev")


        #np.transpose(prior_utils.create_prior(type_info, hparams.alpha)
        # all_logits.append(logit_predictions)


    # save as pickle 
    with open(os.path.join(os.path.dirname(checkpoint_file), "logits.pickle"), "wb") as f:
        pickle.dump(store, f)

    """     
    df["t1"] = all_predictions[:n]
    df["t2"] = all_predictions[n:]
    df["t1"] = df["t1"].map(id2type)
    df["t2"] = df["t2"].map(id2type)
    df.to_csv(output_file, sep="\t", header=False, index=False)

    np.unique(labels).shape
    (63,)
    """

def get_embeddings(model_name, output_file):
    checkpoint_file = os.path.join(config.CHECKPOINT_DIR, model_name)
    graph = tf.Graph()
    with graph.as_default():
        sess = tf.Session()
        saver = tf.train.import_meta_graph("{}.meta".format(checkpoint_file))
        saver.restore(sess, checkpoint_file)

        embedding_op = graph.get_tensor_by_name("output/W:0")
        type_embedding = sess.run(embedding_op)
        np.save(output_file, type_embedding)

def main(options):
    if options.input_file != "":
        get_types(options.model_name, options.input_file, options.dev_file, options.output_file, options)
    if options.embedding:
        get_embeddings(options.model_name, options.output_file)

if __name__ == "__main__":
    parser = OptionParser()
    options, args = parse_args(parser)
    main(options)
