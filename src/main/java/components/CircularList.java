package components;

import java.util.AbstractList;
import java.util.List;
 
/**
 * 返回一个以目标数组为循环基础，以pos为起始的数组，该结构只存储结构不存储数据
 */
public class CircularList<E> extends AbstractList<E> {
    private final List<E> elements;
    private int startIndex;

  
    public CircularList(List<E> elements, int startIndex) {
        this.elements = elements;
        this.startIndex = startIndex;
    }

 

    @Override
    public E get(int index) {
        if (index < 0 || index >= elements.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + elements.size());
        }
        return elements.get((startIndex + index) % elements.size());
    }

    @Override
    public int size() {
        return elements.size();
    }

 
 
 
}