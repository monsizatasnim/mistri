# mistri
# Local Service Booking Platform

A web-based platform that connects customers with verified local service providers and enables service providers to manage bookings, discover nearby partner shops, and purchase required materials through an integrated marketplace.

## Project Overview

The Local Service Booking Platform is designed to provide a reliable and transparent way for customers to discover, compare, and book local service providers such as electricians, plumbers, cleaners, AC technicians, and other skilled professionals.

The platform addresses common problems in local service discovery, including difficulty finding available providers, lack of transparent pricing, limited trust and verification, and inefficient communication between customers and service providers.

In addition to service booking, the platform introduces a **Partner Shop Marketplace**, allowing service providers to discover nearby shops and order required materials or equipment without leaving the platform.

The system is organized into three major portals:

- Customer Web Portal
- Service Provider Portal
- Admin & Partner Shop Management Portal

All three portals share a common business-rule core following the **Clean Architecture** approach.

## Objectives

The main objectives of the platform are to:

- Provide customers with reliable local service discovery.
- Allow customers to compare providers based on rating, distance, price, and availability.
- Enable customers to book specific service time slots.
- Provide secure and flexible payment options.
- Establish trust through provider verification and customer reviews.
- Help service providers manage bookings, pricing, profiles, and availability.
- Allow providers to discover nearby Partner Shops.
- Enable providers to purchase or rent required materials and equipment.
- Provide administrators with tools for verification, dispute resolution, reporting, and platform configuration.
- Provide Partner Shops with an online platform to manage products, inventory status, discounts, and orders.

## Key Features

### Customer Portal

- Customer registration and authentication
- Provider search by service category and location
- Multi-criteria provider ranking
- Provider filtering by:
  - Service type
  - Price range
  - Minimum rating
  - Verification status
- Provider profile and verification badge
- Distance-based provider discovery
- Time-slot booking
- Booking rescheduling and cancellation
- Multiple payment methods
- Payment history and receipt generation
- Provider rating and review system
- Booking-based review eligibility
- Dispute and complaint submission
- Evidence attachment for disputes
- Booking and dispute status tracking

> The provider ranking system considers rating, distance, price, and availability, with configurable weights managed by the administrator.

### Service Provider Portal

- Provider registration
- NID, trade license, and experience certificate submission
- Provider verification workflow
- Verification status tracking
- Service category management
- Flexible pricing models
- Availability management
- Booking request management
- Accept, decline, or propose alternative booking times
- Cancellation policy configuration
- Booking history
- Completed-job tracking
- Nearby Partner Shop discovery
- Shop filtering by category and online-order availability
- Distance and estimated travel time display
- Material and equipment ordering
- Sale or rental selection
- Urgent order requests
- Order status notifications
- Multiple Partner Shop payment options

> Providers can continue to appear in customer searches even before verification, but their current verification status is displayed clearly instead of showing a verified badge.

### Partner Shop Management

Partner Shops provide service providers with access to materials and equipment required during service jobs.

Features include:

- Partner Shop registration
- Admin approval workflow
- Shop profile management
- Shop category management
- Opening and closing hours
- Product catalog management
- Product price management
- Stock availability status
- Sale/rent availability
- Online order acceptance toggle
- Provider-specific discounts and offers
- Order acceptance
- Order modification
- Order rejection
- Urgent order handling
- Payment status management

> If a shop modifies an order, the provider must explicitly reconfirm the modified order before payment is captured.

### Admin Portal

Administrators are responsible for maintaining platform trust, security, and operational integrity.

Admin features include:

- Provider verification management
- Partner Shop approval
- Customer and provider account management
- Account suspension/reactivation
- Review moderation
- Dispute management
- Refund and dispute resolution
- Platform-wide configuration
- Provider ranking weight configuration
- Convenience-fee configuration
- Booking and platform reports
- Audit logging
- Verification and configuration activity tracking

> Admin functionality is strictly role-protected and cannot be accessed through regular customer or provider sessions.

## Provider Ranking System

One of the core features of the platform is a multi-criteria provider ranking system.

Providers are ranked using:

```
Score = w1 × Rating + w2 × (1 − Distance) + w3 × (1 − Price) + w4 × Availability
```

**Default weights:**

| Criteria     | Weight |
|--------------|--------|
| Rating       | 40%    |
| Distance     | 30%    |
| Price        | 20%    |
| Availability | 10%    |

Distance and price are normalized using min-max scaling, while rating is normalized from the 0–5 scale. Availability is determined based on whether the provider has an open slot within the customer's selected date range.

Administrators can modify these weights through platform configuration without changing the source code.

## System Architecture

The platform follows **Clean Architecture** with four major layers:

```
┌───────────────────────────────────────────────────┐
│              Frameworks & Drivers                 │
│  Spring Boot | MYSQL | HTML/CSS/JavaScript | APIs │
├───────────────────────────────────────────────────┤
│              Interface Adapters                   │
│      Controllers | Presenters | Repositories      │
├───────────────────────────────────────────────────┤
│                  Use Cases                        │
│   Booking | Ranking | Payment | Dispute           │
│         Shop Order | Verification | etc.          │
├───────────────────────────────────────────────────┤
│                   Entities                        │
│  Customer | Provider | Booking | Payment          │
│      Review | Dispute | Product | Order | Shop    │
└───────────────────────────────────────────────────┘

           Dependency Rule
                 ↓
     Dependencies point inward
```

