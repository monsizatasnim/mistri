# Test Report

## How to run
1. Start MySQL (XAMPP), the backend, and the frontend (`npm run dev` in `frontend`).
2. Set JAVA_HOME to JDK 21 and the environment variables (JWT_SECRET, DB_PASSWORD, ADMIN_REGISTRATION_KEY).
3. Run all tests from IntelliJ (right-click the test folder, Run) or `.\mvnw.cmd test` for the unit tests.

## Tests written by QA
| Test class | Type | Covers |
|---|---|---|
| CustomerApiTest | API | registration, duplicate/invalid registration, login, wrong password, token required, payment, history, receipt |
| ProviderApiTest | API | provider login, verification status, price update, cancellation policy limits, admin endpoint blocked |
| BookingApiTest | API | create booking, list, cancel with full refund, reschedule, invalid start |
| RankingFormulaTest | Unit | rating, price, area, weights, tie-break |
| RefundRulesTest | Unit | refund before/after provider response, status flow |
| ProviderPriceSeleniumTest | UI | provider login, set price range, validation |
| ProviderOrderPaymentSeleniumTest | UI | provider pays a shop order |
| ShopOwnerPortalSeleniumTest | UI | approval status, profile edit, add product, language switch |
| MainFlowsSeleniumTest | UI | customer login, wrong password, search and book, shop owner orders |

## Evidence
Screenshots of the latest runs are in `docs/test-results/`.

## Known limitations
- UI and API tests need the local database to contain seeded accounts (customer, approved shop owner, provider).
- CustomerBookingApiTest needs its own seeded customer and provider; without them its setup login fails and its tests are skipped.
- ProviderRankingService uses a null booking time, so the availability score is always 1.0.
- Chrome "CDP version" warnings are harmless.