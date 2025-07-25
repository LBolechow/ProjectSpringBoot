User Microservice – Key Features

    Registration & Login:
    User registration with email verification and secure password. Login with JWT and proper error handling (e.g., inactive account, wrong password).

    Account Management:
    Update personal data, change password (with current password check), view profile and activity history.

    Password Reset:
    Reset password via email link (valid for a limited time).
    
    Logout & Account Deletion:
    Logout invalidates JWT. Users can permanently delete their account and data.

    Message Queue Integration (RabbitMQ):
    Acts as a message producer and queue manager. It publishes events (e.g., registration, login, password reset requests, account deletion) to RabbitMQ, enabling communication with other microservices. Consumer services (e.g., mailer, logging) react to these events asynchronously, ensuring loose coupling        and scalability.   

Password Reset Notification Service (Python, RabbitMQ)

    A Python-based mailer service that listens to a RabbitMQ queue and sends password reset emails to users. When a password reset event is published by the User Microservice, the mailer consumes the message, generates a secure reset link, and delivers it to the user's registered email address.

Event Logging Service (Python, Redis, RabbitMQ)

    A Python-based microservice that acts as a RabbitMQ consumer, responsible for capturing and storing system events in Redis. It listens to various event queues (e.g., user registration, login, password reset) and logs structured event.

Frontend is made in Angular.
