#!/bin/sh

# (c) B. Piwowarski, 2017
# Runs the test suite

# Fail on any error
set -e

log() {
    echo "$@" 1>&2
}

log "Build and testing the server"
(
    cd build
    gradle build
    gradle test
)

log "Build and test the CPP library"
(
    cd cpplib
    mkdir build
    (cd build && cmake -C ..)
    make -C experimaestro-tests
)