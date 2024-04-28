package edu.innotech;

public class Fraction implements Fractionable{
    private int num;
    private int denum;

    public Fraction(int num, int denum) {
        this.num = num;
        this.denum = denum;
    }

    @Override
    @Mutator
    public void setNum(int num){
        this.num = num;
    }

    @Override
    @Mutator
    public void setDenum(int denum){
        this.denum = denum;
    }

    @Override
    @Cache
    public double doubleValue(){
        return (double) num/denum;
    }

    @Override
    @Cache
    public String toString(){
        return "Fraction{" + "num = " + num + ", denum = " + denum + "}";
    }


}