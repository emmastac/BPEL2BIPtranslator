#!/bin/bash
FDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

set -e
set -x verbose

mkdir -p output build

bipc.sh -I . -p check -d "comp()" \
 --gencpp-output output \
--gencpp-enable-marshalling \
--gencpp-cc-I $PWD/src-ext

cd build

cmake $(ls -d ${FDIR}/output)
make

echo "Finished. Run: ./build/system"
