package fr.nolan;

import fr.nolan.config.Options;
import fr.nolan.jda.JDAManager;

import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SondaBot {

    private static final ThreadPoolExecutor threadPool;

    public static ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }

    private static final SondaBot INSTANCE;

    public static SondaBot getInstance() {
        return INSTANCE;
    }

    private static final Logger LOGGER;

    public static Logger getLogger() {
        return LOGGER;
    }

    private Options options;
    private Configuration properties;

    static {
        INSTANCE = new SondaBot();
        LOGGER = LogManager.getLogger(SondaBot.class);
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }


    public static void main(String... args) {
        new JDAManager();
    }

    public Options getOptions() {
        return options;
    }

    public Configuration getProperties() {
        return properties;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public void setProperties(Configuration properties) {
        this.properties = properties;
    }

}
