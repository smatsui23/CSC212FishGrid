package edu.smith.cs.csc212.fishgrid;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class manages our model of gameplay: missing and found fish, etc.
 * @author jfoley
 *
 */
public class FishGame {
	
	/**
	 * This is the world in which the fish are missing. (It's mostly a List!).
	 */
	World world;
	/**
	 * The player (a Fish.COLORS[0]-colored fish) goes seeking their friends.
	 */
	Fish player;
	/**
	 * The home location.
	 */
	FishHome home;
	List<Fish> goHome;
	/**
	 * These are the missing fish!
	 */
	List<Fish> missing;
	
	/**
	 * These are fish we've found!
	 */
	List<Fish> found;
	
	/**
	 * These are hearts we've found!
	 */
	List<Heart> hearts;    //list of hearts for the score system??
	
	/**
	 * Number of steps!
	 */
	int stepsTaken;
	
	/**
	 * Score!
	 */
	int score;
	
	/**
	 * Create a FishGame of a particular size.
	 * @param w how wide is the grid?
	 * @param h how tall is the grid?
	 */
	
	public static final int NUM_ROCKS = 10; 
	
	public static final int NUM_HEARTS = 5;
	
	public FishGame(int w, int h) {
		world = new World(w, h);
		
		missing = new ArrayList<Fish>();
		found = new ArrayList<Fish>();
		goHome = new ArrayList<Fish>();
		hearts = new ArrayList<Heart>();
		
		// Add a home!
		home = world.insertFishHome();
		
		
		
		
		for (int i=0; i<NUM_ROCKS; i++) {
			world.insertRockRandomly();
		}
		
		world.insertSnailRandomly();
		
		
		for (int i=0; i<NUM_HEARTS; i++) {
			Heart point = world.insertHeartRandomly();
			hearts.add(point);
		}
		
		
		// Make the player out of the 0th fish color.
		player = new Fish(0, world);
		// Start the player at "home".
		player.setPosition(home.getX(), home.getY());
		player.markAsPlayer();
		world.register(player);
		
		// Generate fish of all the colors but the first into the "missing" List.
		for (int ft = 1; ft < Fish.COLORS.length; ft++) {
			Fish friend = world.insertFishRandomly(ft);
			missing.add(friend);
		}	
	}	
	
	
	/**
	 * How we tell if the game is over: if missingFishLeft() == 0.
	 * @return the size of the missing list.
	 */
	public int missingFishLeft() {
		return missing.size();
	}
	
	/**
	 * This method is how the Main app tells whether we're done.
	 * @return true if the player has won (or maybe lost?).
	 */
	public boolean gameOver() {
		return (missing.size() == 0 && found.size() == 0) ;
	}

	/**
	 * Update positions of everything (the user has just pressed a button).
	 */
	public void step() {
		// Keep track of how long the game has run.
		this.stepsTaken += 1;
		
		//After 20 steps, a fish in the front of the found list can 
		//move into missing list again
		Random rand = ThreadLocalRandom.current();
		if (rand.nextDouble() < 0.3) {
			if (found.size() > 2) {
				if (this.stepsTaken > 20) {
					found.remove(0);
					//how to add the removed index to missing list ??
				}
			}	
		}
		
		
		
				
		// These are all the objects in the world in the same cell as the player.
		List<WorldObject> overlap = this.player.findSameCell();
		// The player is there, too, let's skip them.
		overlap.remove(this.player);
		
		// If we find a fish, remove it from missing.
		for (WorldObject wo : overlap) {
			// It is missing if it's in our missing list.
			if (missing.contains(wo)) {
				if (!(wo instanceof Fish)) {
					throw new AssertionError("wo must be a Fish since it was in missing!");
				}
				// Convince Java it's a Fish (we know it is!)
				Fish justFound = (Fish) wo;
				
				found.add(justFound);
				
				// Remove from missing list.
				missing.remove(justFound);
				
				// Increase score when you find a fish!
				score += 10;
				
				
				//TO DO: DOES NOT WORK...WHY??
				if (Color.green.equals(((Fish)wo).getColor())) {
					score += 10;
				}
				
				
				 
			}
			
			if (hearts.contains(wo)) {
				Heart justFound = (Heart) wo;
				score +=5;
				hearts.remove(justFound);
				world.remove(wo);
			}
			
			
			
			
		}
		
		// Make sure missing fish *do* something.
		wanderMissingFish();
		// When fish get added to "found" they will follow the player around.
		World.objectsFollow(player, found);
		
		//When the player returns home, fish in found list should move to goHome list
		if (player.getX() == home.getX() && player.getY() == home.getY()) {
			goHome.addAll(found);
			 
			for (Fish fish: found) {
				world.remove(fish);
	
			}
			found.removeAll(found);
			//make a for-loop and remove each found fish from the found list 
			//still not working?
		}
		
		
		//Fish that wander home by accident should be marked accordingly as home!
		//if (fish.getX() == home.getX() && fish.getY() == home.getY() ) {
		//	goHome.addAll(found);
		//	found.removeAll(found); 
		//}
		
		
		
		// Step any world-objects that run themselves.
		world.stepAll();
	}
	
	/**
	 * Call moveRandomly() on all of the missing fish to make them seem alive.
	 */
	private void wanderMissingFish() {
		Random rand = ThreadLocalRandom.current();
		for (Fish lost : missing) {
			// 30% of the time, lost fish move randomly.
			if (rand.nextDouble() < 0.3) {
				lost.moveRandomly();
				if (rand.nextDouble() < 0.8) {
					lost.fastScared();
				}
				if (rand.nextDouble() < 0.3) {
					lost.noMove();
				}
			}
			
			if(lost.getX() == home.getX() && lost.getY() == home.getY()) {
				goHome.addAll(found);
				found.removeAll(found); 
				//world.remove(Fish); ///How does this work??
			}
		}
	}
	
	

	

	/**
	 * This gets a click on the grid. We want it to destroy rocks that ruin the game.
	 * @param x - the x-tile.
	 * @param y - the y-tile.
	 */
	public void click(int x, int y) {
		// TODO(FishGrid) use this print to debug your World.canSwim changes!
		System.out.println("Clicked on: "+x+","+y+ " world.canSwim(player,...)="+world.canSwim(player, x, y));
		List<WorldObject> atPoint = world.find(x, y);
		// TODO(FishGrid) allow the user to click and remove rocks.
		for (WorldObject ro: atPoint) {
			if (ro instanceof Rock) {
				world.remove(ro);
			}
		}
		//check every object atPoint parameter 
		// if it is a rock.. remove 
		
		
	}
	
}
