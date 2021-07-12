import numpy as np 
from collections import OrderedDict

from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from conformal_predictor import InductiveConformalPredictor #conformal.
from sklearn.datasets import load_digits, load_iris, fetch_20newsgroups_vectorized
from sklearn import svm


# source: https://medium.com/data-from-the-trenches/measuring-models-uncertainty-with-conformal-prediction-f6aa8debb50e


data = load_digits()

data = fetch_20newsgroups_vectorized(subset="all")
X, y = data.data, data.target
"""
n_samples = len(data.images)
X = data.images.reshape((n_samples, -1))
#X = X / X.max()
"""

alpha = 0.05
seed = 42

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3,
                                                    stratify=y, random_state=seed)

# Further splitting of data into test and calibration
X_test, X_calib, y_test, y_calib = train_test_split(X_test, y_test, test_size=0.5, 
                                                    stratify=y_test, random_state=seed)

# Fitting classifier to train data
clf = LogisticRegression(max_iter=10000) 
#clf = svm.SVC(kernel='linear', probability=True)
clf.fit(X_train, y_train)

# Fitting conformal predictor
cfp = InductiveConformalPredictor(predictor=clf)
cfp.fit(X_test, y_test)

# Scoring of classifier and conformal predictor
y_test_conf = cfp.predict(X_calib, alpha=alpha)


def e_per_eff(y_true, y_sets):
    conformal = [True if t in p else False for t,p in zip(y_true, y_sets)]

    d = OrderedDict()
    for l in sorted(set([len(x) for x in y_sets])):
        sets_mask = [i for i, x in enumerate(y_sets) if len(x) == l]
        d[l] = [True if t in p else False for t,p in zip(np.array(y_true)[sets_mask], np.array(y_sets)[sets_mask])]
        d[l] = 1- (sum(d[l])/len(d[l]))
    return d

"""
On calibration set!
"""
print("** calibration set **")
y_pred = clf.predict(X_calib)

y_test_prob = cfp.predict_proba(X_calib, mondrian=True)

y_test_prob_nonmondrian = cfp.predict_proba(X_calib, mondrian=False)

y_test_conformity_score = cfp._uncertainty_conformity_score(X_calib)

correctness = [True if p == t else False for t,p in zip(y_calib, y_pred)]
conformal = [True if t in p else False for t,p in zip(y_calib, y_test_conf)]

accuracy = sum(correctness)/len(correctness) #absolute random
print(accuracy)

conformality = sum(conformal)/len(conformal)
print(conformality)

error_rate_per_efficiency = e_per_eff(y_calib, y_test_conf)
print(error_rate_per_efficiency)


"""
On test set
"""
print("** test set **")
y_pred = clf.predict(X_test)
y_test_conf = cfp.predict(X_test, alpha=alpha)

y_test_prob = cfp.predict_proba(X_test, mondrian=True)

y_test_prob_nonmondrian = cfp.predict_proba(X_test, mondrian=False)

y_test_conformity_score = cfp._uncertainty_conformity_score(X_test)

correctness = [True if p == t else False for t,p in zip(y_test, y_pred)]
conformal = [True if t in p else False for t,p in zip(y_test, y_test_conf)]

accuracy = sum(correctness)/len(correctness) #absolute random
print(accuracy)

conformality = sum(conformal)/len(conformal)
print(conformality)

# print(y_test_conf)
# print(y_test_prob)
# print(y_test_prob_nonmondrian)
# print(y_test_conformity_score)
# print(y_pred)

# import pandas as pd 

# df = pd.DataFrame()
# df["y_hat"] = y_pred
# df["p_hat"] = y_test_prob 
# df["p_nonmondrian"] = y_test_prob_nonmondrian 
# df["correct"] = correctness
# df["conformal"] = conformal



import pdb; pdb.set_trace()  # breakpoint 31b36e37 //
