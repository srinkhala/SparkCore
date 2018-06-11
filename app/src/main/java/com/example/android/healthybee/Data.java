package com.example.android.healthybee;

public class Data {
    private String foobar = null;

    public boolean isReady() {
        return (foobar != null);
}
    public String getFoobar() { return foobar; }

    public void setFoobar(String foobar) { this.foobar = foobar; }

    @Override
    public String toString() {
        return "Data [Foobar=" + foobar + "]";
    }
}
