package dev.cromo29.machines.utility;

import dev.cromo29.durkcore.util.MakeItem;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utilitys {

    public static final List<Integer> SLOTS = Arrays.asList(10, 11, 12, 13, 14, 15, 16);

    public static void updatePages(Scroller scroller) {

        scroller.getPages().forEach((page, inv) -> {

            ItemStack nextItem = inv.getItem(scroller.getNextPageSlot());
            ItemStack previousItem = inv.getItem(scroller.getPreviousPageSlot());

            if (nextItem != null && nextItem.getType() == scroller.getNextPageItem().getType()) {

                List<String> nextReplaced = new ArrayList<>();
                for (String text : nextItem.getItemMeta().getLore()) {

                    text = text.replace("<page>", page + "");
                    text = text.replace("<pages>", scroller.getTotalPages() + "");

                    nextReplaced.add(text);
                }

                nextItem = new MakeItem(nextItem)
                        .setLore(nextReplaced)
                        .build();
                inv.setItem(scroller.getNextPageSlot(), nextItem);
            }

            if (previousItem != null && previousItem.getType() == scroller.getPreviousPageItem().getType()) {

                List<String> previousReplaced = new ArrayList<>();
                for (String text : previousItem.getItemMeta().getLore()) {

                    text = text.replace("<page>", page + "");
                    text = text.replace("<pages>", scroller.getTotalPages() + "");

                    previousReplaced.add(text);
                }

                previousItem = new MakeItem(previousItem)
                        .setLore(previousReplaced)
                        .build();
                inv.setItem(scroller.getPreviousPageSlot(), previousItem);
            }
        });

    }
}
