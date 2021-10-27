package it.areson.aresonsomnium.books;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.LinkedList;

public class BookBuilder {
    private final ItemStack writtenBook;
    private final BookMeta bookMeta;

    public BookBuilder() {
        this.writtenBook = new ItemStack(Material.WRITTEN_BOOK);
        this.bookMeta = (BookMeta) this.writtenBook.getItemMeta();
    }

    public void buildWrittenBook(String title, String author, Iterable<String> pages) {
        this.bookMeta.setTitle(title);
        this.bookMeta.setAuthor(author);
        for (String page : pages) {
            String colouredText = ChatColor.translateAlternateColorCodes('&', page);
            this.bookMeta.addPages(Component.text(colouredText));
        }
        this.applyMeta();
    }

    public void buildWrittenBook(String title, String author, String content) {
        LinkedList<String> sortedPages = new LinkedList<>();
        String[] splitContent = content.split(" ");
        StringBuilder page = new StringBuilder();
        for (String word : splitContent) {
            int charactersPerPage = 256;
            if (page.length() + word.length() < charactersPerPage) {
                page.append(word).append(" ");
            } else {
                sortedPages.add(page.toString());
                page = new StringBuilder(word + " ");
            }
        }
        sortedPages.add(page.toString());
        this.buildWrittenBook(title, author, sortedPages);
    }

    private void applyMeta() {
        this.writtenBook.setItemMeta(this.bookMeta);
    }

    public ItemStack getWrittenBook() {
        return writtenBook;
    }
}
