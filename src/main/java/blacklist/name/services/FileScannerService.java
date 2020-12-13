package blacklist.name.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileScannerService {

	// todo make service that creates a file with new names

	public List<String> getListFromFile(final String filename, boolean fileFromDifferentLocation) {
		List<String> fileLinesList = null;

		try {
			URI resource;

			if (fileFromDifferentLocation) {
				resource = new File(filename).toURI();
			} else {
				resource = getFileURIResource(filename);
			}
			Stream<String> lines = Files.lines(Path.of(resource));

			fileLinesList = lines.collect(Collectors.toList());
			fileLinesList.forEach(String::toLowerCase);
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		return fileLinesList;
	}

	private URI getFileURIResource(final String filename) throws URISyntaxException {
		return getClass().getClassLoader().getResource(filename).toURI();
	}

}
