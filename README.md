## README

This is just a first stab at a new version of the OWL entailment extractor described in "Samantha Bail, Bijan Parsia, Uli Sattler. Extracting finite sets of entailments from OWL ontologies. In Proc. of DL 2011, 2011." [PDF available here](https://dl.dropbox.com/u/3074592/publications/entailments-dl2011.pdf)

## The currently submitted options are:
* reasoner:{pellet | hermit | jfact} *feel free to extend for other reasoners*
* includeAtomicSubs:true/false
* includeAtomicEquiv:true/false (*not implemented*)
* includeUnsatClasses:true/false
* includeAsserted:true/false
* includeTop:true/false
* includeNonStrict:true/false
* includeIndirect:true/false

## Dealing with imported entailments:

* includeNative:true/false
* includeImported:true/false (*false implemented by default*)
* includeMixed:true/false

The lib include jars for some of the reasoners *just to get started*. It does *not* include Pellet. I recommend downloading the latest versions of the reasoners and replacing the current libs if the code is used in production.

## Test files

 There are test files in *testfiles*. Yeah. This includes a simple toy ontology puma.owl and a sample config file which lists all currently available options. 