/**
 * 
 */
package pro.sort.method;

import java.util.Comparator;

/**
 * @author Nikolay
 *
 */
public interface ISorterMethod {

	public default <T> void sort(T[] array, int start, int end, Comparator<T> comparator) {
		sort(array, start, end, comparator, 0);
	};

	/**
	 * @param <T>
	 * @param array
	 * @param start
	 * @param end
	 * @param comparator
	 * @param i
	 */
	public <T> void sort(T[] array, int start, int end, Comparator<T> comparator, int i);
}
