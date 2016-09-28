# mwes_m2
Java implementation of an MWE identification method (only two word noun compound category) based on the non-substitutability of MWEs.

Introduction:
=======================================================

The `unige.cui.meghdad.nlp.mwe2` package implements a model of extracting two-word multiword expressions (MWEs) or collocations based on the Substitution Driven Measures of Association (SDMAs).  

`unige.cui.meghdad.nlp.mwe2` implements `m1` and `m2` models presented at: TO_APPEAR

Note
=======================================================
Since MWEs are better defined on a spectrum of idiosyncrasy and not as a binary phenomena, the program generates a ranked list of MWEs. 
The compounds at the top of this list are those that are least non-substitutable and consequently more idiosyncratic or lexically rigid. 
The compounds at the bottom of the list on the other hand are more substitutable and hence less idiosyncratic.


Command Line Quick Start
=======================================================

The program can 

After downloading the repository, in the command line, change directory to `dist/`
Then run the following command: 

Here, path to the POS tagged corpus must be provided through "-p2corpus" option. 
Other flags that are optional include:

`-maxRank` Indicates the top n ranked MWEs that will be returned. Defaul=200. 

`-rc` Ranking criterion: delta_12, delta_21, or combined. Default = delta_21. 
(for more information about the criteria see the article). 

#### Example:

`java -cp dist/cui-mf-nlp-mwe-m1.jar unige.cui.meghdad.nlp.mwe1.Collocational_Bidirect_Prob_Corpus -p2corpus "PATH_2_POSTAGGED_CORPUS"`


### 2. To rank a list of MWE candidates that are provided in an input file. 

Here, path to the list of POS tagged two-word candidates (through -p2POSTaggedCandidates), path to a list of all bigrams (through -p2bigrams) and all unigrams (through -p2unigrams) extracted from the corpus must be provided.
Other flags that are optional include:

`-rc` Ranking criteria: delta_12, delta_21, or combined. Default = delta_21.

#### Example:

`java -cp dist/cui-mf-nlp-mwe-m1.jar unige.cui.meghdad.nlp.mwe1.Collocational_Bidirect_Prob_File -p2POSTaggedCandidates "PATH_2_POSTAGGED_CANDIDATES" -p2bigrams "PATH_2_BIGRAMS" -p2unigrams "PATH_2_UNIGRAMS"`


Contact:
=======================================================

To report bugs and other issues and if you have any question please contact: meghdad.farahmand@gmail.com


