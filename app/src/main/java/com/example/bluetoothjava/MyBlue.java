package com.example.bluetoothjava;

public class MyBlue {
    private String name;
    private String address;

    public MyBlue(String name, String address) {
        if (name != null){
            this.name = name;
        }else this.name = "noname";

        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String toString() {
        return this.name;
    }
}
