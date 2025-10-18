package com.kidcode.web.magicwords;

import java.util.List;
import java.util.ArrayList;

public class BackpackWords {

    // Adds a new treasure to the backpack
    public static List<String> pack(List<String> backpack, String item) {
        if (backpack == null) {
            backpack = new ArrayList<>();
        }
        backpack.add(item);
        return backpack;
    }

    // Returns the first treasure
    public static String front(List<String> backpack) {
        if (backpack == null || backpack.isEmpty()) {
            return null;
        }
        return backpack.get(0);
    }

    // Returns the last treasure
    public static String back(List<String> backpack) {
        if (backpack == null || backpack.isEmpty()) {
            return null;
        }
        return backpack.get(backpack.size() - 1);
    }
}
