/**
 * Created by ek on 27/03/16.
 */

class Animal {
    public void accept(Visitor v){
        v.visit(this);
    }
}

class Dog extends Animal {
    @Override
    public void accept(Visitor v){
        v.visit(this);
    }
}

class Cat extends Animal {
    @Override
    public void accept(Visitor v){
        v.visit(this);
    }
}

class Cow extends Animal {
    @Override
    public void accept(Visitor v){
        v.visit(this);
    }
}

interface Visitor {
    void visit(Animal animal);
    void visit(Dog dog);
    void visit(Cat cat);
    void visit(Cow cow);
}

class SpeakVisitor implements Visitor {
    @Override
    public void visit(Animal animal) {
        System.out.println("*Silence*");
    }

    @Override
    public void visit(Dog dog) {
        System.out.println("The dog says: Woof!");
    }

    @Override
    public void visit(Cat cat) {
        System.out.println("The cat says: Meow!");
    }

    @Override
    public void visit(Cow cow) {
        System.out.println("The cow says: Moooooooo!");
    }
}

class EatVisitor implements Visitor {
    @Override
    public void visit(Animal animal) {
        System.out.println("The animal says: I don't know what to eat.");
    }

    @Override
    public void visit(Dog dog) {
        System.out.println("The dog says: I'll eat anything you offer me to eat.");
    }

    @Override
    public void visit(Cat cat) {
        System.out.println("The cat says: I'll eat some mice.");
    }

    @Override
    public void visit(Cow cow) {
        System.out.println("The cow says: I'll eat some grass.");
    }
}

public class Main {
    public static void main(String[] args) {
        Animal[] animals = { new Animal(), new Dog(), new Cat(), new Cow() };
        SpeakVisitor speakVisitor = new SpeakVisitor();
        EatVisitor eatVisitor = new EatVisitor();

        System.out.println("*** Let's do some talking! ***");
        for(Animal animal : animals)
            animal.accept(speakVisitor);

        System.out.println("\n*** Time for a lunch break! ***");
        for(Animal animal : animals)
            animal.accept(eatVisitor);
    }
}
