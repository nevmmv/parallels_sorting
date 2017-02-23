package pro.sort.method;
import java.util.Comparator;

public class InsertionMethod implements ISorterMethod{

   public <T> void sort(T[] array, int start, int end, Comparator<T> comparator){
      sort(array, start, end, comparator, 0);
   }
   
   public <T> void sort(T[] array, int start, int end, Comparator<T> comparator, int skip){
      System.out.println("sortingInsertions:"+(start + skip + 1)+" => "+end+"\n");
      for(int i = start + skip + 1; i < end; i++){

         T current = array[i];

         int j = i-1;
         if(comparator.compare(current, array[j]) < 0){
            do{
               array[j+1] = array[j];
               j--;
            }while(j >= start && comparator.compare(current, array[j]) < 0);
            array[j+1] = current;
         }
      }
   }
}