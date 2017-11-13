LL(1) Parser - Arithmetic Calculator
---

### Compile & Execution

Η μεταγλώττιση γίνεται μέσω Makefile  
 __make__ : Compile  
 __make clean__ : Διαγραφη .class αρχείων

 __java Main__ : Εκτέλεση με input απο console   
 __java Main < [input_file]__ : Εκτέλεση με input απο αρχείο (κάθε expression πρέπει να είναι σε ξεχωριστές γραμμές)

 ** Έχω προσθέσει κάποια test files στο directory inputs*

### Grammar Transformation

Έστω η ακόλουθη γραμματική χωρίς συμφραζόμενα:

```
exp --> num
      | exp op exp
      | ( exp )  

op  --> +
      | -
      | *
      | /  

num --> 0
      | 1
      | 2
      | 3
      | 4
      | 5
      | 6
      | 7
      | 8
      | 9
```

Η παραπάνω είναι αριστερά αναδρομική επομένως πρέπει να μετατραπεί σε μια ισοδύναμη μη αριστερά αναδρομική.


H LL(1) εκδοχή της παραπανω γραμματικής είναι η εξής:


```
goal  --> exp
exp   --> term exp2

exp2  --> + term exp2
        | - term exp2
        | ε

term  --> factor term2

term2 --> * factor term2
        | / factor term2
        | ε

factor--> num
        | ( exp )  
        | ε    

num   --> 0
        | 1
        | 2
        | 3
        | 4
        | 5
        | 6
        | 7
        | 8
        | 9
```

### FIRST, FIRST+ & FOLLOW sets for the LL(1) Grammar  

__FIRST sets__
```
FIRST(goal) = FIRST(exp) = FIRST(term) = FIRST(factor) = { 0,1,2,3,4,5,6,7,8,9,( }
FIRST(exp2) = { +,-,ε }
FIRST(term2) = { *,/,ε }
```
__FOLLOW sets__
```
FOLLOW(goal) = { $,) }
FOLLOW(exp) = FOLLOW(goal) = { $,) }
FOLLOW(exp2) = FOLLOW(exp) = { $,) }
FOLLOW(term) += FIRST(exp2) += { +,-,ε } += { +,-,FOLLOW(exp) } += { +,-,$,) }
FOLLOW(term2) = FOLLOW(term) = { +,-,$,) }
FOLLOW(factor) += FIRST(term2) += { *,/,ε } += { *,/,FOLLOW(term) } = { *,/,+,-,$,) }
```  
__FIRST+ sets__
```
FIRST+(goal) = FIRST+(exp) = FIRST+(term) = FIRST+(factor) = { 0,1,2,3,4,5,6,7,8,9,( }
FIRST+(exp2) = FIRST(exp2) U FOLLOW(exp2) = { +,-,$,) }
FIRST+(term2) = FIRST(term2) U FOLLOW(term2) = { *,/,+,-,$,) }
```
### Lookahead Table

| |+,-|*,/|num,(|
|:---:|:---:|:---:|:---:|
|**exp2**|term exp2|error|error|
|**term2**|ε|factor term2|error|
|**factor**|error|error|success|
