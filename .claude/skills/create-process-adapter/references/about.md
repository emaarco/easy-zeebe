# What is a Process Adapter?

A process adapter is an **outbound adapter** in hexagonal architecture terms.
It lives at the boundary where *your application* reaches out to the process engine:
your domain or application service calls the adapter to initiate or advance a process instance —
start a process, send a correlation message, signal a boundary event, or query state.

This is the mirror of a job worker:

| Direction    | Pattern         | Who initiates the call?                     |
|--------------|-----------------|---------------------------------------------|
| **Inbound**  | Job Worker      | The engine calls your code (or you poll it) |
| **Outbound** | Process Adapter | Your code calls the engine                  |

The process adapter collects all outgoing engine operations for one process in a single class,
so the rest of your codebase depends only on a clean port interface — never on the Camunda SDK directly.
