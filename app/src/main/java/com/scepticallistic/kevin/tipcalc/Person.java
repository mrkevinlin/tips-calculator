package com.scepticallistic.kevin.tipcalc;

public class Person {
    public double subtotal, fraction, taxAmount, tipAmount, total;

    public Person() {

    }

    public void setSubtotal(double sub) {
        subtotal = sub;
    }

    public void setFraction(double frac) {
        fraction = frac;
    }

    public void setTaxAmount(double tax) {
        taxAmount = tax;
    }

    public void setTipAmount(double tip) {
        tipAmount = tip;
    }

    public double calculateTotal() {
        total = subtotal + taxAmount + tipAmount;
        return total;
    }
}
