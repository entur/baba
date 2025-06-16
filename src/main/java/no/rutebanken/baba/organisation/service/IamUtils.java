package no.rutebanken.baba.organisation.service;

import java.util.Arrays;
import java.util.List;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

public final class IamUtils {

  private IamUtils() {}

  static String generatePassword() {
    List<CharacterRule> rules = Arrays.asList(
      // at least one upper-case character
      new CharacterRule(EnglishCharacterData.UpperCase, 1),
      // at least one lower-case character
      new CharacterRule(EnglishCharacterData.LowerCase, 1),
      // at least one digit character
      new CharacterRule(EnglishCharacterData.Digit, 1)
    );
    return new PasswordGenerator().generatePassword(12, rules);
  }
}
