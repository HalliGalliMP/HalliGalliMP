package com.example.halligalli;

public class Card {
    private String fruit;
    private int count;

    public Card(String fruit, int count) {
        this.fruit = fruit;
        this.count = count;
    }

    public String getFruit() {
        return fruit;
    }

    public int getCount() {
        return count;
    }

    // 카드 이미지 리소스 반환 메서드
    public int getCardImageResource() {
        switch (fruit) {
            case "딸기":
                return getStrawberryResource();
            case "바나나":
                return getBananaResource();
            case "라임":
                return getLimeResource();
            case "자두":
                return getPlumResource();
            default:
                return R.drawable.card_deck; // 기본 카드 뒷면 이미지
        }
    }

    private int getStrawberryResource() {
        switch (count) {
            case 1: return R.drawable.strawberry1;
            case 2: return R.drawable.strawberry2;
            case 3: return R.drawable.strawberry3;
            case 4: return R.drawable.strawberry4;
            case 5: return R.drawable.strawberry5;
            default: return R.drawable.card_deck;
        }
    }

    private int getBananaResource() {
        switch (count) {
            case 1: return R.drawable.banana1;
            case 2: return R.drawable.banana2;
            case 3: return R.drawable.banana3;
            case 4: return R.drawable.banana4;
            case 5: return R.drawable.banana5;
            default: return R.drawable.card_deck;
        }
    }

    private int getLimeResource() {
        switch (count) {
            case 1: return R.drawable.lime1;
            case 2: return R.drawable.lime2;
            case 3: return R.drawable.lime3;
            case 4: return R.drawable.lime4;
            case 5: return R.drawable.lime5;
            default: return R.drawable.card_deck;
        }
    }

    private int getPlumResource() {
        switch (count) {
            case 1: return R.drawable.plum1;
            case 2: return R.drawable.plum2;
            case 3: return R.drawable.plum3;
            case 4: return R.drawable.plum4;
            case 5: return R.drawable.plum5;
            default: return R.drawable.card_deck;
        }
    }
}
