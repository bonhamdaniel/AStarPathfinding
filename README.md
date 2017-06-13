# AStarPathfinding
Simple Java game demonstrating the A* pathfinding algorithm.

- Description: 		Search Strategy Game
  - A tiled search area game with an ant for a robot character, a donut as the goal, different terrains with different costs, obstacles that cannot be crossed, and various options that allow the user to set up the game board and execute a search.  The ant’s position is the start tile of the search, the donut’s position the goal, while the program uses an A* pathfinding algorithm to find the minimal cost path from the ant to the donut, and then displays the resulting search evolution to the user.  There are three terrains that an ant encounters on its path: open terrain with a cost of one, grassland with a cost of three, and swampland with a cost of four.  The user may setup and run the search as many times as they would like using the different setup options outlined below.
- Usage Instructions:	You are in charge of setting up the game tiles and executing the search
  - The goal of the game is to set up different game tile scenarios and execute searches, to see how the search strategy will work to maneuver the ant’s way to the donut.  You can either manually place the ant, the donut, the terrains, and the obstacles on the board yourself, choose to randomize the board completely, or choose a combination of the two.
  - The board tile setup options are controlled by the buttons:
    - “Blank”		Removes tiles from the board, allowing you to specify all.
Your first click on any empty tile will place the ant.  Your second click will place the donut.  Every click following will place either a terrain or obstacle.  Any tile containing a terrain or obstacle can be altering by re-clicking on the tile, which will cycle through the terrain/obstacle set – you can simply click each tile until it contains the object you desire.
At any time, you can click “Randomize” and it will leave the tiles you have set, but fill all the remaining empty tiles with random objects.  The “Execute” button will do the same thing, but follow immediately with a search.
    - “Randomize”	Fills every tile on the board with a random object.
This option will place one ant, one donut, and a random number of each terrain and obstacle.
    - “Full Board”	Like “Randomize”, but places the ant in the bottom right corner and the donut in the top left corner – sets up a maximum distance search.
    - “Execute”	Executes the A* Pathfinding Algorithm.
The results are shown both in the game tiles themselves, with each connection in the search being displayed consecutively, and in a text box above the buttons, where it should the node sequence and the total cost.
- Compilation:	javac AStar.java Board.java Connection.java Game.java Graph.java Heuristic.java Node.java NodeRecord.java
- Execution:		java Game (command line) or AStar.jar executable included in submission
- Bugs/Problems:		Board active when showing result path
  - User is able to click on tiles, and change them, while the result path is being displayed.
