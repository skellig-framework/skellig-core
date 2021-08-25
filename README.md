## What is Skellig Framework

Skellig is Automation/Performance Testing Framework with focus on writing tests with minimal coding. It has its own DSL which is designed to write various tests data and validation rules for execution result. The Skellig DSL provides essential built-in functions and data converters which help to work with test data, although providing enough flexibility to write your own plugins or specific logic on Kotlin/Java. 

## What Skellig Framework can do

* Send HTTP requests and validate response
* Sync/Async operations with TCP / AMQP / IBMMQ channels (ex. send, read, consume, respond) with output validation
* RMDB/NOSQL database operations (ex. select, insert, delete) with data validation
* Remote Unix commands with response validation

### Sample test

File bookings.sf
```feature
Name: Booking events

   Test: Book seats of the event
   Steps:
   Event Add event with available seats <available_seats>
   Book seats <seats> of the event
   Seats <seats> have been booked successfully for the event
   Data:
     |available_seats |seats
     |s1=10,s2=20     |s2   
```

File bookings.std
```java
name('Add event with available seats (.+)') {
    id = addEventTest
    protocol = rmq
    sendTo ['event.changed']
    properties { content_type = application/json }

    variables {
        eventCode = ${newEventCode:evt1_inc(event,5)}
    }

    message {
        json {
            code = ${eventCode}
            name = 'event 1'
            date = toDateTime(01-01-2020 10:30:00)
            location = somewhere
            pricePerSeats [ ${1} ]
            takenSeats [${takenSeats:}]
       }
    }
}

name('Book seats (.+) of the event\s*(.*)') {
    url = '/booking/request'
    http_method = POST
    http_headers{ Content-type = 'application/json'}

    variables {
        eventCode = '${2:${get(addEventTest).variables.eventCode}}'
     }

    payload {
        json {
            eventCode = ${eventCode}
            seats = listOf(${1})
        }
    }

    validate {
        statusCode = int(200)
        'body.toString()' {
               jsonPath(eventCode) = ${eventCode}
               jsonPath(success) = true
         }
    }
}

name('Seats (.+) have been booked successfully for the event') {
     servers [skellig-db]
     table = event
     command = select

     where {
        code = get(addEventTest).variables.eventCode
     }

    validate {
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
        context = SkelligDemoContext.class,
        config = "skellig-demo-local.conf")
public class SkelligDemoTestRunner {
}
```
For more information please refer [this guide](https://github.com/skellig-framework/skellig-core/wiki/Skellig-Quickstart-Guide)

Or for complete source code of the [demo project](https://github.com/skellig-framework/skellig-demo)
