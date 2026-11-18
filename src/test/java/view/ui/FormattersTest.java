package view.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests for {@link Formatters}. */
class FormattersTest {

  @Test
  void money_positiveCents_formatsWithCommasAndTwoDecimals() {
    assertEquals("$1,234.56", Formatters.money(123_456));
  }

  @Test
  void money_zero_formatsAsZeroDollars() {
    assertEquals("$0.00", Formatters.money(0));
  }

  @Test
  void money_negativeCents_usesTrueMinus() {
    assertEquals(Formatters.MINUS + "$50.00", Formatters.money(-5_000));
  }

  @Test
  void signedMoney_positiveCents_showsPlus() {
    assertEquals("+$200.00", Formatters.signedMoney(20_000));
  }

  @Test
  void signedMoney_negativeCents_showsTrueMinus() {
    assertEquals(Formatters.MINUS + "$50.00", Formatters.signedMoney(-5_000));
  }

  @Test
  void signedMoney_zero_showsNoSign() {
    assertEquals("$0.00", Formatters.signedMoney(0));
  }

  @Test
  void percent_positiveFraction_showsPlus() {
    assertEquals("+1.25%", Formatters.percent(0.0125));
  }

  @Test
  void percent_negativeFraction_showsTrueMinus() {
    assertEquals(Formatters.MINUS + "0.50%", Formatters.percent(-0.005));
  }

  @Test
  void percent_zero_showsNoSign() {
    assertEquals("0.00%", Formatters.percent(0));
  }
}