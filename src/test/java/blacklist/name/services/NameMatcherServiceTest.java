package blacklist.name.services;

import blacklist.name.MainProgram;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NameMatcherServiceTest {

    private NameMatcherService nameMatcherService;

    @Before
    public void setUp() {
        nameMatcherService = new NameMatcherService(MainProgram.NAMES_FILENAME, MainProgram.NOISE_WORDS_FILENAME);
    }

    @Test
    public void cleanNameOfNoiseWordsAndSpecialCharactersTest() {
        String cleanedName = nameMatcherService.cleanNameOfNoiseWordsAndSpecialCharacters("kristo. jyrgenson");
        Assert.assertEquals("kristo jyrgenson", cleanedName);
        cleanedName = nameMatcherService.cleanNameOfNoiseWordsAndSpecialCharacters("dr. jeffrey-joe jameson");
        Assert.assertEquals("dr jeffreyjoe jameson", cleanedName);
        cleanedName = nameMatcherService.cleanNameOfNoiseWordsAndSpecialCharacters("--..-0kris..//to");
        Assert.assertEquals("kristo", cleanedName);
        cleanedName = nameMatcherService.cleanNameOfNoiseWordsAndSpecialCharacters("");
        Assert.assertEquals("", cleanedName);
    }

    @Test
    public void getMatchingNamesTest() {
        String cleanedName = nameMatcherService.cleanNameOfNoiseWordsAndSpecialCharacters("kristo jyrgenson");
        List<String> blackListednames = nameMatcherService.getMatchingNames(createBlackListedNamesMap(), cleanedName);
    }

    private Map<String, List<String>> createBlackListedNamesMap() {
        String name = "kristo jyrgenson";
        Map<String, List<String>> blackListedNames = new HashMap<>();
        blackListedNames.put(name, Arrays.asList(name.split(NameMatcherService.SPACE).clone()));
        return blackListedNames;
    }

}
