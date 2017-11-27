all: compile

compile:
	java -jar jtb132di.jar mini-java.jj
	java -jar javacc5.jar mini-java-jtb.jj
	javac Main.java

clean:
	rm -f *.class *~
