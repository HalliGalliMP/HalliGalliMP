package com.example.halligalli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public Card drawCard() {
        if (!cards.isEmpty()) {
            return cards.remove(0); // 덱의 맨 위 카드를 제거하고 반환
        }
        return null; // 카드가 없을 경우 null 반환
    }

    public void clearCard() {
        cards.clear(); // 리스트의 모든 요소 제거
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void addAll(List<Card> newCards) {
        cards.addAll(newCards);
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards); // 덱의 복사본 반환
    }

    // 마지막 카드 몇 장을 가져오는 메서드
    public Card getLastCards() {

        if (!cards.isEmpty()) {
            return cards.get(cards.size() - 1); // 마지막 카드 반환
        }
        return null; // 덱이 비어 있으면 null 반환
    }
}
