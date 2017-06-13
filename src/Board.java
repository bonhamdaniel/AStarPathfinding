/*
 * Board class - handles the bulk of the game duties.
 */

import java.awt.BorderLayout;// imports library necessary to use BorderLayout in JPanel
import java.awt.Color;// imports library used to implement Game background color
import java.awt.Dimension;// imports library necessary to use Dimension objects
import java.awt.Graphics;// imports library necessary for the Graphics used to implement Game board
import java.awt.Graphics2D;// imports library necessary for the Graphics2D used to implement Game board
import java.awt.GridLayout;// imports library necessary for the GridLayout
import java.awt.Point;// imports library necessary for the Point object, used for positions
import java.awt.Toolkit;// imports library necessary for the Toolkit
import java.awt.event.ActionEvent;// imports library necessary for handling ActionEvents
import java.awt.event.ActionListener;// imports library necessary for using an ActionListener
import java.awt.event.MouseEvent;// imports library necessary for using a MouseEvent object
import java.awt.event.MouseListener;// imports library necessary for using an MouseListener
import java.io.File;// imports library necessary for using a File
import java.util.ArrayList;// imports library necessary for ArrayList usage
import java.util.Random;// imports library necessary for random number generation
import javax.sound.sampled.AudioInputStream;// imports library necessary for using an Audio stream
import javax.sound.sampled.AudioSystem;// imports library necessary for using an Audio System
import javax.sound.sampled.Clip;// imports library necessary for using an audio clip
import javax.swing.ImageIcon;// imports library necessary for using the ImageIcon, used to creates images
import javax.swing.JButton;// imports library necessary for using JButtons
import javax.swing.JPanel;// imports library necessary for using JPanel
import javax.swing.JTextField;// imports library necessary for the text field that holds the score
import javax.swing.Timer;// imports library necessary for the timer used

public class Board extends JPanel implements ActionListener, MouseListener {
	private static final long serialVersionUID = 1L;// serialization variable
	private Timer timer;// represents the game timer
	private final int DELAY = 10;// used to set the between-event delay
	
	private final int WIDTH_TILES = 16;// the number of width tiles on the game board
	private final int HEIGHT_TILES = 16;// the number of height tiles on the game board
	private final int TOTAL_TILES = WIDTH_TILES * HEIGHT_TILES;// the total number of tiles on the game board

	private Node[] nodes = new Node[TOTAL_TILES];// used to represent each node/tile in the Game space
	private String[] icons = {"grass.png", "swamp.png", "terrain.png", "rock.png"};// the different images used for the tiles
	private int[] costs = {3, 4, 1, 999999};// the different costs associated with tiles
	private Graph graph = new Graph();// the graph used to represent the Game space
	private File buttonSound = new File("buttonpress.wav");// sound for button presses
	private File pathSound = new File("pathsound.wav");// sound for path nodes
	private File successSound = new File("successSound.wav");// sound for end of path
	private File emptySound = new File("emptySound.wav");// sound for end of path
	
	private boolean addAnt = false;// used to determine when an ant has or hasn't been added to the board
	private boolean addDonut = false;// used to determine when a donut has or hasn't been added to the board
	private boolean wantRandom = true;// used to determine when tiles should be filled with random entities
	private boolean blankBoard = false;// used to determine when a blank board is in use
	private boolean active = false;// used to determine whether a path is active
	
	private int start = 0;// used to represent the start node in the search, where the ant is
	private int goal = 0;// used to represent the goal node in the search, where the donut is
	
	private ArrayList<Connection> results;// used to hold the results of the Pathfinding search
	private int resultCount = 0;// used to hold the number of connections in the results of the Pathfinding search
	private boolean result = false;// used to determine when a result is ready to be displayed
	private int timeCount = 0;// used to represent the amount of time that has gone by in the Game, used to control result display
	
	private JPanel statusPane = new JPanel();// adds a JPanel to hold the path details of the result
	private JTextField pathDetails = new JTextField();// holds the path details
	