The architecture separates core business rules from frameworks, databases, user interfaces, and external services. This makes the business logic easier to test, maintain, and modify independently of implementation details.

## Technology Stack

**Backend**
- Java
- Spring Boot

**Frontend**
- HTML
- CSS
- JavaScript

**Database**
- MYSQL

**External Services**
- bKash API
- Nagad API
- Rocket API

## Payment Methods

**Customer Payments**
- bKash
- Nagad
- Rocket
- Credit/Debit Card
- Cash on Completion

**Partner Shop Payments** (Service providers can pay Partner Shops using)
- Cash
- bKash
- Bank Transfer

## Main System Workflow

### Customer Workflow

```
Register / Login
      ↓
Search Service
      ↓
Compare Providers
      ↓
Select Provider
      ↓
Select Time Slot
      ↓
Confirm Booking
      ↓
Payment
      ↓
Booking Confirmed
      ↓
Service Completed
      ↓
Rate & Review
      ↓
Raise Dispute (if required)
```

### Service Provider Workflow

```
Register
      ↓
Submit Verification Documents
      ↓
Manage Profile & Pricing
      ↓
Receive Booking Request
      ↓
Accept / Decline / Propose New Time
      ↓
Complete Service
      ↓
Mark Booking Completed
```

**If materials are required:**

```
Active Job
      ↓
Find Nearby Partner Shop
      ↓
Browse Products
      ↓
Place Order
      ↓
Shop Accepts / Modifies / Declines
      ↓
Provider Reconfirms (if modified)
      ↓
Payment
      ↓
Pickup / Delivery
```

> The provider workflow and Partner Shop ordering process are defined as a dedicated part of the Service Provider Portal.

## Security & Privacy

The platform includes several security and privacy requirements:

- HTTPS/TLS communication
- Role-based access control
- CSRF protection
- Login rate limiting
- Encryption of sensitive data at rest
- Protected verification documents
- Secure payment processing
- Session management
- Input validation
- Database constraints
- Atomic transaction processing
- Audit logging
- Restricted access to NID, trade license, and experience certificates

> Sensitive provider verification documents are accessible only to the submitting provider and authorized administrators.

## Responsive & Accessible Design

The platform is designed as a responsive web application supporting:

- Desktop
- Tablet
- Mobile devices

Provider-facing and Partner Shop interfaces are designed with:

- Bengali language support
- Icon-first navigation
- Simplified interfaces
- Clear validation feedback
- Mobile-friendly layouts

> These requirements are specifically included to support users operating in job-site and low-connectivity environments.

## Project Structure

A recommended repository structure based on the defined Clean Architecture is:

```
local-service-booking-platform/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── ...
│   │   └── test/
│   │
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   └── scripts/
│   │
│   ├── styles/
│   └── Dockerfile
│
├── docs/
│   ├── requirements/
│   ├── architecture/
│   └── diagrams/
│
├── docker-compose.yml
├── .gitignore
└── README.md
```

## Core Domain Entities

The core business domain includes entities such as:

- Customer
- Service Provider
- Shop Owner
- Booking
- Payment
- Review
- Dispute
- Product
- Order

These entities belong to the innermost business-rule layer and remain independent of the database, web framework, and external services.

## Future Enhancements

Planned future improvements include:

- Native Android application
- Native iOS application
- Personalized provider ranking based on customer booking history
- Real-time customer-provider chat
- Bulk/wholesale material ordering
- Third-party delivery partner integration
- Real-time Partner Shop inventory synchronization
- Shop-side analytics dashboard

> These features are identified as future enhancements and are outside the current committed scope.

## Project Team

| Name                      | Role             |
|---------------------------|------------------|
| Mohammad Arafat Hossain   | Project Manager  |
| Monsiza Tasnim Orchid     | Team Lead        |
| Afroza Ahammed Akhi       | QA Lead          |
| Jaye Barai                | Reporting Lead   |

**Department of Computer Science and Engineering**
**University of Asia Pacific**

The project is developed as part of the **Software Engineering Lab (CSE 314)** course.

## Documentation

The project documentation includes:

- Software Requirements Specification (SRS)
- Functional Requirements
- Non-Functional Requirements
- Use Case Diagrams
- Activity Diagrams
- Swimlane Diagrams
- Sequence Diagrams
- Class Diagram
- State Diagrams
- Safety & Security Requirements
- System Architecture
- Technology and Infrastructure Specifications

## License

This project is developed for academic and educational purposes as part of the Software Engineering Lab course at the University of Asia Pacific.

## Acknowledgement

This project was developed as an academic software engineering project with a focus on requirements engineering, system architecture, clean design principles, and practical implementation of a multi-role service booking platform.
