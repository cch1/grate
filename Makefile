# Some inspiration: https://github.com/git/git/blob/master/Makefile
# More inspiration: https://clarkgrubb.com/makefile-style-guide
SHELL = /bin/bash

teams := Stratos Tanagra

# This is the default target because it is the first real target in this Makefile
.PHONY: default
default: update

.PHONY: update
update:
	git pull
	git submodule update --init

.PRECIOUS: results/%/
results/%/:
	mkdir -p $@

results/%/log.txt: | results/%/
	git log --grep $* --topo-order --reverse --decorate=no --abbrev=8 --pretty=format:"%h %an %cI %s" > $@

.PHONY: clean
clean:
	rm -rf results/

.PHONY: Event01 # The ESC Dash
Event01: results/Event01/log.txt

.PHONY: Event02 # The ESC Relay
Event02: results/Event02/log.txt $(foreach team, $(teams), results/Event02/$(team).out)

results/Event02/%.out: | results/Event02/
	-(cd $*; git log --grep $* --topo-order --reverse --decorate=no --abbrev=8 --pretty=format:"%h %an %cI %s") | head -4 > $@

.PHONY: Event03 # The CSV Reader
Event03: results/Event03/log.txt $(foreach team, $(teams), results/Event03/$(team).out)

results/Event03/%.out: | results/Event03/
	-./$*/Event03 $(FIELD) $(ROW) > $@

.PHONY: Event04 # The ATP Results Accumulator
Event04: results/Event04/log.txt $(foreach team, $(teams), results/Event04/$(team).out)

results/Event04/%.out: | results/Event04/
	-./$*/Event04 > $@

