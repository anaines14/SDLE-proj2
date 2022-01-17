# SDLE Project

SDLE Project for group T2G12.

Group members:

1. Ana Barros (up201806593@edu.fe.up.pt)
2. Ivo Saavedra (up201707093@edu.fe.up.pt)
3. João Martins (up201806436@edu.fe.up.pt)
4. Diogo Rosário (up201806582@edu.fe.up.pt)


## Compilation and Execution
Java 16+ is required to run this application.  
Maven version 4.0.0 is also required.  

To compile run from ./src/proj2/:
`mvn compile`

To run the TestApp with args: `mvn exec:java -D"exec.args"="<arg1>"`
- **arg1**: test file

To run without a test file: `mvn exec:java`

## Executing
To test the functionalities run: `TestApp.java [<test_file>]`  
The user can provide a txt file with the desired commands (one for each line).  
When no file is provided the user must input the commands on the terminal.  

These are the supported commands for TestApp:

Start a single peer: `START <username> <capacity> [<join_peer_id>]`
- **username:** peer username
- **capacity:** connection capacity
- **join_peer_id:** id of the peer to join after starting

Start multiple peers with random values: `START_MULT <n>`
- **n:** number of peers to create

Post something to the timeline: `POST <username> "<content>"`
- **username:** target peer
- **content:** the post's content

Update a post from the given user: `UPDATE <username> <post_id> "<content>"`
- **username:** target peer
- **post_id:** id of the post to edit
- **content:** updated content

Delete a single post: `DELETE <username> <post_id>`
- **username:** target peer
- **post_id:** id of the post to delete

Request timeline from another user: `TIMELINE <username> <req_timeline>`
- **username:** user that requests the timeline
- **req_timeline:** target user that provides the timeline

Subscribe to another user: `SUB <username> <target_username>`
- **username:** user that sends subscription request
- **target_username:** target of subscription

Ignore a debug prints: `IGNORE <message>`
- **message:** message type 

Add delay when sending messages (testing purposes): `MSG_DELAY <value>`
- **value:** delay in seconds

Print additional information when peer suffers changes: `LISTEN <username>`
- **username:** target peer

Print every stored information from a single peer: `PRINT <username>`
- **username:** target peer

Start authentication server: `AUTH`

Login a single user: `LOGIN <username> <password>`
- **username:** target peer
- **password:** user's password

Register user: `REGISTER <username> <password>`
- **username:** target user
- **password:** user password

Logout a user: `LOGOUT <username>`
- **username:** target user

Print every running peers' information: `PRINT_PEERS`

Stop a single peer: `STOP <username>`
- **username:** target peer

Stop execution of every single peer: `STOP_ALL`

Add a breakpoint (holds execution util user continues): `BREAK`

Pause command execution: `SLEEP <seconds>`
- **seconds:** time to wait


## Examples for test runs
### Example 1 - Simple Execution
Starts peer A with a capacity of 3.  
Starts peer B with a capacity of 6 and tells it to connect to peer A.
B requests a subscription to peer A.  
Sleeps for 3 seconds.  
Stops all peers.

```markdown
start A 3
start B 6 A
sub B A
sleep 3
stop_all
```

### Example 2 - Authentication
Starts peer A, B and C (B and C join A).  
Starts the authentication server.  
Sleeps for 5 seconds.  
Registers A, B, and C with passwords.  
Sleeps for 5 seconds.  
Logout B.  
Sleeps 5 seconds.  
A posts "hello world" to its timeline. 
Sleeps for a second.  
C requests A's timeline.  
Sleeps for 1 second.  
Prints C's information.  
Stops all peers.

```
start A 10
start B 10 A
start C 10 A
auth
sleep 5
register A Apassword
register C Cpassword
register B Bpassword
sleep 5
logout B
sleep 5
post A "hello world"
sleep 1
timeline C A
sleep 1
print C
stop_all
```




