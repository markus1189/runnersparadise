#!/usr/bin/env bash
nix-shell --pure --command "latexmk -shell-escape -pdf"
