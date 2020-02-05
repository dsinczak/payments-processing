# Payments processing assignment
Create compact RESTful web service for payments processing.

Please note that it's better to only implement half of requirements but do absolutely best you can.

# Requirements

## Payment creation
Client should be able to create payment of one of 3 types - TYPE1, TYPE2, TYPE3. Fields 'amount' (positive decimal), 'currency' (EUR or USD), 'debtor_iban' and 'creditor_iban' (texts) are mandatory for all types. 

Additional type-specific requirements:
1. TYPE1 is only applicable for EUR payments, has additional field 'details' (text) which is mandatory;
1. TYPE2 is only applicable for USD payments, has additional field ‘details’ (text) which is optional.
1. TYPE3 is applicable for payments in both EUR and USD currency, has additional field for creditor bank BIC code (text) which is mandatory.

## Payment cancelation
Client should be able to cancel the payment. It is possible to cancel payment only on the day of creation before 00:00. When cancel happens, cancellation fee should be calculated and saved along the payment in database.

Cancellation fee is calculated as: h * k

Where h - number of full hours (2:59 = 2h) payment is in system; k - coefficient (0.05 for TYPE1; 0.1 for TYPE2, 0.15 for TYPE3). Result is an amount in EUR.

## Payments querying
It should be possible to query all payments that aren't canceled as well as filter them by amount. Query should return payment IDs. 
There should also be an option to query specific payment by ID, and it should return payment ID and cancelation fee.

## Client country logging
Resolve clients country (use any external web service to resolve it by user IP) and write it to the log (that’s OK if it will fail sometimes). Information about clients country won't be required anywhere in business logic. 

## Notification
Imagine other (micro)service should be notified about the fact we saved valid TYPE1 or TYPE2 payment. The fact that external service was (un-)successfully notified should be saved to our database.

# What you'll get karma points for:
- Application efficiency - please be careful with what you make client wait for;
- Testability (proved with tests :) );
- Appropriate usage of design patterns - real-life services you're going to work on will be much bigger - sometimes it might be a good idea to get abstract;
- Writing “defensive” code - it's really important to be careful working with people's money;
- Clean code - imagine your code will be reviewed by Robert Martin;
- Ability to argue on various “why”s about framework/design/code/whatever decisions.

It would also be nice to be able to launch app and get all dependencies up and running in one command.

# Solution
## What is finished
1. Fully implemented payment functionality in Domain Driven Design approach. All business rules are implemented in:
    - [Payment](payments-processing-app/src/main/java/org/dsinczak/paymentsprocessing/domain/Payment.java) - which is domain aggregate root that encapsulates payment state with its behaviour
    - [PaymentFactory](payments-processing-app/src/main/java/org/dsinczak/paymentsprocessing/domain/PaymentFactory.java) - which is domain aggregate factory that encapsulates domain object creation invariants
    
   Access to domain object is guaranteed by repository ([PaymentRepository](payments-processing-app/src/main/java/org/dsinczak/paymentsprocessing/domain/PaymentRepository.java)) contract.
   
   Payment cancellation is implemented as business logic closure based on [CancellationFeePolicy](payments-processing-app/src/main/java/org/dsinczak/paymentsprocessing/domain/CancellationFeePolicy.java).
   Implementation requested in exercise is implemented in [ByHourCancellationFeePolicy](payments-processing-app/src/main/java/org/dsinczak/paymentsprocessing/domain/ByHourCancellationFeePolicy.java). Such behaviour
   is often a point of extension so according to open closed principle I made cancellation fee a policy (a.k.a. strategy pattern)
   
1. Fully implemented event sending mechanism for events publication. Application uses [EventPublisher](payments-processing-app/src/main/java/org/dsinczak/paymentsprocessing/notification/EventPublisher.java) abstraction.
   
   Provided implementation uses 2 phase mechanism for event publication:
   - First phase stores event in DB. This way we are sure that event was send from application service. And what is more important, sending did not involve any complex mechanism like 2-phase commit.
   - Second phase is based on scheduled function execution that periodically checks for events in db and sends them using configured conduit.
   
   TODOs:
   - Event sending conduit implementation. Current implementation just logs the fact that event was send
   - Event sending retries and circuit breaking. When conduit is not responding sending should be retried, When destination is down then we should follow circuit breaker pattern and stop trying for some time.
   - I dit not implement event receive confirmation. Depending on requirements detail the solution can be quite complex and amount of work required to do it right might be just too much for such short period of time.
   
1. Client IP geo-location is implemented in [ClientAuditInterceptor](payments-processing-app/src/main/java/org/dsinczak/paymentsprocessing/web/ClientAuditInterceptor.java). This is very simple implementation in a form 
of MVC endpoint interceptor. What is worth mentioning is that logging of client country tells us nothing if we cannot correlate such log with rest of logs. This is why i introduced 
[MdcLoggingInterceptor](payments-processing-app/src/main/java/org/dsinczak/paymentsprocessing/web/MdcLoggingInterceptor.java) that introduces correlation id to MDC.
      
1. Payment querying is partly implemented. I decided to follow CQRS approach where read model is separate from write model. With such simple business rules this might look like exaggeration but such approach 
pays off very fast when domain aggregates grow. There are many pros when considering CQRS and separation of querying from business logic is only one of them.     

    I did not implement one of the query methods. In order to do it right (amount filtering) I would need to spend more time.

1. REST endpoint for:
    - payment creation: POST http://localhost:8080/payment
    - payment cancellation: PUT http://localhost:8080/payment/{paymentid}/cancellation
    - finding payment id and cancellation id by payment id: GET: http://localhost:8080/payment/{paymentid}/cancellation
    
1. High testability of solution. Basically every class can be tested as single entity or in cooperation with other classes as components.
    Additionally whole application is by separate module with integration tests.
    
## Architecture
A tried to follow few approaches:
1. Domain Driven Design - by hiding payment business logic in domain (Payment and PaymentFactory). Of course this is very simple implementation but what is important is that all complexity and state
    manipulation is encapsulated inside domain objects. Use cases are realized in application service which is also responsible for technical aspect of transaction management.
1. CQRS - where domain is my write model and read model is separate entity. Beside taking load of write model to return proper data is also allows scalability. We can break those two stack apart and
    scale applications separately.
1. Hexagonal architecture ([more](https://medium.com/swlh/implementing-a-hexagonal-architecture-bcfbe0d63622)) - we could go further with cutting application into self containing parts. Whole *org.dsinczak.paymentsprocessing.domain* package is
    framework agnostic and basically can be passed to different tech stack. It defines ports in a form of interfaces.
1. Functional'ish programming - java is not very handy if it goes about functional programming but vavr library has few nice perks.

## How to run
1. From InteliJ: just run *org.dsinczak.paymentsprocessing.PaymentsApplication* class.
1. From console: in main project there is very simple and naive *run.sh* script that builds application and runs fat jar.   