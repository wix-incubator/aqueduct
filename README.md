# Aqueduct 
Aqueduct is durable HTTP client designed for efficient server to server communication. Its asynchronous persistent 
HttpTaskQueue API can be used for reliable notifications and messages between applications and services. Each queued task
is stored transactionally first. All the network communication work is performed in background by http client built 
on [Netty NIO framework](http://netty.io/). Aqueduct also provides Synchronous API