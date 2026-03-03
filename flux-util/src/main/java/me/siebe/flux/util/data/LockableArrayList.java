package me.siebe.flux.util.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * This is an {@link ArrayList} that can be locked to allow/prevent updates to the list.
 *
 * @param <E> the type of elements in this list
 */
public class LockableArrayList<E> extends ArrayList<E> {
    private boolean locked = false;

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    @Override
    public boolean add(E e) {
        if (locked) throw new IllegalStateException("Cannot add to locked list");
        return super.add(e);
    }
    @Override
    public void add(int index, E element) {
        if (locked) throw new IllegalStateException("Cannot add to locked list");
        super.add(index, element);
    }
    @Override
    public void addFirst(E element) {
        if (locked) throw new IllegalStateException("Cannot add to locked list");
        super.addFirst(element);
    }
    @Override
    public void addLast(E element) {
        if (locked) throw new IllegalStateException("Cannot add to locked list");
        super.addLast(element);
    }
    @Override
    public E remove(int index) {
        if (locked) throw new IllegalStateException("Cannot remove from locked list");
        return super.remove(index);
    }
    @Override
    public E removeFirst() {
        if (locked) throw new IllegalStateException("Cannot remove from locked list");
        return super.removeFirst();
    }
    @Override
    public E removeLast() {
        if (locked) throw new IllegalStateException("Cannot remove from locked list");
        return super.removeLast();
    }
    @Override
    public boolean remove(Object o) {
        if (locked) throw new IllegalStateException("Cannot remove from locked list");
        return super.remove(o);
    }
    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (locked) throw new IllegalStateException("Cannot add to locked list");
        return super.addAll(c);
    }
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        if (locked) throw new IllegalStateException("Cannot add to locked list");
        return super.addAll(index, c);
    }
    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        if (locked) throw new IllegalStateException("Cannot remove from locked list");
        super.removeRange(fromIndex, toIndex);
    }
    @Override
    public boolean removeAll(Collection<?> c) {
        if (locked) throw new IllegalStateException("Cannot remove from locked list");
        return super.removeAll(c);
    }
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        if (locked) throw new IllegalStateException("Cannot remove from locked list");
        return super.removeIf(filter);
    }
}
