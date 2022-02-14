package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Pathfinding extends ApplicationAdapter {
	final int SPACING = 30;
	SpriteBatch batch;
	Texture startImg;
	Texture targetImg;

	Integer[] start;
	Integer[] target;
	ArrayList<int[]> walls;
	ArrayList<String> stringWalls;
	ShapeRenderer wallShape;
	DijkstraAlgorithm Dijkstra;

	OrthographicCamera camera;
	ShapeRenderer grids;

	@Override
	public void create() {
		batch = new SpriteBatch();
		startImg = new Texture(Gdx.files.internal("start.png"));
		targetImg = new Texture(Gdx.files.internal("target.png"));

		grids = new ShapeRenderer();
		walls = new ArrayList<int[]>();
		stringWalls = new ArrayList<String>();
		wallShape = new ShapeRenderer();

		// Algorithm
		Dijkstra = new DijkstraAlgorithm();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 960, 540);
	}

	// make Node class so each node will have parents, itself, and its value.
	public static class Node {
		Node parent;
		Integer[] node;

		public Node(Node parent, Integer[] node) {
			this.parent = parent;
			this.node = node;
			// this works like a linked list which will be useful for backtracking
			// in the "backtrack" function, the program runs something like; null <- ... <- node.parent.parent <- node.parent <- node
		}
	}

	public class DijkstraAlgorithm {
		// unvisited variable store Rectangle value of active nodes (the ones that can spread),
		// visited variable is where the inactive nodes get added to.
		ArrayList<Node> unvisited = new ArrayList<Node>();
		ArrayList<Node> visited = new ArrayList<Node>();
		ArrayList<String> allNeighbors = new ArrayList<String>(); // for storing every coordinate
		Node currentNode;

		// ShapeRenderer for drawing the flood and path
		ShapeRenderer shape = new ShapeRenderer();
		ArrayList<Node> path = new ArrayList<Node>();

		boolean run = false;

		private void spread() {
			try {
				if (run) {
					// check all neighbors from a node, the 'neighbors' are nodes that connects to the node
					// in cross-shaped form.
					Integer[][] neighbors = {
							{currentNode.node[0] + SPACING, currentNode.node[1]}, // RIGHT
							{currentNode.node[0], currentNode.node[1] + SPACING}, // TOP
							{currentNode.node[0] - SPACING, currentNode.node[1]}, // LEFT
							{currentNode.node[0], currentNode.node[1] - SPACING}, // BOTTOM
					};

					for (Integer[] neighbor : neighbors) {
						int value = Math.abs((start[0] - neighbor[0]) +
								(start[1] - neighbor[1])); // getting the value of each node's distance relative to start

						Node neighborNode = new Node(currentNode, neighbor);
						String stringNeighbor = Arrays.toString(new int[]{(int) neighbor[0], (int) neighbor[1]});

						if (Arrays.equals(currentNode.node, target)) // if the current node is on target,
							backtrack(currentNode);                   // backtrack from current node to start node.

						// filter all sorts of unwanted things; make sure that the new node(neighbor) has not been visited,
						// make sure the flood won't overlap/cross the walls. the last 4 make sure neighbor isn't beyond the screen limit.
						if (!allNeighbors.contains(stringNeighbor) && !stringWalls.contains(stringNeighbor) &&
								-SPACING < neighbor[0] && neighbor[0] < Gdx.graphics.getWidth() &&
								-SPACING < neighbor[1] && neighbor[1] < Gdx.graphics.getHeight()) {
							allNeighbors.add(stringNeighbor);
							unvisited.add(neighborNode);
						}
					}
					// remove the current node from unvisited, and turn/add it to visited.
					visited.add(currentNode);
					unvisited.remove(currentNode);

					// this is because the unvisited removed the closest/recently added node. which means
					// the next index 0 will be the 'most recent' node.
					currentNode = unvisited.get(0);
				}
			} catch (Exception ignored) {} // ignore different kind of scenarios that can break the program
		}

		private void backtrack(Node finished) {
			// call this function when the flood finally found the target node.
			// from here, keep calling the parent's node until the node meets null, which means it will
			// go back to the starting node, hence the name; backtracking.
			run = false;
			while (finished != null) {
				path.add(finished);
				finished = finished.parent;
			}
			// this will call the parent until it reaches start (null <- ... <- node.parent.parent <- node.parent <- node)
		}

		private void draw() {
			// draw each node on each category with different colors to differentiate them
			for (Node node : unvisited) {
				shape.setColor(100f, 0f, 0f, 0.3f);
				shape.rect(node.node[0], node.node[1], SPACING, SPACING);
			}
			for (Node node : visited) {
				shape.setColor(100, 0, 100, 0);
				shape.rect(node.node[0], node.node[1], SPACING, SPACING);
			}

			if (path != null) {
				for (Node node : path) {
					shape.setColor(100, 100, 0, 1);
					shape.rect(node.node[0], node.node[1], SPACING, SPACING);
				}
			}
		}
	}

	private void drawWalls() {
		if (!walls.isEmpty()) {
			wallShape.setColor(0, 0, 0, 1);
			for(int[] wall : walls)
				wallShape.rect(wall[0], wall[1], SPACING, SPACING); }
	}

	private void drawGrids() { // credit to: https://stackoverflow.com/q/24215500
		grids.setColor(0, 0, 0, 1);
		for (int i = 0; i < Gdx.graphics.getHeight() / SPACING; i++) {
			grids.line(0, i * SPACING, Gdx.graphics.getWidth(), i * SPACING);
		}
		for (int i = 0; i < Gdx.graphics.getWidth() / SPACING; i++) {
			grids.line(i * SPACING, 0, i * SPACING, Gdx.graphics.getHeight());
		}
	}

	@Override
	public void render() {
		ScreenUtils.clear(255, 255, 255, 1);
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		// draw start and target with Texture from above.
		if (start != null)
			batch.draw(startImg, start[0], start[1], SPACING, SPACING);
		if (target != null)
			batch.draw(targetImg, target[0], target[1], SPACING, SPACING);
		batch.end();

		grids.begin(ShapeRenderer.ShapeType.Line);
		drawGrids();
		grids.end();

		wallShape.begin(ShapeRenderer.ShapeType.Filled);
		drawWalls();
		wallShape.end();

		Dijkstra.spread();

		Dijkstra.shape.begin(ShapeRenderer.ShapeType.Filled);
		Dijkstra.draw();
		Dijkstra.shape.end();

		// get user inputs
		// set the starting node when user click on grid.
		if (Gdx.input.isTouched()) {
			int x = (Gdx.input.getX() / SPACING) * SPACING;
			int y = Gdx.graphics.getHeight() - ((Gdx.input.getY() / SPACING) * SPACING) - SPACING;

			start = new Integer[]{x, y};
		}
		// same thing with the starting node, but instead of mouse click, this uses tab to add target node.
		if (Gdx.input.isKeyPressed(Input.Keys.TAB)) {
			int x = (Gdx.input.getX() / SPACING) * SPACING;
			int y = Gdx.graphics.getHeight() - ((Gdx.input.getY() / SPACING) * SPACING) - SPACING;

			target = new Integer[]{x, y};
		}
		// add wall from coordinate of the mouse when user holds "W"
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			int x = (Gdx.input.getX() / SPACING) * SPACING;
			int y = Gdx.graphics.getHeight() - ((Gdx.input.getY() / SPACING) * SPACING) - SPACING;

			int[] pos = {x, y};
			if (!stringWalls.contains(Arrays.toString(pos))){
				walls.add(pos);
				stringWalls.add(Arrays.toString(pos));}
		}
		// start the algorithm when user press space key.
		if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
			Dijkstra.currentNode = new Node(null, start);
			Dijkstra.run = true;
		}
	}

	public void dispose() {
		batch.dispose();
		startImg.dispose();
		targetImg.dispose();
	}
}