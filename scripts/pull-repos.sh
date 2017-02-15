#!/bin/sh

#
# Usage: pull-repos.sh <username>
# Example: pull-repos.sh cmacfarl
#

if [ -z $1 ]; then
  echo "Please supply the github username corresponding to the repos you wish to pull"
  exit 0
fi

git clone https://github.com/$1/ftc_app.git
git clone https://github.com/$1/terkel.git
git clone https://github.com/$1/FTC-Team-25.git

