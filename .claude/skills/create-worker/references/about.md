# What is a Job Worker?

A job worker is an **inbound adapter** in hexagonal architecture terms.
It lives at the boundary where the process engine reaches into your application:
Zeebe polls your worker for jobs and, when one is available, invokes the `handle` method.
It is much like the engine making an HTTP request to your code to perform some work.

The worker translates the Zeebe job (variables, job key) into a domain call on a use-case.
After that it completes the job and returns control to the engine.

This is the mirror of a process adapter:

| Direction    | Pattern         | Who initiates the call?                     |
|--------------|-----------------|---------------------------------------------|
| **Inbound**  | Job Worker      | The engine calls your code (or you poll it) |
| **Outbound** | Process Adapter | Your code calls the engine                  |
