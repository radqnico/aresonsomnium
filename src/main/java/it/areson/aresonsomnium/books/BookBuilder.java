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
        Iterable<String> pages = Splitter.fixedLength(256).split(content);
        this.buildWrittenBook(title, author, pages);
    }

    private void applyMeta() {
        this.writtenBook.setItemMeta(this.bookMeta);
    }

    public ItemStack getWrittenBook() {
        return writtenBook;
    }
}
