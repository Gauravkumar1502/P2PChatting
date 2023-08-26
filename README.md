# P2PChatting

## Server1:
* Create a server class that listens for incoming client connections.
* Maintain a list of connected clients and their associated output streams.
* Use multithreading to handle multiple clients concurrently.
* Implement a load balancer logic if required.


## Client:
* Create a client class that connects to the server.
* Prompt the user for their username upon connecting to the server.
* Keep track of the client's own output stream to send messages.


## Communication:
* Implement a protocol for communication between clients and the server.
* Define specific commands (e.g., "ls" for listing online clients, "cr" for changing the receiver, etc.).
* When a client sends a message, the server should forward it to the intended recipient.


## Peer-to-Peer Communication:
* When a client wants to initiate a chat with another client, the "cr" command should be used to switch the current receiver.
* Allow the client to type and send messages to the current receiver.