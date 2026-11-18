package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class OrderTest {

  // acceptOrder

  @Test
  void acceptOrder_completeMarketOrder_isOpen() {
    // Inputs
    Order order = pendingOrder();

    // Action
    order.acceptOrder();

    // Assertion
    assertEquals(OrderStatus.OPEN, order.getStatus());
  }

  @Test
  void acceptOrder_completeLimitOrder_isOpen() {
    // Inputs
    Order order = pendingOrder();
    order.setOrderType(OrderType.LIMIT);
    order.setLimitPrice(Money.of("150.00"));

    // Action
    order.acceptOrder();

    // Assertion
    assertEquals(OrderStatus.OPEN, order.getStatus());
  }

  @Test
  void acceptOrder_missingSide_throws() {
    // Inputs
    Order order = new Order("order-1", "AAPL");
    order.setAmount(10);
    order.setOrderType(OrderType.MARKET);

    // Action and assertion
    assertReason(OrderException.Reason.MISSING_SIDE, order::acceptOrder);
    assertEquals(OrderStatus.PENDING, order.getStatus());
  }

  @Test
  void acceptOrder_missingAmount_throws() {
    // Inputs
    Order order = new Order("order-1", "AAPL");
    order.setSide(OrderSide.BUY);
    order.setOrderType(OrderType.MARKET);

    // Action and assertion
    assertReason(OrderException.Reason.INVALID_QUANTITY, order::acceptOrder);
    assertEquals(OrderStatus.PENDING, order.getStatus());
  }

  @Test
  void acceptOrder_missingType_throws() {
    // Inputs
    Order order = new Order("order-1", "AAPL");
    order.setSide(OrderSide.BUY);
    order.setAmount(10);

    // Action and assertion
    assertReason(OrderException.Reason.MISSING_ORDER_TYPE, order::acceptOrder);
    assertEquals(OrderStatus.PENDING, order.getStatus());
  }

  @Test
  void acceptOrder_limitOrderWithoutPrice_throws() {
    // Inputs
    Order order = pendingOrder();
    order.setOrderType(OrderType.LIMIT);

    // Action and assertion
    assertReason(OrderException.Reason.MISSING_LIMIT_PRICE, order::acceptOrder);
    assertEquals(OrderStatus.PENDING, order.getStatus());
  }

  @Test
  void acceptOrder_alreadyOpen_throws() {
    // Inputs
    Order order = openOrder();

    // Action and assertion
    assertReason(OrderException.Reason.ALREADY_OPEN, order::acceptOrder);
  }

  @Test
  void acceptOrder_canceled_throws() {
    // Inputs
    Order order = pendingOrder();
    order.cancelOrder();

    // Action and assertion
    assertReason(OrderException.Reason.ALREADY_CANCELED, order::acceptOrder);
  }

  // rejectOrder

  @Test
  void rejectOrder_pending_isRejected() {
    // Inputs
    Order order = pendingOrder();

    // Action
    order.rejectOrder();

    // Assertion
    assertEquals(OrderStatus.REJECTED, order.getStatus());
  }

  @Test
  void rejectOrder_open_throws() {
    // Inputs
    Order order = openOrder();

    // Action and assertion
    assertReason(OrderException.Reason.ALREADY_OPEN, order::rejectOrder);
    assertEquals(OrderStatus.OPEN, order.getStatus());
  }

  @Test
  void rejectOrder_alreadyRejected_throws() {
    // Inputs
    Order order = pendingOrder();
    order.rejectOrder();

    // Action and assertion
    assertReason(OrderException.Reason.ALREADY_REJECTED, order::rejectOrder);
  }

  // fillOrder

  @Test
  void fillOrder_open_isFilled() {
    // Inputs
    Order order = openOrder();

    // Action
    order.fillOrder();

    // Assertion
    assertEquals(OrderStatus.FILLED, order.getStatus());
  }

  @Test
  void fillOrder_pending_throws() {
    // Inputs
    Order order = pendingOrder();

    // Action and assertion
    assertReason(OrderException.Reason.NOT_OPEN, order::fillOrder);
    assertEquals(OrderStatus.PENDING, order.getStatus());
  }

  @Test
  void fillOrder_alreadyFilled_throws() {
    // Inputs
    Order order = openOrder();
    order.fillOrder();

    // Action and assertion
    assertReason(OrderException.Reason.ALREADY_FILLED, order::fillOrder);
  }

  @Test
  void fillOrder_canceled_throws() {
    // Inputs
    Order order = openOrder();
    order.cancelOrder();

    // Action and assertion
    assertReason(OrderException.Reason.ALREADY_CANCELED, order::fillOrder);
  }

  // cancelOrder

  @Test
  void cancelOrder_pending_isCanceled() {
    // Inputs
    Order order = pendingOrder();

    // Action
    order.cancelOrder();

    // Assertion
    assertEquals(OrderStatus.CANCELED, order.getStatus());
  }

  @Test
  void cancelOrder_open_isCanceled() {
    // Inputs
    Order order = openOrder();

    // Action
    order.cancelOrder();

    // Assertion
    assertEquals(OrderStatus.CANCELED, order.getStatus());
  }

  @Test
  void cancelOrder_filled_throws() {
    // Inputs
    Order order = openOrder();
    order.fillOrder();

    // Action and assertion
    assertReason(OrderException.Reason.ALREADY_FILLED, order::cancelOrder);
    assertEquals(OrderStatus.FILLED, order.getStatus());
  }

  @Test
  void cancelOrder_alreadyCanceled_throws() {
    // Inputs
    Order order = pendingOrder();
    order.cancelOrder();

    // Action and assertion
    assertReason(OrderException.Reason.ALREADY_CANCELED, order::cancelOrder);
  }

  @Test
  void cancelOrder_rejected_throws() {
    // Inputs
    Order order = pendingOrder();
    order.rejectOrder();

    // Action and assertion
    assertReason(OrderException.Reason.ALREADY_REJECTED, order::cancelOrder);
  }

  // setSide

  @Test
  void setSide_pending_setsSide() {
    // Inputs
    Order order = pendingOrder();

    // Action
    boolean result = order.setSide(OrderSide.SELL);

    // Assertion
    assertTrue(result);
    assertEquals(OrderSide.SELL, order.getSide());
  }

  @Test
  void setSide_open_leavesSideUnchanged() {
    // Inputs
    Order order = openOrder();

    // Action
    boolean result = order.setSide(OrderSide.SELL);

    // Assertion
    assertFalse(result);
    assertEquals(OrderSide.BUY, order.getSide());
  }

  // setAmount

  @Test
  void setAmount_pending_setsAmount() {
    // Inputs
    Order order = pendingOrder();

    // Action
    boolean result = order.setAmount(25);

    // Assertion
    assertTrue(result);
    assertEquals(25, order.getAmount());
  }

  @Test
  void setAmount_zero_throws() {
    // Inputs
    Order order = pendingOrder();

    // Action and assertion
    assertReason(OrderException.Reason.INVALID_QUANTITY, () -> order.setAmount(0));
    assertEquals(10, order.getAmount());
  }

  @Test
  void setAmount_negative_throws() {
    // Inputs
    Order order = pendingOrder();

    // Action and assertion
    assertReason(OrderException.Reason.INVALID_QUANTITY, () -> order.setAmount(-5));
    assertEquals(10, order.getAmount());
  }

  @Test
  void setAmount_open_leavesAmountUnchanged() {
    // Inputs
    Order order = openOrder();

    // Action
    boolean result = order.setAmount(25);

    // Assertion
    assertFalse(result);
    assertEquals(10, order.getAmount());
  }

  @Test
  void setAmount_openWithInvalidAmount_returnsFalse() {
    // Inputs
    Order order = openOrder();

    // Action
    boolean result = order.setAmount(0);

    // Assertion
    assertFalse(result);
    assertEquals(10, order.getAmount());
  }

  // setOrderType

  @Test
  void setOrderType_pending_setsType() {
    // Inputs
    Order order = pendingOrder();

    // Action
    boolean result = order.setOrderType(OrderType.LIMIT);

    // Assertion
    assertTrue(result);
    assertEquals(OrderType.LIMIT, order.getType());
  }

  @Test
  void setOrderType_open_leavesTypeUnchanged() {
    // Inputs
    Order order = openOrder();

    // Action
    boolean result = order.setOrderType(OrderType.LIMIT);

    // Assertion
    assertFalse(result);
    assertEquals(OrderType.MARKET, order.getType());
  }

  // setLimitPrice

  @Test
  void setLimitPrice_pendingLimitOrder_setsPrice() {
    // Inputs
    Order order = pendingOrder();
    order.setOrderType(OrderType.LIMIT);

    // Action
    order.setLimitPrice(Money.of("150.00"));

    // Assertion
    assertEquals(Money.of("150.00"), order.getLimitPrice());
  }

  @Test
  void setLimitPrice_zero_throws() {
    // Inputs
    Order order = pendingOrder();
    order.setOrderType(OrderType.LIMIT);
    order.setLimitPrice(Money.of("150.00"));

    // Action and assertion
    assertReason(OrderException.Reason.INVALID_LIMIT_PRICE, () -> order.setLimitPrice(Money.ZERO));
    assertEquals(Money.of("150.00"), order.getLimitPrice());
  }

  @Test
  void setLimitPrice_negative_throws() {
    // Inputs
    Order order = pendingOrder();
    order.setOrderType(OrderType.LIMIT);
    order.setLimitPrice(Money.of("150.00"));

    // Action and assertion
    assertReason(OrderException.Reason.INVALID_LIMIT_PRICE,
        () -> order.setLimitPrice(Money.of("-5.00")));
    assertEquals(Money.of("150.00"), order.getLimitPrice());
  }

  @Test
  void setLimitPrice_null_throws() {
    // Inputs
    Order order = pendingOrder();
    order.setOrderType(OrderType.LIMIT);
    order.setLimitPrice(Money.of("150.00"));

    // Action and assertion
    assertReason(OrderException.Reason.INVALID_LIMIT_PRICE, () -> order.setLimitPrice(null));
    assertEquals(Money.of("150.00"), order.getLimitPrice());
  }

  @Test
  void setLimitPrice_missingType_throws() {
    // Inputs
    Order order = new Order("order-1", "AAPL");

    // Action and assertion
    assertReason(OrderException.Reason.MISSING_ORDER_TYPE,
        () -> order.setLimitPrice(Money.of("150.00")));
    assertNull(order.getLimitPrice());
  }

  @Test
  void setLimitPrice_marketOrder_throws() {
    // Inputs
    Order order = pendingOrder();

    // Action and assertion
    assertReason(OrderException.Reason.LIMIT_PRICE_NOT_ALLOWED,
        () -> order.setLimitPrice(Money.of("150.00")));
    assertNull(order.getLimitPrice());
  }

  @Test
  void setLimitPrice_openLimitOrder_leavesPriceUnchanged() {
    // Inputs
    Order order = pendingOrder();
    order.setOrderType(OrderType.LIMIT);
    order.setLimitPrice(Money.of("150.00"));
    order.acceptOrder();

    // Action
    order.setLimitPrice(Money.of("200.00"));

    // Assertion
    assertEquals(Money.of("150.00"), order.getLimitPrice());
  }

  // Helpers

  /** A complete pending market order that is ready to be accepted. */
  private static Order pendingOrder() {
    Order order = new Order("order-1", "AAPL");
    order.setSide(OrderSide.BUY);
    order.setAmount(10);
    order.setOrderType(OrderType.MARKET);
    return order;
  }

  /** A complete order that has already been accepted. */
  private static Order openOrder() {
    Order order = pendingOrder();
    order.acceptOrder();
    return order;
  }

  private static void assertReason(OrderException.Reason expected, Executable action) {
    OrderException e = assertThrows(OrderException.class, action);
    assertEquals(expected, e.getReason());
  }
}
