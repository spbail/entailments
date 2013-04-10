## README

This is just a first stab at a new version of the OWL entailment extractor described in "Samantha Bail, Bijan Parsia, Uli Sattler. Extracting finite sets of entailments from OWL ontologies. In Proc. of DL 2011, 2011." [PDF available here](https://dl.dropbox.com/u/3074592/publications/entailments-dl2011.pdf)

## The currently submitted options are:
* reasoner:{pellet | hermit | jfact} *feel free to extend for other reasoners*
* includeAtomicSubs:true/false
* includeAtomicEquiv:true/false
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

## A few notes

* Using includeAtomicEquiv:true in combination with includeNonStrict:true isn't of much use. If you include equivalence axioms, including non-strict is not necessary to preserve the monotone growth of the entailment set, so it would just add lots of duplicate information to the entailment set. Not bad, just not necessary.
* In order to preserve monotonicity of the entailment set growth (i.e. get same or more entailments when adding an axiom to the ontology), we need the most explicit (but non-redundant) entailment set. Current settings for that would be "true" settings for includeAtomicSubs, includeAsserted, includeNonStrict, includeIndirect. Alternatively, includeAtomicSubs, includeAtomicEquiv, includeAsserted, includeIndirect, if you want to include equivalence axioms.
* includeImported:false excludes axioms that are stated in an imported ontology, even if the axiom uses only entities from the root ontology. Not a problem, just something to be aware of.
* Current implementation of equivalences uses n-ary equivalences class axioms, which is the easiest solution for now.
* Current implementation uses exhaustive representation of subsumptions involving n-ary nodes. E.g. in puma.owl we have Equiv(Puma, Cougar), Subcls(Puma, Cat), which results in the entailment set Subcls(Puma, Cat), Subcls(Cougar, Cat). This is the easiest solution that is guaranteed to preserve monotonicity.