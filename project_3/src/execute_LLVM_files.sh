#!/bin/bash

for filename in LLVM/*.ll; do
    echo "Compiling ${filename} with clang"
    clang-4.0 "${filename}" -o "LLVM/${filename##*/}".out
    echo "Your output:"
    ./"LLVM/${filename##*/}".out
done
