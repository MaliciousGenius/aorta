// ----------------------------------------------------------------------------
// Copyright (C) 2012 Louise A. Dennis, and  Michael Fisher 
//
// This file is part of Agent JPF (AJPF)
// 
// AJPF is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
// 
// AJPF is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with AJPF; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.csc.liv.ac.uk/~lad
//
//----------------------------------------------------------------------------

package ajpf.util;

import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collection;

public class VerifyMap<K extends Comparable<? super K>, V> implements Map<K, V> {
	ArrayList<Tuple<K, V>> tuplearray = new ArrayList<Tuple<K, V>>();
	
	public int size() {
		return tuplearray.size();
	}
	
	public boolean isEmpty() {
		return tuplearray.isEmpty();
	}
	
	public boolean containsKey(Object key) {
		for (Tuple<K, V> t: tuplearray) {
			if (t.matches(key)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsValue(Object value) {
		for (Tuple<K, V> t: tuplearray) {
			if (t.valueMatch(value)) {
				return true;
			}
		}
		return false;
	}
	
	public V get(Object key) {
		for (Tuple<K, V> t: tuplearray) {
			if (t.matches(key)) {
				return t.getValue();
			}
		}
		return null;
	}
	
	public V put(K key, V value) {
		V out = insert(new Tuple<K, V>(key, value));
		return out;
	}
	
/*	private K getKey(V value) {
		for (Tuple<K, V> t: tuplearray) {
			if (t.valueMatch(value)) {
				return t.getKey();
			}
		}
		return null;		
	} */
	
	public V remove(Object key) {
		if (containsKey(key)) {
			for (Tuple<K, V> tuple: tuplearray) {
				if (tuple.getKey().equals(key)) {
					tuplearray.remove(tuple);
					return tuple.getValue();
				}
			}
			
		//	V value = get(key);
		//	Tuple<K, V> tuple = new Tuple<K, V>(key, value);
		//	tuplearray.remove(tuple);
		//	return value;
		} 
		return null;
	}
	
	public void putAll(Map<? extends K, ? extends V> m) {
		for (K key: m.keySet()) {
			put(key, m.get(key));
		}
	}
		
	public void clear() {
		tuplearray.clear();
	}
	
	public Set<K> keySet() {
		TreeSet<K> set = new TreeSet<K>();
		for (Tuple<K, V> t: tuplearray) {
			set.add(t.getKey());
		}
		return set;
	}
	
	public Collection<V> values() {
		ArrayList<V> values = new ArrayList<V>();
		for (Tuple<K, V> t: tuplearray) {
			values.add(t.getValue());
		}
		return values;
	}
	
	public Set<Map.Entry<K, V>> entrySet() {
		TreeSet<Map.Entry<K, V>> set = new TreeSet<Map.Entry<K, V>>();
		for (Tuple<K, V> t: tuplearray) {
			set.add(t);
		}
		return set;		
	}
	
	public int hashCode() {
		int h = 0;
		for (Tuple<K, V> t: tuplearray) {
			h += t.hashCode();
		}	
		return h;
	}
	
	
	private V insert(Tuple<K, V> t) {
		for (int i = 0; i < tuplearray.size(); i++) {
			Tuple<K, V> t1 = tuplearray.get(i);
			int comparison = t.getKey().compareTo(t1.getKey());
			if (comparison < 0) {
				tuplearray.add(i, t);
				return null;
			} else if (comparison == 0) {
				V returnvalue = t1.getValue();
				tuplearray.remove(i);
				tuplearray.add(i, t);
				return returnvalue;
			}
		}
		
		tuplearray.add(t);
		return null;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Map<?, ?>) {
			return ((Map<?, ?>) o).entrySet().equals(entrySet());
		}
		return false;
	}

	class Tuple<K1 extends Comparable<? super K1>, V1> implements Comparable<Tuple<K1, V1>>, Map.Entry<K1, V1> {
		K1 key;
		V1 value;
		
		public Tuple (K1 k, V1 v) {
			key = k;
			value = v;
		}
		
		public K1 getKey() {
			return key;
		}
		
		public V1 getValue() {
			return value;
		}
		
		public boolean matches(Object k) {
			return (key == null ? k == null : key.equals(k) );
		}
		
		public boolean valueMatch(Object v) {
			return (value == null ? v == null : value.equals(v));
		}
				
		public int compareTo(Tuple<K1, V1> t) {
			return t.getKey().compareTo(getKey());
		}
		
		public String toString() {
			String s = key.toString() + "-" + value.toString();
			return s;
		}
		
		public int hashCode() {
			return key.hashCode() + value.hashCode()*7;
		}
		
		
		public V1 setValue(V1 v) {
			V1 oldvalue = value;
			value = v;
			return oldvalue;
		}
		
		public boolean equals( Object other) { 
		    if (this == other) return true; 
		    if (other == null) return false;  
		    if (this.getClass() != other.getClass()) return false; 
		    Tuple<?, ?> otherTuple = (Tuple<?, ?>) other; 
		    return (this.key.equals(otherTuple.key)  
		         && this.value.equals(otherTuple.value)); 
		  } 

	}
	
	public String toString() {
		return tuplearray.toString();
	}
} ///:~