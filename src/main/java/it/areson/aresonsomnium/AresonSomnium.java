package it.areson.aresonsomnium;

import it.areson.aresonsomnium.entities.SomniumPlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Properties;

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

        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        entityManagerFactory = Persistence.createEntityManagerFactory("persistence-unit");

        SomniumPlayer somniumPlayer = new SomniumPlayer();

        entityManager.getTransaction().begin();
        entityManager.persist(somniumPlayer);
        entityManager.getTransaction().commit();
    }

}
