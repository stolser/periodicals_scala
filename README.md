# The "Periodicals" Scala web project
Users have roles: ADMIN and/or SUBSCRIBER.

An Admin manages a catalog of periodicals (CRUD operations). It cannot subscribe to periodicals. For that a user must have the SUBSCRIBER role.

A Subscriber can look through a list of periodicals and check detailed info about specific ones. He can subscribe to an interesting periodical by specifying a number of months for a future subscription and creating a new invoice.
To activate a subscription the user must pay a newly created invoice on the 'My account' page.

Periodical have 3 statuses: ACTIVE, INACTIVE, and DISCARDED. A Subscriber can see and subscribe only to ACTIVE periodicals.
An Admin can see all the periodicals in the system and change their statuses.

Periodicals are removed in a two-stage process (the Recycling Bin functionality). First their status must be changed to DISCARDED and only after that, 
they can be removed from the database as a separate operation.

## Used technologies:
* Scala 2.12.7, Enumeratum
* ScalaTest (FunSuite style), Mockito Scala
* sbt
* Java EE 7: Servlets, JSP, JSTL
* JDBC, MySQL
* Bootstrap, jQuery