	private JPanel buttonPane = new JPanel(new GridLayout(1, 3));// adds a JPanel to hold the buttons
	private JButton randomize = new JButton("Randomize");// the button to randomize the tiles on the board
	private JButton execute = new JButton("Execute");// the button to execute the search of the tiles on the board
	private JButton blank = new JButton("Blank");// the button to remove the tiles on the board
	private JButton fullBoard = new JButton("Full Board");// the button to randomize the tiles on the board with the ant at the last and donut at first
	
	private JPanel infoPane = new JPanel();// the panel to hold the path details and the buttons
	
	// Board constructor - no parameters necessary
	public Board() {
		addMouseListener(this);// adds a Mouse listener on the game space
		setFocusable(true);// allows focusable
		setPreferredSize(new Dimension(640, 685));// sets the preferred size of the game space window
		setBackground(Color.WHITE);// sets the background color of the display to white
		setDoubleBuffered(true);// sets double buffered to true
		setLayout(new BorderLayout());// creates the layout for the game panel
		
		infoPane.setLayout(new GridLayout(2,1));// sets the layout for the details/buttons pane
		
		pathDetails.setSize(640, 25);// sets the size of the path details box
		statusPane.setLayout(new GridLayout(1, 1));;// sets the layout for the status JPanel pane
		statusPane.add(pathDetails);// adds the path details JTextField to the status pane
		infoPane.add(statusPane, BorderLayout.SOUTH);// adds the statusPane holding the path details to the display
		
		randomize.addActionListener(this);// adds a listener for the randomize button
		execute.addActionListener(this);// adds a listener for the execute button
		blank.addActionListener(this);// adds a listener for the blank button
		fullBoard.addActionListener(this);// adds a listener for the fullBoard button
		
		buttonPane.setLayout(new GridLayout(1, 3));// sets the layout for the buttonPane JPanel pane
		buttonPane.add(blank);// adds the fullBoard button to the buttonPane
		buttonPane.add(randomize);// adds the blank button to the buttonPane
		buttonPane.add(fullBoard);// adds the randomize button to the buttonPane
		buttonPane.add(execute);// adds the execute button to the buttonPane
		
		infoPane.add(buttonPane, BorderLayout.SOUTH);// adds the buttonPane, holding the buttons, to the display
		add(infoPane, BorderLayout.SOUTH);// adds the infoPane to the JPanel displaying the game
		
		for (int i = 0; i < nodes.length; i++)// loops through the game nodes
			nodes[i] = new Node();// fills each element with a blank node to start
		
		blank.doClick();// calls the method to populate the board with random tiles to start
		
		timer = new Timer(DELAY, this);// creates timer with between-event DELAY 
		timer.start();// starts the timer
	}// Board() constructor
	
	@Override // paintComponent method - performs duties to paint Board
	public void paintComponent(Graphics g) {
		super.paintComponent(g);// calls JPanel paintComponent
		doDrawing(g);// calls the doDrawing method to get graphics information about objects in the Game
		Toolkit.getDefaultToolkit().sync();// synchronizes the toolkit state
	}// paintComponent(Graphics) method
	
