package com.foodcafe.myapplication;

public class CardItem {
    private int imageRes;
    private String text;

    public CardItem(int imageRes, String text) {
        this.imageRes = imageRes;
        this.text = text;
    }

    public int getImageRes() {
        return imageRes;
    }

    public String getText() {
        return text;
    }
}
