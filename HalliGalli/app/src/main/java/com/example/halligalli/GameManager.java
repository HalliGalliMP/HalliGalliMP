package com.example.halligalli;

import java.util.List;

public class GameManager {

    public static void main(String[] args){




    }

    private void createCards(List<Card> deck, String fruit) {
        for (int i = 0; i < 5; i++) deck.add(new Card(fruit, 1)); // 1개짜리 5장
        for (int i = 0; i < 3; i++) deck.add(new Card(fruit, 2)); // 2개짜리 3장
        for (int i = 0; i < 3; i++) deck.add(new Card(fruit, 3)); // 3개짜리 3장
        for (int i = 0; i < 2; i++) deck.add(new Card(fruit, 4)); // 4개짜리 2장
        deck.add(new Card(fruit, 5)); // 5개짜리 1장
    }





}
