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
