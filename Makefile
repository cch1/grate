# Some inspiration: https://github.com/git/git/blob/master/Makefile
# More inspiration: https://clarkgrubb.com/makefile-style-guide
SHELL = /bin/bash

PROJECT_NAME ?= grate

srcfiles = $(shell find src/ -type f -name '*.clj' -or -name '*.cljc' -or -name '*.edn')

testfiles = $(shell find test/ -type f -name '*.clj' -or -name '*.cljc' -or -name '*.edn')

input = ./results/atp_matches_futures_2015.csv
output = ./atp_matches_futures_2015-rankings.csv
tmpdir = ./tmp

# This is the default target because it is the first real target in this Makefile
.PHONY: default # Same as "make run"
default: run

.PHONY: run # Process the default input file and produce the default output file
run: $(output)

$(output): $(srcfiles) $(input)
	cat $(input) | clj -X atp.core/rate glicko 50 > $@

.PHONY: test # Run the test suite
test: .make.test

.make.test: deps.edn $(testfiles) $(srcfiles)
	clojure -M:test:test-runner
	touch .make.test

.PHONY: lint # Lint the source code
lint: .make.lint

.make.lint: $(srcfiles)
	clojure -M:lint ; test $$? -lt 3
	touch .make.lint

clean:
	rm -f $(output)
	rm -f $(tmpdir)/*
	rm -f .make.*

# Copied from: https://github.com/jeffsp/makefile_help/blob/master/Makefile
# Tab nonesense resolved with help from StackOverflow... need a literal instead of the \t escape on MacOS
help: # Generate list of targets with descriptions
	@grep '^.PHONY: .* #' Makefile | sed 's/\.PHONY: \(.*\) # \(.*\)/\1	\2/' | expand -t20
