# Project Title

Music offers an extremely rich and interesting playing field. The objective of this miniproject is to develop models that are able to recognize the genre of a musical piece, starting from the raw waveform or from pre-computed features. This is a typical example of a classification problem on time series data.


The music belongs to one for each genres:
- electronic
- folkcountry
- jazz
- raphiphop
- rock


# Questions

## Question 1 (these names should match the functions in `__init__.py`)

Your goal is to build a transformer that will output a "song fingerprint" feature vector that is based on the 2 raw features mentioned above. This vector has to have the same size, regardless of the duration of the song clip it receives.


## Question 2
## All Features Predictions
The approach of question 1 can be generalized to any number and kind of features extracted from a sliding window. Use the [librosa library](http://bmcfee.github.io/librosa/) to extract features that could better represent the genre content of a musical piece.
I used
- spectral features to capture the kind of instruments contained in the piece
- MFCCs to capture the variations in frequencies along the piece
- Temporal features like tempo and autocorrelation to capture the rythmic information of the piece
- features based on psychoacoustic scales that emphasize certain frequency bands.
- any combination of the above

## Question 3
- a normalization step (not all features have the same size or distribution)
- a dimensionality reduction or feature selection step
- ... any other transformer you may find relevant ...
- an estimator
- a label encoder inverse transform to return the genre as a string

