package blacklist.name.services;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NameMatcherService {

	protected static final String SPACE = " ";
	private static final String EMPTY_STRING = "";
	private static final String ALLOWED_CHARS_PATTERN = "[^a-zA-ZüÜäÄõÕöÖ ]";

	private List<String> namesList;
	private List<String> noisewordsList;

	private boolean namesFileFromDifferentLocation;
	private boolean noisewordsFileFromDifferentLocation;

	private String nameFilename;
	private String noisewordsFilename;

	public NameMatcherService(final String nameFilename, final String noisewordsFilename) {
		this.nameFilename = nameFilename;
		this.noisewordsFilename = noisewordsFilename;
	}

	public void init() {
		FileScannerService fileScannerService = new FileScannerService();
		namesList = fileScannerService.getListFromFile(nameFilename, namesFileFromDifferentLocation);
		noisewordsList = fileScannerService.getListFromFile(noisewordsFilename, noisewordsFileFromDifferentLocation);
	}

	public String cleanNameOfNoiseWordsAndSpecialCharacters(String name) {
		String cleanedName = cleanNameOfSpecialCharacters(name);
		//cleanedName = cleanNameOfNoiseWordsFromList(cleanedName, noiseWords);
		cleanedName = cleanNameOfNoisedWordsWithRegex(cleanedName, noisewordsList);
		return cleanedName.toLowerCase();
	}

	private String cleanNameOfSpecialCharacters(String name) {
		return name.replaceAll(ALLOWED_CHARS_PATTERN, EMPTY_STRING).trim();
	}

	private String cleanNameOfNoiseWords(String name, List<String> noiseWords) {
		List<String> listOfNameWords = Arrays.asList(name.split(SPACE));
		listOfNameWords.removeIf(noiseWords::contains);
		return listOfNameWords.toString();
	}

	private String cleanNameOfNoisedWordsWithRegex(String name, List<String> noiseWords) {
		StringBuilder patternOfNoiseWords = new StringBuilder();

		for (int i = 0; i < noiseWords.size(); i++) {
			patternOfNoiseWords.append(noiseWords.get(i));
			if (i != noiseWords.size() - 1) {
				patternOfNoiseWords.append("|");
			}
		}

		Pattern pattern = Pattern.compile("\\b(" + patternOfNoiseWords.toString() + ")\\b\\s?");
		Matcher matcher = pattern.matcher(name);
		return matcher.replaceAll("");
	}

	public Map<String, List<String>> getNamesListMap() {
		return namesListToMap(namesList);
	}

	private Map<String, List<String>> namesListToMap(List<String> blackListedNames) {
		Map<String, List<String>> blackListedMap = new HashMap<>();
		blackListedNames.forEach(name -> blackListedMap.put(name, Arrays.asList(cleanNameOfNoiseWordsAndSpecialCharacters(name).split(" ").clone())));
		return blackListedMap;
	}

	public List<String> getMatchingNames(Map<String, List<String>> blackListMap, String name) {
		List<String> matchingNamesList = new ArrayList<>();
		List<String> searchNameList = Arrays.asList(name.split(SPACE).clone());

		for (Map.Entry<String, List<String>> blackListedName : blackListMap.entrySet()) {
			List<String> blackListedNameList = blackListedName.getValue();
			int matchCounter;

			if (searchNameList.size() != blackListedNameList.size()) {
				continue;
			}

			matchCounter = (int) searchNameList.stream().filter(blackListedNameList::contains).count();
			if (matchCounter == searchNameList.size()) {
				matchingNamesList.add(String.join(" ", blackListedNameList));
			}
		}

		return matchingNamesList;
	}

	public List<String> getClosestMatchingNames(Map<String, List<String>> blackListMap, String name) {
		List<String> foundNames = new ArrayList<>();
		List<String> searchNameList = Arrays.stream(name.split(SPACE).clone())
				.sorted()
				.collect(Collectors.toList());
		final Long resultScoreThreshold = getResultScoreThreshold(searchNameList);

		for (Map.Entry<String, List<String>> blackListedName : blackListMap.entrySet()) {
			List<String> blackListedNameList = blackListedName.getValue().stream()
					.sorted()
					.collect(Collectors.toList());

			if (fuzzyScore(blackListedNameList, searchNameList) >= resultScoreThreshold) {
				foundNames.add(blackListedName.getKey());
			}
		}

		return foundNames;
	}

	public Integer fuzzyScore(List<String> blacklistNameList, List<String> nameList) {
		int resultScore = 0;
		boolean previousMatched = false;

		// for now just compare every name part to every blacklist name part
		for (int nameListIndex = 0; nameListIndex < nameList.size(); nameListIndex++) {
			String namePart = nameList.get(nameListIndex);

			for (int blacklistNameIdx = 0; blacklistNameIdx < blacklistNameList.size(); blacklistNameIdx++) {
				String blacklistNamePart = blacklistNameList.get(blacklistNameIdx);

				for (int nameCharIndex = 0; nameCharIndex < namePart.length() && nameCharIndex < blacklistNamePart.length(); nameCharIndex++) {
					final char nameChar = namePart.charAt(nameCharIndex);
					final char blackListnameChar = blacklistNamePart.charAt(nameCharIndex);

					if (nameChar == blackListnameChar) {
						resultScore++;

						if (previousMatched) {
							resultScore += 2;
						}
						previousMatched = true;
						continue;
					}

					previousMatched = false;
				}
			}
		}

		return resultScore;
	}

	private int getBlacklistedNameMaximumScore(List<String> blacklistedName) {
		// this calculation with the allowed threshold should be reconsidered.
		// either a really long name or blacklist would always be returned because the calculation would be immense
		int maximumScore = 0;
		for (String namePart : blacklistedName) {
			maximumScore += namePart.length() * 3;
		}
		return maximumScore;
	}

	private long getResultScoreThreshold(List<String> blacklistedName) {
		int maximumScore = getBlacklistedNameMaximumScore(blacklistedName);
		return Math.round(maximumScore * 0.95);
	}

	public void setNamesFileFromDifferentLocation(boolean namesFileFromDifferentLocation) {
		this.namesFileFromDifferentLocation = namesFileFromDifferentLocation;
	}

	public void setNoisewordsFileFromDifferentLocation(boolean noisewordsFileFromDifferentLocation) {
		this.noisewordsFileFromDifferentLocation = noisewordsFileFromDifferentLocation;
	}

	public void setNameFilename(String nameFilename) {
		this.nameFilename = nameFilename;
	}

	public void setNoisewordsFilename(String noisewordsFilename) {
		this.noisewordsFilename = noisewordsFilename;
	}
}
