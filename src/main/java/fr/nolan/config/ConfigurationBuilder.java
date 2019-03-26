package fr.nolan.config;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;

public class ConfigurationBuilder {

    private final FileBasedConfigurationBuilder<FileBasedConfiguration> fileBasedConfigurationBuilder;

    public ConfigurationBuilder(Class<? extends FileBasedConfiguration> type, File file) {
        fileBasedConfigurationBuilder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(type)
                .configure(new Parameters().properties().setFile(file));
    }

    public FileBasedConfiguration getConfiguration() {
        try {
            return fileBasedConfigurationBuilder.getConfiguration();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

}
