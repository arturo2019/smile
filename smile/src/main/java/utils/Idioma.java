package utils;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Idioma {
	private Configuration config;

	public Configuration setIdioma(String idioma) throws ConfigurationException {
		switch (idioma) {
		case "espanol":
			config = new PropertiesConfiguration(
					"C:\\smile\\properties\\lang\\" + idioma + ".properties");
			break;
		case "ingles":
			config = new PropertiesConfiguration(
					"C:\\smile\\properties\\lang\\" + idioma + ".properties");
			break;
		default:
			config = new PropertiesConfiguration(
					"C:\\smile\\properties\\lang\\" + idioma + ".properties");
		}

		return config;
	}
}
