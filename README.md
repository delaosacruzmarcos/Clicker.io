# Clicker.io

*Clicker.io is a browser based clicker game. Played with a mouse players will purchase different items that can generate passive income or increase the value of individual clicks. The two game modes goldmine and code edit, are possible by changing the json string sent between the socket server and the client.*

**Disclaimer I did not write all the code in this repository!**
More on accreditation in the acknowledgment section.

## GameActor
The GameActor class represents the individual playing the game. This is the code run on the client's machine. Utilizing the scala actor system. This allows for multiple players to join and play on the same server.

 - Big parse transfers the json data from the server into the actor’s data structures so it can be updated. 
 - Update message section returns a formed json string that is accepted by the server that contains any modifications made to the game. 
 - Buy equipment and Click message sections contain functions that activate when a client presses either a buy button or clicks. These will update their game’s state variables and change the json string sent back to the server.

## ClickerServer

The ClickerServer must be hosted by running the code on a given machine. This code contains data about the user's games along with controlling all the game actor messaging.

Upon relieving different message types such as buy, click, startgame. The server relays the proper data via json string back to the GameActor class where it can be parsed and applied to the user’s game.

## Equipment Class
The equipment class takes in class parameters that are parsed from the json string in the GameActor class. It then uses methods within it to calculate the income of the next click for the client. This state is created for each of the valid equipment of the game. This can change depending upon the game mode.

## Acknowledgment section
I made this project as part of UB’s CSE 116 curriculum. I utilized office hours and course notes to create the code I wrote. I am only posting this thanks to Dr. Hartloff leniency towards past students. Any other code not explicitly stated was written by him and the CSE 116 staff as part of the handout. That being said, I have a good understanding of how the code they wrote works. Since it’s comprehension was necessary for completion of the assignment.

## Author's note
I coded this project rather early on in my software development career. Therefore my choice of data structures and algorithms may not have been the most efficient. I have since improved my development skills. I may periodically revise this project to update it with more efficient algorithms and structures.

 - Marcos De La Osa Cruz