	// doDrawing method - gets graphics information for each object in the Game
	private void doDrawing(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;// converts Graphics g to Graphics2D
		for (int i = 0; i < WIDTH_TILES; i++) {// loops through x-coords
			for (int j = 0; j < HEIGHT_TILES; j++) {// loops through y-coords
				g2d.drawImage(nodes[i * WIDTH_TILES + j].getImage(), nodes[i * WIDTH_TILES + j].getTile().x, nodes[i * HEIGHT_TILES + j].getTile().y, this);// draws each tile with its current information
			}// for (j)
		}// for (i)
		
		if (result && results != null && resultCount < results.size()) {// checks if results are ready to be displayed
			if (timeCount++ % 75 == 0) {// staggers the evolution of the display of the search path results
				if (nodes[results.get(resultCount).getToNode()].getCost() == 3) {// checks for grass tiles on the path
					playSound(pathSound);// plays sound associated with path nodes
					nodes[results.get(resultCount++).getToNode()].setImage(new ImageIcon("grassPath.png").getImage());// changes image to display path
				} else if (nodes[results.get(resultCount).getToNode()].getCost() == 4) {// checks for swamp tiles on the path
					playSound(pathSound);// plays sound associated with path nodes
					nodes[results.get(resultCount++).getToNode()].setImage(new ImageIcon("swampPath.png").getImage());// changes image to display path
				} else if (nodes[results.get(resultCount).getToNode()].getCost() == 1) {// checks for terrain tiles on the path
					playSound(pathSound);// plays sound associated with path nodes
					nodes[results.get(resultCount++).getToNode()].setImage(new ImageIcon("terrainPath.png").getImage());// changes image to display path
				}else if (resultCount == results.size()-1) {// checks for end of path
					playSound(successSound);// plays sound associated with buttons
					result = false;
				} else if (nodes[results.get(resultCount).getToNode()].getCost() == 999999) {// checks for obstacle tiles on the path
					result = false;// sets flag to indicate result path is done
					resultCount = 0;// resets result count
				}// if (node type)
			}// if (timecount)
		} else {// if no results are currently ready
			resultCount = 0;// resets result count
			result = false;// sets flag to indicate result path is done
		}// if (results)	
	}// doDrawing(Graphics) method
	
