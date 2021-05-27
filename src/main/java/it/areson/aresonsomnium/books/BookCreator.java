package it.areson.aresonsomnium.books;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BookCreator {
    public ItemStack createEmptyWrittenBook() {
        ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
        return writtenBook;
    }
}
