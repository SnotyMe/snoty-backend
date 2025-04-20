#!/bin/sh

git-cliff \
	--tag "$1" \
	-u \
	--use-branch-tags \
	-w ../.. # use root of repository as workdir
