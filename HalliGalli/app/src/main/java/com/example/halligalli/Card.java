package com.example.halligalli;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

public class Card {

    /* 할리갈리 규칙

    1개짜리 5장, 2개짜리 3장, 3개짜리 3장, 4개짜리 2장, 5개짜리 1장씩 => 총 14 / 56장

    바나나 0, 라임 1, 자두 2, 딸기 3
     */

    public static void main(String[] args){

        ArrayList<ArrayList<Integer>> list = getArrayLists();

        // 카드 섞기
        Collections.shuffle(list);

        // 카드 나누기
        int halfSize = list.size() / 2;
        ArrayList<ArrayList<Integer>> playerCard = new ArrayList<>(list.subList(0, halfSize));  // 앞 절반
        ArrayList<ArrayList<Integer>> opponentCard = new ArrayList<>(list.subList(halfSize, list.size()));  // 뒤 절반

        ArrayList<Integer> playerCardTop = new ArrayList<>();  // 빈 ArrayList 할당
        ArrayList<Integer> opponentCardTop = new ArrayList<>();  // 빈 ArrayList 할당

        ArrayList<ArrayList<Integer>> storeCard = new ArrayList<>();






        // 결과 출력 (카드 확인)
        System.out.println("Player's Cards:");
        for (ArrayList<Integer> card : playerCard) {
            System.out.println(card);
        }

        System.out.println("\nOpponent's Cards:");
        for (ArrayList<Integer> card : opponentCard) {
            System.out.println(card);
        }



    }

    @NonNull
    private static ArrayList<ArrayList<Integer>> getArrayLists() {
        ArrayList<ArrayList<Integer>> list = new ArrayList<>(); // ArrayList 선언

        int cnt = 0;
        int value = 0;

        // 카드 초기화
        for(int i = 0; i < 4; i++) { // 카드 종류 4개 (바나나, 라임, 자두, 딸기)
            for(int j = 0; j < 14; j++) { // 각 카드별 14장
                switch (j) {
                    case 0: case 1: case 2: case 3: case 4:
                        value = 1; // 0~4일 경우
                        break;
                    case 5: case 6: case 7:
                        value = 2; // 5~7일 경우
                        break;
                    case 8: case 9: case 10:
                        value = 3; // 8~10일 경우
                        break;
                    case 11: case 12:
                        value = 4; // 11~12일 경우
                        break;
                    case 13:
                        value = 5; // 13일 경우
                        break;
                }

                // ArrayList에 값 추가
                ArrayList<Integer> card = new ArrayList<>();
                card.add(i); // 카드 종류
                card.add(value); // 카드 값
                list.add(card); // 리스트에 카드 추가
                cnt++;
            }
        }
        return list;
    }

    // 플레이어 카드와 상대방 카드의 조건을 확인하는 함수
    //TODO : 수정 필요
    public static int checkCards(ArrayList<Integer> playerCardTop, ArrayList<Integer> opponentCardTop) {
        // 첫 번째 값이 일치하고 두 번째 값들의 합이 5이면 1 반환
        if (playerCardTop.get(0).equals(opponentCardTop.get(0)) && (playerCardTop.get(1) + opponentCardTop.get(1)) == 5) {
            return 1;
        }
        return 0;
    }
}
