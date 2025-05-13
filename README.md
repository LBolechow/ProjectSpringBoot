# User Microservice Features:

## User Registration

### The user can register in the system by providing necessary data such as first name, last name, email address, phone number, and password.

### The password must meet specific security requirements (e.g., minimum 8 characters, uppercase letter, number, special character).

## Login

### The user can log in to the system using their email address or username and password.

### Error handling for invalid login attempts (e.g., incorrect password, unactivated account).

### Generate and send a JWT token upon successful login.

## Account Management

### The user can update their details, such as first name, last name, phone number, email address, or password.

### Support for password changes with current password verification.

## Password Reset

### The user can reset their password by providing their email address, which will receive a reset password link.

### The reset link should be valid for a specific time (e.g., 1 hour).

## Logout

### The user can log out of the system, invalidating the JWT token.

## Email Verification

### After registration, the user receives an email with an activation link, which must be clicked to activate the account.

### The activation link should be valid for a specific time (e.g., 24 hours).

## Account Deletion

### The user can permanently delete their account along with associated data.

## User Profile Overview

### The user can view their personal data (first name, last name, email, phone number) and activity history (e.g., order history, recent logins).

# Technical Assumptions:

### Database: H2 (dev), MySQL/PostgreSQL (prod)

### Validation: Input validation on the server side

### Security: JWT for user session management; passwords stored in a hashed format (e.g., bcrypt)

### API: REST (extendable to GraphQL)

### Integration: Kafka for communication with other microservices (e.g., notifications for registration, logins)

### Testing: JUnit, Mockito

# Acceptance Criteria:

### The user can register and log in, and data is stored correctly in the database.

### Login and registration operations handle JWT.

### The system sends email verification and password reset links correctly.

### The user can update their details after logging in.

### JWT tokens are invalidated upon logout.

### The microservice provides appropriate error codes and messages for failed operations.
