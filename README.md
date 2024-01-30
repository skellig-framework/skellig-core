## What is Skellig Framework

[![Build Status](https://github.com/skellig-framework/skellig-core/actions/workflows/main.yml/badge.svg)](https://github.com/skellig-framework/skellig-core/actions)
[![Release](https://img.shields.io/maven-central/v/org.skelligframework/skellig-junit-runner?color=%2319afaf)](https://repo1.maven.org/maven2/org/skelligframework/)
[![License:MIT](https://img.shields.io/badge/license-Apache%202-yellow)](http://www.apache.org/licenses/)
[![Documentation](https://img.shields.io/badge/development-wiki-yellowgreen.svg)](https://github.com/skellig-framework/skellig-core/wiki)
[![Coverage Status](https://codecov.io/gh/skellig-framework/skellig-core/branch/master/graph/badge.svg?token=KSM07J2MJD)](https://codecov.io/gh/skellig-framework/skellig-core)


Skellig is Automation/Performance Testing Framework with focus on writing tests with minimal coding. It has its own DSL which is designed to write various tests data and validation rules for execution result. The Skellig DSL provides essential built-in functions and data converters which help to work with test data, although providing enough flexibility to write your own plugins or specific logic on Kotlin/Java. 

## What Skellig Framework can do

* Send HTTP requests and validate response
* Sync/Async operations with TCP / AMQP / IBMMQ channels (ex. send, read, consume, respond) with output validation
* RMDB/NOSQL database operations (ex. select, insert, delete) with data validation
* Remote Unix commands with response validation
* Performance testing with build-in or Prometheus metrics

### Sample test
A simple and quick test which demonstrates how to write a test using Skellig Framework. This test books a ticket by
sending a POST request to a web service with relevant information and verifies the response. It also checks if 
the database has a valid record and before running the test, it adds an event to book by placing it into the RMQ channel.

File bookings.skellig
```yml
Feature: Booking events

   Scenario: Book seats of the event
   * Add event with available seats <available_seats>
       |newEventCode |<eventCode> |
   * Book seats <seats> of the event
   * Seats <seats> have been booked successfully for the event
  
   Examples:
     |eventCode|available_seats |seats |
     |e0001    |s1=10,s2=20     |s2    |
```

File bookings.sts
```java
name("Add event with available seats (.+)") {
    id = addEventTest
    protocol = rmq
    sendTo ["event.changed"]
    properties { content_type = application/json }

    values {
        // if parameter 'newEventCode' not provided, 
        // then use the standard function inc(event,5) to increment 5 digits every run for key 'event' 
        // and attach it to 'evt1_'
        eventCode = ${newEventCode,evt1_inc(event,5)}
    }

    message {
        // convert data to json
        json {
            code = ${eventCode}
            name = "event 1"
            // use standard function toDateTime(...) which returns LocalDateTime object
            date = toDateTime("01-01-2020 10:30:00") 
            location = somewhere
            pricePerSeats [ ${1} ]  // set data from captured first parameter taken from test name
            takenSeats [${takenSeats,}]
       }
    }
}

name("Book seats (.+) of the event\s*(.*)") {
    url = "/booking/request"
    method = POST
    headers { Content-type = "application/json"}

    values {
        // set second parameter captured from the test name to 'eventCode' var
        // otherwise get 'eventCode' from test with id 'addEventTest'
        eventCode = ${2,${get(addEventTest).values.eventCode}}
     }

    payload {
        json {
            eventCode = ${eventCode}  // get 'eventCode' from the values
            seats = listOf(${1})  // just another way of setting list, instead of [...]
        }
    }

    validate {
        statusCode = "200".toInt()  // convert to int
        // extract body from the response and convert to string, then validate some fields.
        // Because it's json, we extract these fields by jsonPath'
        body.toString() {
               jsonPath(eventCode) = ${eventCode}
               jsonPath(success) = true
         }
    }
}

name("Seats (.+) have been booked successfully for the event") {
     servers [skellig-db]
     table = event
     command = select

     where {
        code = get(addEventTest).values.eventCode
     }

    validate {
         // as the result is a list of rows, we take the first row and validate just one column from it 
         skellig-db.fromIndex(0).taken_seats = contains(${1})
    }
}
```

SkelligDemoTestRunner class
```java
@RunWith(SkelligRunner.class)
@SkelligOptions(
        features = {"tests/"},
        testSteps = {"tests", "org.skellig.demo"},
        config = "skellig-demo-local.conf")
public class SkelligDemoTestRunner {
}
```
For more information please refer [this guide](https://github.com/skellig-framework/skellig-core/wiki/Skellig-Quickstart-Guide)

Or for complete source code of the [demo project](https://github.com/skellig-framework/skellig-demo) with the latest updates

IntelliJ plugin [page](https://plugins.jetbrains.com/plugin/20299-skellig-framework)
