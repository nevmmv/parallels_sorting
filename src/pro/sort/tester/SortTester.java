package pro.sort.tester;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import net.mokyu.threading.MultithreadedExecutor;
import pro.sort.method.BubbleMethod;
import pro.sort.method.ISorterMethod;
import pro.sort.method.InsertionMethod;
import pro.sort.sorter.ParallelSorter;

public class SortTester {

	private static final int numElements = 1_000_000;
	private static final int numThreads = 10;
	private static final int numChunks =  4;
	private static final int shuffleDisplacement = 1;

	public static void main(String[] args) {

		MultithreadedExecutor executor = new MultithreadedExecutor(numThreads);
		ISorterMethod insertionMethod = new InsertionMethod();
		ISorterMethod bubbleMethod = new BubbleMethod();
		
		ParallelSorter<Element> parallelInsertionSort = new ParallelSorter<>(numChunks, insertionMethod);

		ParallelSorter<Element> parallelBubbleSort = new ParallelSorter<>(numChunks, bubbleMethod);

		Element[] array1 = new Element[numElements];
		Element[] array2 = new Element[numElements];
		Element[] array3 = new Element[numElements];
		Element[] array4 = new Element[numElements];
		Element[] array5 = new Element[numElements];
		Element[] array6 = new Element[numElements];

		Comparator<Element> comparator = new Comparator<Element>() {
			public int compare(Element e1, Element e2) {
				return Integer.compare(e1.value, e2.value);
			}
		};

		System.out.println("Generating values...");
		Random r = new Random();
		for (int i = 0; i < numElements; i++) {
			array1[i] = new Element(r.nextInt());
		}
		System.out.println("Sorting values...");
		Arrays.sort(array1, 0, numElements, comparator);

		System.out.println("Copying values to other arrays...");
		for (int i = 0; i < numElements; i++) {
			Element e = array1[i];
			array2[i] = e;
			array3[i] = e;
			array4[i] = e;
			array5[i] = e;
			array6[i] = e;
		}

		System.out.println("Done.");

		int a = 0;
		while (a < 1) {
			a++;
			shuffle(array1);
			shuffle(array2);
			shuffle(array3);
			shuffle(array4);
			shuffle(array5);
			shuffle(array6);

			long startTime;

			/*
			 * startTime = System.nanoTime(); Arrays.sort(array1, 0,
			 * numElements, comparator); System.out.println(
			 * "Java sort unsorted: " + (System.nanoTime() - startTime) / 1000 /
			 * 1000f + " ms");
			 * 
			 * startTime = System.nanoTime(); Arrays.sort(array1, 0,
			 * numElements, comparator); System.out.println(
			 * "Java sort sorted:   " + (System.nanoTime() - startTime) / 1000 /
			 * 1000f + " ms");
			 * 
			 * startTime = System.nanoTime(); Arrays.parallelSort(array2, 0,
			 * numElements, comparator); System.out .println(
			 * "Java parallel sort unsorted: " + (System.nanoTime() - startTime)
			 * / 1000 / 1000f + " ms");
			 * 
			 * startTime = System.nanoTime(); Arrays.parallelSort(array2, 0,
			 * numElements, comparator); System.out .println(
			 * "Java parallel sort sorted:   " + (System.nanoTime() - startTime)
			 * / 1000 / 1000f + " ms");
			 */
/////////			
			startTime = System.nanoTime();
			insertionMethod.sort(array3, 0, numElements, comparator);
			System.out.println("Insertion sort unsorted: " + (System.nanoTime() - startTime) / 1000 / 1000f + " ms");

			startTime = System.nanoTime();
			insertionMethod.sort(array3, 0, numElements, comparator);
			System.out.println("Insertion sort sorted:   " + (System.nanoTime() - startTime) / 1000 / 1000f + " ms");
/////////
			startTime = System.nanoTime();
			parallelInsertionSort.sort(array4, 0, numElements, comparator, executor);
			System.out.println(
					"Parallel insertion sort unsorted: " + (System.nanoTime() - startTime) / 1000 / 1000f + " ms");

			startTime = System.nanoTime();
			parallelInsertionSort.sort(array4, 0, numElements, comparator, executor);
			System.out.println(
					"Parallel insertion sort sorted:   " + (System.nanoTime() - startTime) / 1000 / 1000f + " ms");
/////////
			startTime = System.nanoTime();
			parallelBubbleSort.sort(array5, 0, numElements, comparator, executor);
			System.out
					.println("Parallel bubble sort unsorted: " + (System.nanoTime() - startTime) / 1000 / 1000f + " ms");

			startTime = System.nanoTime();
			parallelBubbleSort.sort(array5, 0, numElements, comparator, executor);
			System.out
					.println("Parallel bubble sort sorted:   " + (System.nanoTime() - startTime) / 1000 / 1000f + " ms");
//////////
			startTime = System.nanoTime();
			bubbleMethod.sort(array6, 0, numElements, comparator);
			System.out.println("Bubble sort unsorted: " + (System.nanoTime() - startTime) / 1000 / 1000f + " ms");

			startTime = System.nanoTime();
			bubbleMethod.sort(array6, 0, numElements, comparator);
			System.out.println("Bubble sort sorted:   " + (System.nanoTime() - startTime) / 1000 / 1000f + " ms");

			
			// System.out.println(Arrays.toString(array1) + "\n" +
			// Arrays.toString(array5));
			System.out.println("First elements: \n" + array1[0] + ",\n" + array2[0] + ",\n" + array3[0] + ",\n"
					+ array4[0] + ",\n" + array5[0] + '\n');

		}

	}

	private static void shuffle(Object[] ar) {
		Random rnd = new Random();

		for (int i = shuffleDisplacement; i < ar.length; i++) {

			int index = i - rnd.nextInt(shuffleDisplacement);

			Object a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	private static class Element {

		private int value;

		public Element(int value) {
			this.value = value;
		}

		public String toString() {
			return value + "";
		}
	}
}