# EECS 425 - Computer Networks Project 1 - P2P Network
The first programming project for class EECS 425 - Computer Networks, and it is used to transfer files on a P2P protocol. This program is completely built with Java. 

The main method of the program is written in p2p.java. After the program is launched, the user will be presented with a prompt, where the user could enter commands to connect to peers, disconnect from peers, request for a certain file, or exit the program. The commands are defined by the file Command.java. This file also includes launching a socket connection and initiating queries.  

At startup, the program launches a UDP peer to peer discovery protocol, which is initialized at the beginning of the program. Onwards, the user could choose from a list of IP addresses and ports of active peers online. This is done by broadcasting a UDP discovery message to the internet. When the peers receive the discovery message, the peers will respond with a list of their available ports and sending each port number as the acknowledgment. Note that for a peer to be able to receive the discovery message, the peer will have to keep a UDP server running in the background. Therefore, the UDP server is launched along with the protocol at the start of the program. And as long as a peer is online and running the program correctly, it will be able to receive and acknowledge discovery messages from other peers. The default port for the UDP discovery protocol is 52322, which is defined in p2p.java so that the user could easily re-define the port. The functionality of the UDP discovery protocol is written in PeerTopology.java. 

After the peer discovery, the user is introduced with a set of commands. The user should first use the 'Connect' command to connect to the available IP addresses and ports obtained from the UDP peer discovery protocol from above. The program would use one of its own available ports to connect to another peer with the entered IP address and port information. Meanwhile, the program could also accept a socket connection from a peer using one of its available ports. 

The peer to peer connection is handled and defined in TcpSocket.java and TcpSocketController.java. When the program is launched, the TCP socket controller generates welcome sockets from the port information provided by config_peer.txt. Each welcome socket is defined by TcpSocket.java, which includes the general behaviors of the sockets such as but not limited to reading data from and writing data to a connection. This is done by defining DataInputStreams and DataOutputStreams. The socket will be waiting for a stream coming from the peer connection by spawning a thread in the background with a while loop. This way, the program can always write a query to the peer in the foreground. The TCP socket controller keeps track of which sockets are available or in use, and updates the information to config_neighbors.txt, as well as query handling. 

After the user has established connections, the user could initiate a query for a file on the internet. The queries are triggered by the command 'Get' and will send a query to its connected neighbors by writing into the established sockets. When a peer receives the query, it will propogate the query to every socket it is connected to except the one which the query came from. Instead, the peer would check whether it has the file in its shared/ directory and send back an acknowledgement to the source. When a peer receives a query, it will first check if the query is initiated by itself. If not, the peer will pass on the query or the response to its neighbors. To prevent the queries and responses from being passed into an infinite loop, the TCP socket controller keeps track of the list of query IDs and response IDs to drop the queries that has already been processed by the peer. This way, the queries could be put to an end, and the responses could be safely propogated back to the peer initiating the query. 

When the user receives a response from a peer after checking that the response ID matches its query ID, the program creates a new socket that connects to the IP address and port information provided in the response. With this socket, the program writes a file query to the peer with the file, and the peer responds by delivering the file content to the user and storing it in the obtained/ directory.  

A few helper files are used in the program, including NetworkUtil.java and ParseFile.java, respectively containing the helper methods for obtaining external IP address information and the reading and writing to config files or transferred files.  

The code base includes a makefile. To compile the code, enter the following command into the console: 
```
make
```

To clean up the compiled file and the obtained folder, use the following command: 
```
make clean
```

To launch the program, use the following command: 
```
java p2p
```

Note that this program occassionally fails with a ConcurrentModificationException when the amount of query in the internet is heavy. However, the program will not be affected and could keep running.  
