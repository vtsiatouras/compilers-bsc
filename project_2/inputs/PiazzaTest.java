class Main{
    public static void main(String[] argv){
        A a;
        B b;
        a=a.foo(new B());
        a=a.foo(new A());
//        b=a.foo(new A());

        a=b.foo(b);
//        b=b.foo(b);
        a=b.foo(a);
//        b=b.foo(a);
    }
}

class A {
    B obj;

    public A foo(A anArg){
        return anArg;
    }
}

class B extends A{
    int x;

    public int getX() {
        return x;
    }

    public A foo(A anArg){
        A tempA;
        B tempB;
        tempA = anArg.foo(this);
        tempA = this.foo(this);
//        tempB = this.foo(this);
        return new A();
    }


    public A testThis(B anArg){
        int res;
        res = anArg.getX();
        System.out.println( res );
        return this;
    }

    public A testNewSub(A anArg){
        return new B();
    }

    public B testNewSame(A anArg){
        return new B();
    }

//    public B testNewWrong(A anArg){
//        return new A();
//    }

}
