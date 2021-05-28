package it.areson.aresonsomnium.books;

import com.google.common.base.Splitter;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.LinkedList;
import java.util.List;

public class BookBuilder {
    private ItemStack writtenBook;
    private BookMeta bookMeta;
    private final int charactersPerPage = 256;

    public BookBuilder() {
        this.writtenBook = new ItemStack(Material.WRITTEN_BOOK);
        this.bookMeta = (BookMeta) this.writtenBook.getItemMeta();
    }

    public void buildWrittenBook(String title, String author, Iterable<String> pages) {
        this.bookMeta.setTitle(title);
        this.bookMeta.setAuthor(author);
        for (String page: pages) {
            String colouredText = ChatColor.translateAlternateColorCodes('&', page);
            this.bookMeta.addPages(Component.text(colouredText));
        }
        this.applyMeta();
    }

    public void buildWrittenBook(String title, String author, String content) {
        LinkedList<String> sortedPages = new LinkedList<>();
        String[] splitContent = content.split(" ");
        int lastPage = 0;
        String page = "";
        for(String word: splitContent) {
            if(page.length() + word.length() < this.charactersPerPage) {
                page += word + " ";
            } else {
                sortedPages.add(page);
                page = word + " ";
            }
        }
        this.buildWrittenBook(title, author, sortedPages);
    }

    private void applyMeta() {
        this.writtenBook.setItemMeta(this.bookMeta);
    }

    public ItemStack getWrittenBook() {
        return writtenBook;
    }
}
