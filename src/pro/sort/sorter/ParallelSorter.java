package pro.sort.sorter;

import java.util.Comparator;

import net.mokyu.threading.GameExecutor;
import net.mokyu.threading.SplitTask;
import net.mokyu.threading.TaskTree;
import net.mokyu.threading.TaskTreeBuilder;
import pro.sort.method.ISorterMethod;

public class ParallelSorter<T> {

	private int numChunks;
	private int numOverlaps;

	private TaskTree taskTree;

	private Chunk<T>[] chunks;
	private Overlap[] overlaps;

	private T[] array;
	private int sortStart, sortEnd;
	private Comparator<T> comparator;

	private ISorterMethod sorter;

	@SuppressWarnings("unchecked")
	public ParallelSorter(int numChunks, ISorterMethod sorter) {
		if (numChunks < 2) {
			throw new IllegalArgumentException("Minimum number of chunks is 2");
		}
		this.sorter = sorter;
		this.numChunks = numChunks;
		this.numOverlaps = numChunks - 1; // Potential number of overlaps is
											// numChunks minus 1.

		// Initialize numChunks chunks.
		chunks = new Chunk[numChunks];
		for (int i = 0; i < numChunks; i++) {
			chunks[i] = new Chunk<T>();
		}

		overlaps = new Overlap[numOverlaps];
		for (int i = 0; i < numOverlaps; i++) {
			overlaps[i] = new Overlap();
		}

		buildTaskTree();
	}

	private void buildTaskTree() {
		TaskTreeBuilder builder = new TaskTreeBuilder();

		// Create tasks.
		SplitTask sortChunksTask = new SortChunksTask(numChunks);
		SplitTask sortOverlapsTask = new SortOverlapsTask(numOverlaps);

		// Set up task dependencies
		sortOverlapsTask.addRequiredTask(sortChunksTask); // Sort overlaps AFTER
															// sorting chunks.

		// Add to builder and build the task tree.
		builder.addTask(sortChunksTask);
		builder.addTask(sortOverlapsTask);
		taskTree = builder.build();
	}

	public void sort(T[] array, int start, int end, Comparator<T> comparator, GameExecutor executor) {

		// Store important variables
		this.array = array;
		this.sortStart = start;
		this.sortEnd = end;
		this.comparator = comparator;

		// Initialize chunk objects with the bounds of each chunk.
		int length = end - start;
		for (int i = 0; i < numChunks; i++) {
			Chunk<T> c = chunks[i];
			c.start = start + (length / numChunks) * i;
			c.end = start + (length / numChunks) * (i + 1);
		}
		chunks[numChunks - 1].end = end; // Ensure we don't miss the last few
											// elements due to rounding

		/*
		 * Run the precomputed task tree for the sorting algorithm. This is the
		 * equivalent of a multithreaded version of the following code:
		 * 
		 * for(int i = 0; i < numChunks; i++){ sortChunk(i); } updateOverlaps();
		 * for(int i = 0; i < numOverlaps; i++){ sortOverlaps(i); }
		 * 
		 */
		executor.run(taskTree);

		// DEBUGGING: Make sure the array is properly sorted
		/*
		 * for(int i = sortStart + 1; i < sortEnd; i++){
		 * if(comparator.compare(array[i-1], array[i]) > 0){ System.err.println(
		 * "error at index " + i + ": " + array[i-1] + " vs " + array[i]);
		 * break; } }
		 */
	}

	private void sortChunk(int chunkID) {
		Chunk<T> c = chunks[chunkID];
		sorter.sort(array, c.start, c.end, comparator);
		c.first = array[c.start];
		c.last = array[c.end - 1];
	}

	private void updateOverlaps() {
		// int totalOverlappingElements = 0; //Debugging value

		int overlapCount = 0;

		for (int i = 0; i < numChunks;) {

			Chunk<T> current = chunks[i];
			T highest = current.last; // Used to detect overlapping chunks

			int overlapIndex = i;

			for (int j = i + 1; j < numChunks; j++) {
				Chunk<T> c = chunks[j];
				if (comparator.compare(highest, c.first) > 0) {

					// Update the highest value to include all previous chunks
					// and increase overlapIndex to j.
					for (; overlapIndex < j; overlapIndex++) {
						Chunk<T> overlapChunk = chunks[overlapIndex];
						if (comparator.compare(highest, overlapChunk.last) < 0) {
							highest = overlapChunk.last;
						}
					}

				}
			}

			if (overlapIndex == i) {
				// No overlap occured. Skip this chunk.
				i++;
				continue;
			}

			// Initialize a Overlap object for this overlap and increase the
			// overlap count
			Overlap overlap = overlaps[overlapCount++];
			overlap.enabled = true;
			overlap.firstChunkID = i;
			overlap.lastChunkID = overlapIndex;

			// Skip all chunks affected by this overlap
			i = overlapIndex;
		}

		// Disable all unused Overlap objects:
		for (int i = overlapCount; i < numOverlaps; i++) {
			overlaps[i].enabled = false;
		}
	}

	private void sortOverlap(int overlapID) {

		Overlap overlap = overlaps[overlapID];

		// Quickly return if this overlap is disabled.
		if (!overlap.enabled) {
			return;
		}

		// Extract some data from the Overlap object
		int firstChunkID = overlap.firstChunkID;
		int lastChunkID = overlap.lastChunkID;
		Chunk<T> firstChunk = chunks[firstChunkID];
		Chunk<T> lastChunk = chunks[lastChunkID];

		// Compute value of upper and lower bounds for the overlap.
		T highest = firstChunk.last;
		T lowest = firstChunk.last;
		for (int j = firstChunkID + 1; j <= lastChunkID; j++) {
			Chunk<T> c = chunks[j];
			if (j < lastChunkID && comparator.compare(highest, c.last) < 0) {
				highest = c.last;
			}
			if (comparator.compare(lowest, c.first) > 0) {
				lowest = c.first;
			}
		}

		// Find array indices of upper and lower bounds.
		int start = firstChunk.end - 1;
		int end = lastChunk.start;

		// TODO: replace with binary search in firstChunk instead of linear
		// search.
		while (start > sortStart && comparator.compare(array[start], lowest) > 0) {
			start--;
		}
		// TODO: replace with binary search in lastChunk instead of linear
		// search.
		while (end < sortEnd && comparator.compare(array[end], highest) < 0) {
			end++;
		}

		// And finally, sort the overlap. Note that there is no need to sort
		// the first few elements that are in the first chunk, since these
		// are already sorted. The last argument is a skip argument which
		// allows us to simply skip sorting those elements but still allow
		// the insertion sort to shuffle in elements into that part of the
		// array.
		sorter.sort(array, start, end, comparator, firstChunk.end - 1 - start);
	}

	private static class Chunk<T> {
		private int start, end;
		private T first, last;
	}

	private static class Overlap {
		private boolean enabled;
		private int firstChunkID, lastChunkID;
	}

	private class SortChunksTask extends SplitTask {

		public SortChunksTask(int numChunks) {
			super(0, 0, numChunks);
		}

		@Override
		protected void runSubtask(int subtask) {
			sortChunk(subtask);
		}

		@Override
		public void finish() {
			updateOverlaps();
		}
	}

	private class SortOverlapsTask extends SplitTask {

		public SortOverlapsTask(int numOverlaps) {
			super(1, 0, numOverlaps);
		}

		@Override
		protected void runSubtask(int subtask) {
			sortOverlap(subtask);
		}

		@Override
		public void finish() {
		}
	}
}