#!/bin/sh

git-cliff \
  "$1" \
	--tag "$2" \
	--use-branch-tags \
	--repository ../..
