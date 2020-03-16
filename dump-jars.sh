#!/bin/bash

find . -name "*.jar" -print -exec unzip -t {}  \;
