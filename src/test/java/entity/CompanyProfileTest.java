package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CompanyProfileTest {

  private static final Stock APPLE = new Stock("AAPL", "Apple Inc.", "NASDAQ");

  @Test
  void constructor_validValues_keepsValues() {
    // Inputs
    Money marketCap = Money.of("5017916605986.00");

    // Action
    CompanyProfile profile = new CompanyProfile(APPLE, "Technology", marketCap);

    // Assertion
    assertEquals(APPLE, profile.stock());
    assertEquals("Technology", profile.industry());
    assertEquals(marketCap, profile.marketCap());
  }

  @Test
  void constructor_unknownIndustryAndMarketCap_keepsEmptyAndZero() {
    // Action
    CompanyProfile profile = new CompanyProfile(APPLE, "", Money.ZERO);

    // Assertion
    assertEquals("", profile.industry());
    assertEquals(Money.ZERO, profile.marketCap());
  }

  @Test
  void constructor_negativeMarketCap_throws() {
    // Inputs
    Money negative = Money.ofCents(-1);

    // Action / Assertion
    assertThrows(IllegalArgumentException.class,
        () -> new CompanyProfile(APPLE, "Technology", negative));
  }

  @Test
  void constructor_nullStock_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new CompanyProfile(null, "Technology", Money.ZERO));
  }

  @Test
  void constructor_nullIndustry_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new CompanyProfile(APPLE, null, Money.ZERO));
  }

  @Test
  void constructor_nullMarketCap_throws() {
    // Action / Assertion
    assertThrows(NullPointerException.class,
        () -> new CompanyProfile(APPLE, "Technology", null));
  }
}
