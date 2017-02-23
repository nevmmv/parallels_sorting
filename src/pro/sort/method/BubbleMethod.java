package pro.sort.method;

import java.util.Comparator;

public class BubbleMethod implements ISorterMethod {

	public <T> void sort(T[] array, int start, int end, Comparator<T> comparator) {
		sort(array, start, end, comparator, 0);
	}

	public <T> void sort(T[] array, int start, int end, Comparator<T> comparator, int skip) {
		System.out.println("sortingBubbles:"+(start + skip + 1)+" => "+end+"\n");
		for (int i = start + skip + 1; i < end; i++) {
			for (int j = 1; j < (end - i); j++) {
				if (comparator.compare(array[i], array[j]) < 0) {
					T temp = array[i];
					array[i] = array[j];
					array[j] = temp;
				}
			}
		}
	}
}