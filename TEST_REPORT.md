# Test Report

## How to run all tests with one command
1. Start MySQL (XAMPP), the backend, and the frontend (`npm run dev` inside `frontend`).
2. Set `JAVA_HOME` to JDK 21 and the environment variables (JWT_SECRET, DB_PASSWORD, and so on).
3. Run from the project root: `.\mvnw.cmd test`

Last full run (2026-10-26, JDK 21, Chrome 155): **143 tests run, 0 failures, 0 errors, BUILD SUCCESS**.

## Tests written for this story

| Test class | Type | What it covers | Tests |
|---|---|---|---|
| CustomerApiTest | API | customer registration (valid, duplicate, missing password), login, wrong password, token required, payment, payment history, receipt, zero amount rejected | 10 |
| ProviderApiTest | API | provider login, wrong password, verification status, price update, cancellation policy limits, admin endpoint blocked for provider | 6 |
| BookingApiTest | API | login required, create booking, list bookings, cancel with full refund, reschedule, invalid start | 5 |
| RankingFormulaTest | Unit | rating, price, area and weight handling in the ranking formula, neutral rating for no reviews, tie-break by provider id | 6 |
| RefundRulesTest | Unit | full refund before provider response, no full refund after, completed booking cannot be cancelled, start and complete flow | 9 |
| ProviderPriceSeleniumTest | UI (Selenium) | provider login, set price range, validation error | 2 |
| ProviderOrderPaymentSeleniumTest | UI (Selenium) | provider pays for a shop order (creates a fresh order each run) | 1 |
| ShopOwnerPortalSeleniumTest | UI (Selenium) | approval status, profile edit, add product, language switch | 4 |

The team's existing unit tests (notifications, admin reports, admin logs, shop orders, shop discovery and others) run with the same command.

## Acceptance criteria check

- API tests cover registration, login, booking and payment: done.
- Unit tests cover the ranking formula and refund rules: done.
- Selenium UI tests cover login, price setting, shop order payment and shop owner screens. Search-and-book is a follow-up.
- All tests run with one command (`.\mvnw.cmd test`): done.
- Results documented: this file.

## Known limitations
- API and UI tests need MySQL, the backend, the frontend and seeded users (cloud@example.com, cust@example.com, selshop@example.com).
- ProviderRankingService sets the booking time to null, so the availability part of the score is always 1.0 (reported to the code owner).
- Chrome "CDP version" warnings in the Selenium logs are harmless.