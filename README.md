# README #

Meta-repository in which I keep environments and repositories
from which to obtain trained predictors where to estimate SCE on.

## Goals

1. keep repositories, 
2. within each keep trained models
3. within each make a predict / logits script
4. align data loaders to compare at instance-level between models


### Nested NER TACL

1. second best decoding 

We need to get some logit space that stands for all possible label sequences
So 

N_test x L x K x K
% x K #nesting depth, at most K 

Combination-wise: L^(K x K)

=> use K-level CRFs for decoding, so average the energy over K CRFs.

These are now our logits!
The decoding and predictions are special in its own right, will have to use the repo :/
=> check which elements required to form TouristLeMC object

% have to create a new object containing this?
% can simplify a lot [only nonbayesian]
%% whatever gets me started fastest
%%% also will have to create a vector/tensor representing y_true & correctness 

2. have to adapt to Transformers

----

### Seq2Seq NER (ACL 2019)

1. already converted ACE to conll format
2. next step to get embeddings

This: https://github.com/Adaxry/get_aligned_BERT_emb 
might come in handy

3. 

----


## Data Preprocessing 

1. XML format (original)
2. nested format (secondbest-decoding-NER)
3. conll format (seq2seq-NER)
4. jsonlines format (biaffine-NER)
5. (rasa NLU format) (pyramid-NER)

Have converters from 1-2, 2-3. 


### Created virtual env TF1

---

# setup a virtualenv for each repository:

######################################

var="XXX"
% https://git-scm.com/book/en/v2/Git-Tools-Submodules

1. install virtualenv $var


```sh
sudo apt-get install python3-pip
sudo apt-get install python3-venv
sudo pip3 install virtualenv
sudo pip3 install virtualenvwrapper

# set the following .bashrc
export VIRTUALENVWRAPPER_PYTHON=/usr/bin/python3.8
export WORKON_HOME=$HOME/.virtualenvs
source /usr/local/bin/virtualenvwrapper.sh

# optional bashrc values
alias py='python3'
ulimit -n 9048
export PYTHONIOENCODING=utf8


#FINALLY run 
mkvirtualenv -p /usr/bin/python3.8 -a $HOME/code/SP-calibration-NER/$var $var
```

2. install poetry and finish virtual environment

```sh
#Using poetry and the readily defined pyproject.toml, we will install all required packages
workon $var
pip3 install poetry
cd $HOME/code/$var
poetry install
```