package com.scepticallistic.kevin.tipcalc;

public class Person {
    public double subtotal, taxAmount, tipAmount, total;

    public Person() {

    }

    public void setSubtotal(double sub) {
        subtotal = sub;
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
