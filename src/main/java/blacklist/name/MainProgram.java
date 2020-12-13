package blacklist.name;

import blacklist.name.services.NameMatcherService;

import java.util.List;
import java.util.Map;

public class MainProgram {

	public static final String NAMES_FILENAME = "names.txt";
	public static final String NOISE_WORDS_FILENAME = "noise_words.txt";

	public static void main(String[] args) {
		String name = "dr. joselyn bin jameson";
		NameMatcherService nameMatcherService = new NameMatcherService(NAMES_FILENAME, NOISE_WORDS_FILENAME);

		if (args.length != 0) {
			for (int i = 0; i < args.length; i++) {
				if (i == 0) {
					name = args[i];
				} else if (i == 1) {
					nameMatcherService.setNameFilename(args[1]);
					nameMatcherService.setNamesFileFromDifferentLocation(true);
				} else if (i == 2) {
					nameMatcherService.setNameFilename(args[2]);
					nameMatcherService.setNoisewordsFileFromDifferentLocation(true);
				}
			}
		}

		nameMatcherService.init();
		String cleanedName;
		Map<String, List<String>> blackListedNamesMap = nameMatcherService.getNamesListMap();
		cleanedName = nameMatcherService.cleanNameOfNoiseWordsAndSpecialCharacters(name);

		// if there is an exact name and variations of it. then consider those to be true
		List<String> matchingNames = nameMatcherService.getMatchingNames(blackListedNamesMap, cleanedName);

		if (matchingNames.size() == 0) {
			matchingNames = nameMatcherService.getClosestMatchingNames(blackListedNamesMap, cleanedName);
		}

		matchingNames.stream().forEach(System.out::println);
	}
}
