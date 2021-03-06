package incubator.obscol;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * TODO: Missing documentation. Supported property: "size".
 * 
 * @param <E>
 */
public class WrapperObservableList<E> implements ObservableList<E>,
		Serializable {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Support for property listeners.
	 */
	private PropertyChangeSupport pcs;

	private final List<E> list;
	private final List<ObservableListListener<? super E>> listeners;

	public WrapperObservableList(List<E> list) {
		if (list == null) {
			throw new IllegalArgumentException("list == null");
		}

		this.list = list;
		this.pcs = new PropertyChangeSupport(this);
		listeners = new ArrayList<ObservableListListener<? super E>>();
	}

	@Override
	public synchronized void addObservableListListener(
			ObservableListListener<? super E> l) {
		if (l == null) {
			throw new IllegalArgumentException("l == null");
		}

		listeners.add(l);
	}

	@Override
	public synchronized void removeObservableListListener(
			ObservableListListener<? super E> l) {
		if (l == null) {
			throw new IllegalArgumentException("l == null");
		}

		listeners.remove(l);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		if (pcl == null) {
			throw new IllegalArgumentException("pcl == null");
		}

		pcs.addPropertyChangeListener(pcl);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		if (pcl == null) {
			throw new IllegalArgumentException("pcl == null");
		}

		pcs.removePropertyChangeListener(pcl);
	}

	private void fireElementAdded(E e, int idx) {
		for (ObservableListListener<? super E> l : new ArrayList<ObservableListListener<? super E>>(
				listeners)) {
			l.elementAdded(e, idx);
		}

		int sz = size();
		pcs.firePropertyChange("size", sz - 1, sz);
	}

	private void fireElementRemoved(E e, int idx) {
		for (ObservableListListener<? super E> l : new ArrayList<ObservableListListener<? super E>>(
				listeners)) {
			l.elementRemoved(e, idx);
		}

		int sz = size();
		pcs.firePropertyChange("size", sz + 1, sz);
	}

	private void fireElementChanged(E oldE, E newE, int idx) {
		for (ObservableListListener<? super E> l : new ArrayList<ObservableListListener<? super E>>(
				listeners)) {
			l.elementChanged(oldE, newE, idx);
		}
	}

	private void fireListCleared(int oldSize) {
		for (ObservableListListener<? super E> l : new ArrayList<ObservableListListener<? super E>>(
				listeners)) {
			l.listCleared();
		}

		if (oldSize > 0) {
			pcs.firePropertyChange("size", oldSize, 0);
		}
	}

	@Override
	public synchronized boolean add(E e) {
		list.add(e);

		fireElementAdded(e, list.size() - 1);

		return true;
	}

	@Override
	public synchronized void add(int index, E element) {
		list.add(index, element);
		fireElementAdded(element, index);
	}

	@Override
	public synchronized boolean addAll(Collection<? extends E> c) {
		if (c.size() == 0) {
			return false;
		}

		for (E e : c) {
			add(e);
		}

		return true;
	}

	@Override
	public synchronized boolean addAll(int index, Collection<? extends E> c) {
		if (c.size() == 0) {
			return false;
		}

		for (E e : c) {
			add(index, e);
			index++;
		}

		return true;
	}

	@Override
	public synchronized void clear() {
		int sz = size();
		list.clear();
		fireListCleared(sz);
	}

	@Override
	public synchronized boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public synchronized boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public synchronized E get(int index) {
		return list.get(index);
	}

	@Override
	public synchronized int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public synchronized boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public synchronized Iterator<E> iterator() {
		return list.iterator();
	}

	@Override
	public synchronized int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public synchronized ListIterator<E> listIterator() {
		return list.listIterator();
	}

	@Override
	public synchronized ListIterator<E> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public synchronized boolean remove(Object o) {
		@SuppressWarnings("unchecked")
		E e = (E) o;

		int idx = indexOf(o);
		if (idx == -1) {
			return false;
		}

		list.remove(idx);
		fireElementRemoved(e, idx);
		return true;
	}

	@Override
	public synchronized E remove(int index) {
		E e = list.remove(index);
		fireElementRemoved(e, index);
		return e;
	}

	@Override
	public synchronized boolean removeAll(Collection<?> c) {
		boolean rany = false;
		for (Object o : c) {
			rany |= remove(o);
		}

		return rany;
	}

	@Override
	public synchronized boolean retainAll(Collection<?> c) {
		boolean any = false;
		for (int i = 0; i < list.size(); i++) {
			if (!c.contains(get(i))) {
				remove(i);
				i--;
				any = true;
			}
		}

		return any;
	}

	@Override
	public synchronized E set(int index, E element) {
		E e = list.set(index, element);
		fireElementChanged(e, element, index);
		return e;
	}

	@Override
	public synchronized int size() {
		return list.size();
	}

	@Override
	public synchronized List<E> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	@Override
	public synchronized Object[] toArray() {
		return list.toArray();
	}

	@Override
	public synchronized <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}
}
