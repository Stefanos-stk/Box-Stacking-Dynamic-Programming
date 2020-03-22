import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//@Author: Stefanos Stoikos CS 140 Algorithms

public class Solution {
	public static void main(String[] args) throws IOException {
		// Arraylist that will store the initial input blocks
		List<Block> blocks = new ArrayList<Block>(500);

		// Getting the input file name as args[0]
		File file = new File(args[0]);
		Scanner scanner = new Scanner(file);
		// We skip the first int
		scanner.nextInt();
		// We create the blocks with the input file
		while (scanner.hasNextInt()) {
			blocks.add(new Block(scanner.nextInt(), scanner.nextInt(), scanner.nextInt()));
		}
		scanner.close();
		// Printing out the max height and parsing in the args[1] which is the output
		// file name
		System.out.print(" blocks and a height of " + maxHeightSol(blocks, args[1]));

	}

	// Arraylist that will contain the required blocks for the max height
	public static List<Block> result;

	// Creating a comparable data structure object
	public static class Block implements Comparable<Block> {
		// Variables that we are going to be using
		int width, height, length, bArea; // bArea is the base area that is going to be lying on top of the other block

		// Creating the data structure of a block (l,w,h)
		public Block(int length, int width, int height) {
			this.height = height;
			this.width = width;
			this.length = length;

		}

		// Compare to method in order to store the blocks in order based on their bArea
		@Override
		public int compareTo(Block block) {
			return this.bArea - block.bArea; // returns an int
		}

		// To string method in order to view the dimensions of the blocks
		public String toString() {
			return "Length: " + length + ",Width: " + width + ",Height: " + height;

		}

	}

	public static int maxHeightSol(List<Block> blocks, String name) {
		// Creating an array that will store all the possible rotations of the blocks
		List<Block> rotations = new ArrayList<>(500);

		// Iterate through the given blocks and populate the rotations arraylist with
		// all the possible combinations (3!)
		for (Block b : blocks) {

			// L W H
			rotations.add(b);
			// L H W
			rotations.add(new Block(b.length, b.height, b.width));
			// W H L
			rotations.add(new Block(b.width, b.height, b.length));
			// W L H
			rotations.add(new Block(b.width, b.length, b.height));
			// H L W
			rotations.add(new Block(b.height, b.length, b.width));
			// H W L
			rotations.add(new Block(b.height, b.width, b.length));

		}

		// Sorts them based on the base area, using lambda expressions
		Collections.sort(rotations, (o1, o2) -> (o2.length * o2.width) - (o1.length * o1.width));

		// Initialize array for the max height when box i is on top
		int maxHeight[] = new int[rotations.size()];
		// Initialize array to find the sequence of blocks that lead to max height (that
		// trace where is each block stacked on top of)
		int blockSequence[] = new int[rotations.size()];

		for (int i = 0; i < maxHeight.length; i++) {
			// Fill the max height with the height of each box
			maxHeight[i] = rotations.get(i).height;
			// Fill this table with i, since we are going to replace them with information
			// about the block underneath (another i location)
			blockSequence[i] = i;

		}

		// A block can be stacked on top of another block if and only if the two
		// dimensions on the base of the top block are smaller than the two dimensions
		// on the base of the lower block.
		for (int i = 1; i < maxHeight.length; i++) {
			for (int j = 0; j < i; j++) {
				if (rotations.get(i).length < rotations.get(j).length
						&& rotations.get(i).width < rotations.get(j).width) {
					// If the condition is met then we check if the the height we are going to add
					// is going to produce the max height
					if (maxHeight[j] + rotations.get(i).height > maxHeight[i]) {
						// We add it to the max height
						maxHeight[i] = maxHeight[j] + rotations.get(i).height;
						// We update our block sequence array in order to trace back the blocks we used
						// later
						blockSequence[i] = j;

					}
				}
			}
		}
		// Find the max height so we can return it
		int tmp = Arrays.stream(maxHeight).max().getAsInt();

		// Find the index of the max height in the max height array so that we can use
		// it in the block sequence array
		int index = findIndex(maxHeight, tmp);

		// use the getBlocks method to find the block sequence and write it on the
		// output file
		result = getBlocks(blockSequence, rotations, index, name);

		return tmp;
		// return max height

	}

	// block sequence array, rotations (all the blocks needed), index of max height,
	// the name is the name of the output file, I parse it through here because I
	// wanted the write method to be separate from the main class and in order to
	// avoid static (java related issues)
	public static List<Block> getBlocks(int[] arr, List<Block> ls, int index, String name) {
		// Creating the result arraylist (list with the blocks that produce the max
		// height
		ArrayList<Block> res = new ArrayList<>();
		int temp = 0;
		res.add(ls.get(index));

		// The block sequence has information about the next block (the block which is
		// below) we just follow that
		// We end our search either when we reach 0 or when index is the same for 2
		// times in the row
		while (index != 0 && index != temp) {
			temp = index;
			index = arr[index];
			res.add(ls.get(index));
		}
		// We reverse it, because that's how you want us to print em
		Collections.reverse(res);

		// I wonder if you even check these comments
		// This removes an edge case problem where the result list has 2 times the last
		// block
		if (index == temp)
			res.remove(0);

		try {
			// Parse to the writToFile method the resulting list and the name of output file
			writeToFile(res, name);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Print the size of the tower
		System.out.print("The tallest tower has " + res.size());

		// return it
		return res;
	}

	public static void writeToFile(ArrayList<Block> ls, String name) throws IOException {

		// Create the writer
		FileWriter writer = new FileWriter(name);
		// Write the size
		writer.write(String.valueOf(ls.size()));
		writer.write("\n");

		// Write the blocks
		for (Block str : ls) {
			writer.write(str + System.lineSeparator());
		}

		// Close the writer
		writer.close();
	}

	// I tried to use a generic built in binary search, turns out it just gives up
	// after a while with big arrays
	// This is a abstract layer introduced in Java 8, called Stream API. The
	// stream represents a sequence of objects from a source, which supports
	// aggregate operations. So basically you can do lambda operations on all the
	// elements
	// It was pretty cool learning that.
	public static int findIndex(int arr[], int t) {
		int len = arr.length;
		return IntStream.range(0, len).filter(i -> t == arr[i]).findFirst() // first occurrence
				.orElse(-1); // No element found
	}

}
