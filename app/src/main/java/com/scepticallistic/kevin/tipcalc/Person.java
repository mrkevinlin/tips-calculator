package com.scepticallistic.kevin.tipcalc;

public class Person {
    public double subtotal, taxAmount, tipAmount, total;

    public Person() {

    }

    public void setSubtotal(double sub) {
        subtotal = sub;
    }

    public void setTaxAmount(double tax) {
        taxAmount = subtotal * tax;
    }

    public void setTipAmount(double tip) {
        tipAmount = (subtotal + taxAmount) * tip;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setAllValues(double sub, double tax, double tip) {
        subtotal = sub;
        taxAmount = sub * tax;
        tipAmount = (subtotal + taxAmount) * tip;
    }

    public String calculateTotal() {
        total = subtotal + taxAmount + tipAmount;
        return String.format("%.2f", total);
    }
}
