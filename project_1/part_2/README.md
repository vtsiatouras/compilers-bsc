Parser & Translator
---

### Compile & Execution

Η μεταγλώττιση γίνεται μέσω Makefile

 __make__ : Compile  
 __make clean__ : Διαγραφη .class αρχείων

 __make execute < [input_file]__ : Εκτέλεση με input απο αρχείο

 *Έχω προσθέσει κάποια test files στο directory inputs*

 ### Παραδοχές

 * Τα μεταφρασμένα αρχεία αποθηκεύονται στο directory "out". Αν δεν υπάρχει το συγκεκριμένο dir δημιουργείται κατά την εκτέλεση. Επίσης αν υπάρχει το directory και υπάρχει το αρχείο "Main.java", σε επομένη εκτέλεση τα περιεχόμενα του αρχείου γίνονται overwrite με τον νέο κώδικα που παράχθηκε.
 * Η γραμματική υποστηρίζει όλες τις λειτουργίες που αναφέρονται στην εκφώνηση.
 * Στα declarations των ρουτίνων, τα ορίσματα πρέπει να είναι **μονο** Identifier.
 * Στα top level calls, τα ορίσματα μπορούν να είναι string, function call, if/else & concatenation statements. Η χρήση identifier επιτρέπεται μόνο αν πρόκειται για όνομα συνάρησης.
 * Στα in-body function calls επιτρέπονται όλες οι δυνατές τιμές ενός expression.
 * Τα if/else expressions μεταφράζονται σε Ternary expressions.
