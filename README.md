# README #

## Goals

1. keep repositories, 
2. within each keep trained models
3. within each make a predict / logits script
4. align data loaders to compare at instance-level between models


# setup a virtualenv for each repository:

######################################

var="XXX"

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