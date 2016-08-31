import pandas as pd
column_names=[i for i in range(0,550)]
df_features=pd.read_csv('df_train_anon.csv',header=None)

Y=df_features[549]
y=Y.tolist()

print y

M=df_features.to_dict()

X=[]
for j in range(0,len(M[1])):
    l=[]
    for i in range(0,549):
        l.append(M[i][j])
    X.append(l) 

from sklearn import preprocessing
from sklearn.decomposition import PCA


import scipy as sp
import sklearn as sk
from sklearn.linear_model import LinearRegression
from sklearn.cross_validation import train_test_split

class Estimator_prob3(sk.base.BaseEstimator, sk.base.RegressorMixin):
    """
    A shell estimator that combines a transformer and regressor into a single object.
    """
    def __init__(self, transformer, model):
        self.transformer = transformer
        self.model = model
        pass

    def fit(self, X, y):
        X_trans = self.transformer.fit(X)
        self.model.fit(X_trans, y)
        return self
    
    def score(self, X, y):
        X_test = self.transformer.transform(X)
        return self.model.score(X_test, y)

    def predict(self, X):
        X_test = self.transformer.transform(X)
        X_test=X_test.reshape(1, -1)
        return self.model.predict(X_test)
    
class Transformer_prob3(sk.base.BaseEstimator, sk.base.TransformerMixin):
    """
    Returns the first k columns of a feature array
    """
    def __init__(self):
        self.model= preprocessing.StandardScaler()
        self.pca= PCA(n_components=500)

    def fit(self, X):
        scaler=self.model.fit_transform(X)
        l=[]
        self.model.scale_=self.model.std_.copy()
        for row in scaler:
            l.append(row)
        return self.pca.fit_transform(l)
        
        
    def transform(self, X):
        #X1=self.filter1(X)
        input1=self.model.transform(X)
        l=[]
        for row in input1:
            l.append(row)
        k=self.pca.transform(l)    
        return k


from sklearn import svm

shell = Estimator_prob3(Transformer_prob3(),svm.SVC())

shell.fit(X,y)

k=shell.predict(X[1])

import dill
dill.dump(shell, open("pritesh_music_1.dill", "w"))

print "success"



