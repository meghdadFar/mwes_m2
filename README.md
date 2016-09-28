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

The program can be used in two ways.

## 1. To generate a ranked list of MWEs that are directly extracted from corpus.

In progress. 

## 2. To rank a list of MWE candidates that are provided in an input file.

Required:
Path to the corpus in plain text that is segmented and tokenized must be s[ecified through `-p2corpus` option. 
Path to a list of word pairs must be specified through `-p2candidates` option.
Path to a word representation file (w2v output format) must be specified through `-p2wr` option.
Length of the word representations must be specified through size `-size` option.

Optional:
-rc Ranking criterion: m1, m2, or combined. Default = m2 (for more information about the criteria see the article).
-maxRank Indicates the top n ranked MWEs that will be returned. Defaul=200.

#### Example:

`java -Xmx5g -cp dist/cui-mf-nlp-mwe-m2.jar unige.cui.meghdad.nlp.mwe2.MAIN_File -p2wr "PATH_2_POSTAGGED_REPRESENTATIONS" -p2candidates "PATH_2_CANDIDATES" -p2corpus "PATH_2_CORPUS" -size 200`


Contact:
=======================================================

To report bugs and other issues and if you have any question please contact: meghdad.farahmand@gmail.com


