# The "Periodicals" Scala web project
## Web UI screenshots
https://github.com/stolser/periodicals_scala/wiki/Screens

## Used technologies:
* Scala 2.12.7, Akka 2.5.18, Enumeratum
* ScalaTest (FunSuite, FlatSpec, WordSpec styles), <br/> Mockito Scala, ScalaMock
* sbt
* Java EE 7: Servlets, JSP, JSTL (with custom tags)
* JDBC, MySQL
* Bootstrap, jQuery

## Functionality description
Application users have roles: ADMIN and/or SUBSCRIBER.

An **Admin** manages a catalog of periodicals (CRUD operations). It cannot subscribe to periodicals. For that a user must have the SUBSCRIBER role.

A **Subscriber** can look through a list of periodicals and check detailed info about specific ones. He can subscribe to an interesting periodical by specifying a number of months for a future subscription and creating a new invoice.
To activate a subscription the user must pay a newly created invoice on the 'My account' page.

A periodical can have one of 3 statuses: ACTIVE, INACTIVE, and DISCARDED. A Subscriber can see and subscribe only to ACTIVE periodicals.
An Admin can see all the periodicals in the system and change their statuses.

**_Periodicals are removed from the DB in a two-stage process (the Recycling Bin functionality)_**. First their status must be changed to DISCARDED and only after that, 
they can be removed permanently from the DB as a separate operation.

## Architecture, design and implementation details
The overall application has a 3-tier architecture:
* **Presentation Tier** - is implemented using JSP, JSP Standard Tag Library, Bootstrap CSS framework and jQuery.
* **Application Tier** - contains the business logic separated into more three layers: DAOs - Services - Controllers.   
* **Data Tier** - a MySQL database. 

### The DAO layer
The DAO layer comprises classes and traits implementing the **Data Access Object** pattern. 
This layer is the closest to the DB and isolates actual business logic from the persistent layer which can be easily changed to another one.

### The Service layer
This layer is located between the DAO and Controller layers and has the following main purposes:
* to isolate the Controller layer from a low-level DAO API, in order to simplify the logic of the request processor classes;
* to provide the support for transactions spanning over multiple operations on several different DAOs 
(for example, InvoiceServiceImpl#payInvoice(), UserServiceImpl#createNewUser()).

### The Controller layer
This layer is based on the **Front Controller** pattern. So, all the requests coming for the application backend resources (under /backend/*) 
are processed by a single class, FrontController (specified in 'web.xml'), and then dispatched to the appropriate processor for that type of request.

This logic is implemented by applying the Command design pattern.

**AuthenticationFilter** makes sure that requests come from a **signed-in** and **active user** and the session has not expired. 
Otherwise it redirects to the Sing-in page.

**AuthorizationFilter** makes sure that a current user (specified in the session) has enough permissions (based on user roles) 
to get a requested resource or perform an operation.

## Package Summary
| Package                                  | Description                                                                                                                                                                                                     |
|------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| com.ostoliarov.webproject.**connection** | Contains representation of a connection pool and an abstract connection that wraps a real DB connection. 
|                                          | Trait AbstractConnection allows managing transactions spanning operations over several different DAOs. |
| com.ostoliarov.webproject.**controller** | Contains the Front Controller class, form validation logic, authentication and authorization filters, and classes with request processing logic. 
|                                          | Represents the Controller layer.                               |
| com.ostoliarov.webproject.**dao**        | Contains traits and classes that implements the Data Access Object pattern. 
|                                          | Represents the DAO layer.                                                                                                           |
| com.ostoliarov.webproject.**model**      | Contains case classes representing entities in the system.                                                                                                                                                      |
| com.ostoliarov.webproject.**service**    | Contains traits and classes representing the Service layer.                                                                                                                                                     |
| com.ostoliarov.webproject.**view**       | Contains JSP view resolver logic, local classes and custom JSP tags.   