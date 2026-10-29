# Automated Test Results

Branch: feature/automated-tests. Last full run: 29 Oct 2026.

## How to run

| Command | What runs | Needs |
|---|---|---|
| `.\mvnw.cmd test` | 115 tests (unit tests and the Spring context test) | Nothing. Uses an in-memory H2 database, so MySQL does not have to be running. |
| `.\mvnw.cmd test -Pintegration` | All 147 tests, including API and Selenium tests | MySQL, backend on :8080, frontend on :5173, Chrome, and the test accounts listed below |

Run one class: `.\mvnw.cmd test -Pintegration "-Dtest=BookingApiTest"`

## Acceptance criteria coverage

| Criterion | Tests | Count | Result |
|---|---|---|---|
| API: registration, login, payment | CustomerApiTest | 10 | Pass |
| API: booking | BookingApiTest | 5 | Pass |
| API: provider login, price, cancellation policy, admin access | ProviderApiTest | 6 | Not run (needs provider account) |
| Unit: ranking formula | RankingFormulaTest | 6 | Pass |
| Unit: refund rules | RefundRulesTest | 9 | Pass |
| Selenium: login, search and book, shop orders | MainFlowsSeleniumTest | 4 | Pass |
| Selenium: provider price, provider order payment | ProviderPriceSeleniumTest (2), ProviderOrderPaymentSeleniumTest (1) | 3 | Not run (needs provider account) |
| Selenium: shop owner portal | ShopOwnerPortalSeleniumTest | 4 | Not run (needs approved shop owner) |
| Other unit tests (notifications, reports, shop discovery, shop orders, admin actions) | 12 classes | 84 | Pass |
| Spring context loads | PlatformApplicationTests | 1 | Pass |

Totals: 147 tests. 134 verified passing. 13 not run because of missing accounts.

## Prerequisites for the tests that were not run

| Test class | Needs |
|---|---|
| BookingApiTest | Customer cust@example.com / secret123 |
| ProviderApiTest, ProviderPriceSeleniumTest, ProviderOrderPaymentSeleniumTest | Provider cloud@example.com / secret123 |
| ProviderOrderPaymentSeleniumTest | Also connects to MySQL as root and inserts an order. The password comes from the DB_PASSWORD environment variable. |
| ShopOwnerPortalSeleniumTest | Shop owner selshop@example.com / secret123, approved by an admin |
| MainFlowsSeleniumTest | Customer 01711111111 / test1234 and shop owner shop1@test.com / shop1234 |

Notes:
- API and Selenium tests write real data (customers, bookings, payments) into the database the backend uses.
- Selenium tests are excluded from the default run because they need a running backend, frontend and Chrome.

## Known issues found while writing tests

1. ProviderRankingService.collectFacts() sets `LocalDateTime time = null`, so upcoming bookings are always 0 and the availability score is always 1.0. The ranking tests cover rating, closeness, price and weights, not availability.
2. BookingController start, complete, cancel and reschedule have no ownership check, and the booking API takes customerId from the request body instead of the token.
3. ShopOrderService.placeOrder does not enforce acceptingOnlineOrders and providerDiscountPercent.