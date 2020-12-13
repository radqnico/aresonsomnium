package it.areson.aresonsomnium;

import it.areson.aresonsomnium.entities.SomniumPlayer;
import org.bukkit.plugin.java.JavaPlugin;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class AresonSomnium extends JavaPlugin {

    EntityManagerFactory entityManagerFactory;
    EntityManager entityManager;

    @Override
    public void onDisable() {
        entityManager.close();
        entityManagerFactory.close();
    }

    @Override
    public void onEnable() {
        entityManagerFactory = Persistence.createEntityManagerFactory("aresonSomnium");
        entityManager = entityManagerFactory.createEntityManager();

        SomniumPlayer somniumPlayer = new SomniumPlayer();

        entityManager.getTransaction().begin();
        entityManager.persist(somniumPlayer);
        entityManager.getTransaction().commit();
    }

}