	@Override // actionPerformed method - main game loop, updates Game world
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == randomize) {// checks for cases where the randomize button has been pressed
			playSound(buttonSound);// plays sound associated with buttons
			randomize();// calls the randomize method to fill the board with random tiles
			execute.setEnabled(true);// enables the execute button
			resultCount = 0;// resets the result count
			result = false;// sets result flag to indicate no results
			active = false;// sets the active flag to indicate not active
		} else if (e.getSource() == execute) {// checks for cases where the execute button has been pressed
			if (nodes[255].getCost() != -1) {// checks whether board is complete
				execute();// calls the method to execute the search
				wantRandom = true;// sets flag to handle randomization of the board
				blankBoard = false;// sets flag that board is not blank
				addAnt = false;// flag to indicate if ant is on the board
				addDonut = false;// flag to indicate if donut is on the board
			} else {// if board is not complete
				randomize();// calls the randomize method to fill the board with random tiles
				execute();// calls the method to execute the search
				wantRandom = true;// sets flag to handle randomization of the board
				blankBoard = false;// sets flag that board is not blank
				addAnt = false;// flag to indicate if ant is on the board
				addDonut = false;// flag to indicate if donut is on the board
			}// if (board complete)
			result = true;// sets result flag to indicate results are ready
			active = true;// sets the active flag to indicate a path is being drawn
			execute.setEnabled(false);// disables execute button
			fullBoard.setEnabled(true);// enables fullBoard button
		} else if (e.getSource() == blank) {// checks for cases where the blank button has been pressed
			playSound(emptySound);// plays sound associated with buttons
			blankBoard = true;// sets flag that board is blank
			addAnt = false;// flag to indicate if ant is on the board
			addDonut = false;// flag to indicate if donut is on the board
			blank();// calls the method to empty the board
			wantRandom = false;// sets flag to handle randomization of the board
			execute.setEnabled(true);// enables the execute button
			resultCount = 0;// resets the result count
			result = false;// sets result flag to indicate no results
			active = false;// sets the active flag to indicate not active
			fullBoard.setEnabled(false);// disables fullBoard button
		} else if (e.getSource() == fullBoard) {// checks for cases where the fullBoard button has been pressed
			playSound(buttonSound);// plays sound associated with buttons
			fullBoard();// calls the method that sets up a full board random search
			blankBoard = false;// sets flag that board is not blank
			execute.setEnabled(true);// enables the execute button
			resultCount = 0;// resets the result count
			result = false;// sets result flag to indicate no results
			active = false;// sets the active flag to indicate not active
		}// if (button)
		repaint();// repaints with current information
	}// actionPerformed(ActionEvent) method
	
	// playSound method - takes sound, plays the given sound
	public void playSound(File sound) {
		try {// tries to perform audio tasks
			AudioInputStream swampIn = AudioSystem.getAudioInputStream(sound);// gets input stream from file
			Clip clip = AudioSystem.getClip();// gets a clip to use with input
			clip.open(swampIn);// opens the input stream in clip
			clip.start();// starts the clip
		} catch (Exception e) {// catches any exceptions
			e.printStackTrace();// prints error trace
		}// try
	}// playSound(file) method
	
	// randomize method - no parameters, fills tiles with random entities
	public void randomize() {
		Random random = new Random();// random generator used to get random int
		int num;// will hold the random int that determines with entity to use for a tile
		for (int i = 0; i < WIDTH_TILES; i++) {// loops x-coords
			for (int j = 0; j < HEIGHT_TILES; j++) {// loops y-coords
				num = random.nextInt(4);// gets random int from 0 to 3
				if (nodes[i * WIDTH_TILES + j].getCost() == -1 || wantRandom) {// if tile empty or random chosen
					nodes[i * WIDTH_TILES + j] = new Node(new Point(i * 640 / WIDTH_TILES, j * 640 / HEIGHT_TILES), new ImageIcon(icons[num]).getImage(), costs[num]);// creates random tile
				}// if (random)
			}// for (j)
		}// for (i)
		
		if ( (!addAnt && blankBoard) || !blankBoard ) {// checks whether an ant needs to be added
			start = random.nextInt(TOTAL_TILES);// gets random int and sets as start
			Point point = nodes[start].getTile();// gets the information currently at the tile
			nodes[start] = new Node(point, new ImageIcon("ant.png").getImage(), 0);// sets ant at the node
			addAnt = true;// sets flag to indicate the ant has been added
		}// if (ant)
			
		if ( (!addDonut && blankBoard) || !blankBoard ) {// checks whether a donut needs to be added
			goal = random.nextInt(TOTAL_TILES);// gets random int and sets as goal
			Point point = nodes[goal].getTile();// gets the information currently at the tile
			nodes[goal] = new Node(point, new ImageIcon("donut.png").getImage(), 0);// sets donut at the node
			addDonut = true;// sets flag to indicate the donut has been added
		}// if(donut)
	}// randomize() method
	
	// fullBoard method - no parameters, sets up a full board search with random tiles
	public void fullBoard() {
		Random random = new Random();// random generator to get random int for generating random tiles
		int num;// will hold the random int
		for (int i = 0; i < WIDTH_TILES; i++) {// loops x-coords
			for (int j = 0; j < HEIGHT_TILES; j++) {// loops y-coords
				num = random.nextInt(4);// gets random int between 0 and 3
				nodes[i * WIDTH_TILES + j] = new Node(new Point(i * 640 / WIDTH_TILES, j * 640 / HEIGHT_TILES), new ImageIcon(icons[num]).getImage(), costs[num]);// creates random tile
			}// for (j)
		}// for (i)
		
		start = 255;// sets start to last tile
		Point point = nodes[start].getTile();// gets coords of last tile
		nodes[start] = new Node(point, new ImageIcon("ant.png").getImage(), 0);// sets ant at last tile
		
		goal = 0;// sets goal to first tile
		point = nodes[goal].getTile();// gets coords of first tile
		nodes[goal] = new Node(point, new ImageIcon("donut.png").getImage(), 0);// sets donut at last tile
	}// fullBoard() method
	
	// execute method - performs the A* search on the board
	public void execute() {
		for (int i = 0; i < WIDTH_TILES; i++) {// loops x-coords
			for (int j = 0; j < HEIGHT_TILES; j++) {// loops y-coords
				addNeighbours(i, j, 0);// adds neighbours/connections for each node
			}// for (j)
		}// for (i)
		
		ArrayList<Connection> temp = new ArrayList<Connection>();// will store connections with obstacles
		for (int i = 0; i < graph.getConnections().size(); i++) {// loops through all connections in the graph
			if (nodes[graph.getConnections().get(i).getToNode()].getCost() != 999999) {// check for obstacles
				graph.getConnections().get(i).setCost(nodes[graph.getConnections().get(i).getToNode()].getCost());// sets cost for each connection
			} else {// found an obstacle
				temp.add(graph.getConnections().get(i));// flag a connection for an obstacle
			}// if (!obstacle)
		}// for (graph)
		
		for (Connection connection : temp)// loops through connections flagged for obstacles
			graph.getConnections().remove(connection);// removes any connections with obstacles
		
		Heuristic heuristic = new Heuristic(goal);// creates new heuristic with goal node specified
		results = AStar.pathFindAStar(graph, start, goal, heuristic);// calls A* pathfinding method and receives results
		
		int cost = 0;// will hold path cost
		if (results != null) {// if a path was found
			for (int i = 0; i < results.size(); i++)// loops through connections in the path
				cost += results.get(i).getCost();// adds cost of each connection to the total
		}// if (results)
		pathDetails.setText("Path: " + start + "->" + results + " Cost: " + cost);// displays path details
	}// execute() method
	
	// blank method - creates a blank board for the user to manually specify
	public void blank() {
		pathDetails.setText("Please click where you'd like to start the ant.");// displays message to the user
		for (int i = 0; i < TOTAL_TILES; i++)// loops through all tiles
			nodes[i] = new Node(new Point(i / WIDTH_TILES * 40, i % HEIGHT_TILES * 40), new ImageIcon("empty.png").getImage(), -1);// sets empty tile
	}// blank() method
	
	// addNeighbours method - x-coord, y-coord, cost; sets a tiles neighbours
	public void addNeighbours(int i, int j, int num) {
		int fromNode;// origin node of the connection
		int toNode;// destination node of the connection
		ArrayList<Connection> connections = new ArrayList<Connection>();// will hold the connections	
			fromNode = i * WIDTH_TILES + j;// calculates the from node by the coordinates
			if (i > 0 && j > 0) {// not on left or top game border
				toNode = (i-1) * WIDTH_TILES + (j-1);
				connections.add(new Connection(fromNode, toNode, costs[num]));// add new connection
			}// if (border)
			if (i > 0) {// not on left game border
				toNode = (i-1) * WIDTH_TILES + (j);// calculates to node of connection from coords
				connections.add(new Connection(fromNode, toNode, costs[num]));// add new connection
			}// if (border)
			if (i > 0 && j < 15) {// not on left or bottom game border
				toNode = (i-1) * WIDTH_TILES + (j+1);// calculates to node of connection from coords
				connections.add(new Connection(fromNode, toNode, costs[num]));// add new connection
			}// if (border)
			if (j > 0) {// not on top game border
				toNode = (i) * WIDTH_TILES + (j-1);// calculates to node of connection from coords
				connections.add(new Connection(fromNode, toNode, costs[num]));// add new connection
			}// if (border)
			if (j < 15) {// not on bottom game border
				toNode = (i) * WIDTH_TILES + (j+1);// calculates to node of connection from coords
				connections.add(new Connection(fromNode, toNode, costs[num]));// add new connection
			}// if (border)
			if (i < 15 && j > 0) {// not on right or top game border
				toNode = (i+1) * WIDTH_TILES + (j-1);// calculates to node of connection from coords
				connections.add(new Connection(fromNode, toNode, costs[num]));// add new connection
			}// if (border)
			if (i < 15) {// not on right game border
				toNode = (i+1) * WIDTH_TILES + (j);// calculates to node of connection from coords
				connections.add(new Connection(fromNode, toNode, costs[num]));// add new connection
			}// if (border)
			if (i < 15 && j < 15) {// not on right or bottom game border
				toNode = (i+1) * WIDTH_TILES + (j+1);// calculates to node of connection from coords
				connections.add(new Connection(fromNode, toNode, costs[num]));// add new connection
			}// if (border)
			graph.addConnections(connectionList(connections));
	}// addNeighbours(int, int, int) method
	
	// connectionList method - connections, copies elements from one list and returns the results in another
	public ArrayList<Connection> connectionList(ArrayList<Connection> connection) {
		ArrayList<Connection> result = new ArrayList<Connection>();// will hold list to return
		for (int i = 0; i < connection.size(); i++)// loops through all connection in list
			result.add(connection.get(i));// copies each connection into new list
		return result;// returns result list
	}// connectionList(ArrayList<Connection>) method

	// mousePressed method - handles mouse click events
	public void mousePressed(MouseEvent e) {
		Point position = e.getPoint();// gets coordinates of the click
		if (position.x < 640 && position.y < 640) {// determines if the click is within the bounds of the game space
			int x = (int)Math.floor(position.x / 40);// gets the x tile value
			int y = (int)Math.floor(position.y / 40);// gets the y tile value
			Point point = new Point(position.x - position.x % 40, position.y - position.y % 40);// gets the uper left coordinate of the tile
			if (!addAnt && blankBoard && !active) {// checks if an ant needs to be added
				start = x * WIDTH_TILES + y;// gets the node number and sets it to start
				nodes[x * WIDTH_TILES + y] = new Node(point, new ImageIcon("ant.png").getImage(), 0);// adds ant at specified node
				pathDetails.setText("Please click where you'd like to set the donut.");// displays instructions to the user
				addAnt = true;// sets flag to indicate ant added
			} else if (!addDonut && blankBoard && !active) {// checks if a donut needs to be added
				goal = x * WIDTH_TILES + y;// gets the node number and sets it to goal
				nodes[x * WIDTH_TILES + y] = new Node(point, new ImageIcon("donut.png").getImage(), 0);// adds donut at specified node
				pathDetails.setText("Please add all desired obstacles and/or terrains, then click randomize or execute.");// displays instructions to the user
				addDonut = true;// sets flag to indicate donut added
			} else if (!active){// tiles to be changed
				if (nodes[x * WIDTH_TILES + y].getCost() == -1 || nodes[x * WIDTH_TILES + y].getCost() == 999999) {// if tile is an obstacle
					nodes[x * WIDTH_TILES + y] = new Node(point, new ImageIcon("terrain.png").getImage(), 1);// set node to terrain
				} else if (nodes[x * WIDTH_TILES + y].getCost() == 1) {// if tile is an terrain
					nodes[x * WIDTH_TILES + y] = new Node(point, new ImageIcon("grass.png").getImage(), 3);// set node to grass
				} else if (nodes[x * WIDTH_TILES + y].getCost() == 3) {// if tile is an grass
					nodes[x * WIDTH_TILES + y] = new Node(point, new ImageIcon("swamp.png").getImage(), 4);// set node to swamp
				} else if (nodes[x * WIDTH_TILES + y].getCost() == 4) {// if tile is an swamp
					nodes[x * WIDTH_TILES + y] = new Node(point, new ImageIcon("rock.png").getImage(), 999999);// set node to obstacle
				}// if (tile)
			}// if (ant)
		}// if (on board)
	}// mousePressed(MouseEvent) method
	
	// must be present for MouseListener
	public void mouseReleased(MouseEvent e) {
	}// mouseReleased(MouseEvent) method
	
	// must be present for MouseListener
	public void mouseEntered(MouseEvent e) {
	}// mouseEntered(MouseEvent) method
	
	// must be present for MouseListener
	public void mouseExited(MouseEvent e) {
	}//  mouseExited(MouseEvent) method
	
	// must be present for MouseListener
	public void mouseClicked(MouseEvent e) {
	}// mouseClicked(MouseEvent) method
}// Board class