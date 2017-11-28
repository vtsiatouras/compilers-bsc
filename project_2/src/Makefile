all: compile

compile:
	java -jar jtb132di.jar -te mini-java.jj
	java -jar javacc5.jar mini-java-jtb.jj
	javac Main.java
clean:
	rm -rf *.class *~ syntaxtree/*.class visitor/*.class
