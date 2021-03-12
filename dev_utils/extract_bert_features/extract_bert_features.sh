CONLL_DIR=/home/jordy/code/SP-calibration-NER/acl2019_nested_ner/data/ace2004/

export BERT_MODEL_PATH="./cased_L-24_H-1024_A-16"
python3 extract_features.py --input_file=$CONLL_DIR"train.biluo.conll;"$CONLL_DIR"dev.biluo.conll;"$CONLL_DIR"test.biluo.conll"
--output_file=$CONLL_DIR"/bert_features.hdf5" --bert_config_file $BERT_MODEL_PATH/bert_config.json --init_checkpoint $BERT_MODEL_PATH/bert_model.ckpt --vocab_file  $BERT_MODEL_PATH/vocab.txt --do_lower_case=False --stride 1 --window-size 129
