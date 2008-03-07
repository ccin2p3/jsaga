/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.wsrf.utils.cache;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Special purpose double linked list implementation. 
 * <b>Not for general use</b>
 */
public class LinkedNodeList {
    
    private Node head;
    private Node tail;
    private int size;

    public Node createNode() {
        return new Node();
    }

    public Node createNode(Object value) {
        return new Node(value);
    }

    /**
     * Removes node from the list and adds it to the end of the list.
     */
    public synchronized void moveToEnd(Node node) {
        if (node != this.tail) {
            remove(node);
            add(node);
        }
    }
    
    public int computeSize() {
        int i = 0;
        Node n = this.head;
        while( n != null ) {
            n = n.next;
            i++;
        }
        return i;
    }

    /**
     * When iterating must synchronize on the LinkedNodeList instance.
     */
    public Iterator reverseIterator() {
        return new NodeIterator(this.tail, false);
    }

    // --------- GENERAL LIST METHODS -------------

    /**
     * When iterating must synchronize on the LinkedNodeList instance.
     */
    public Iterator iterator() {
        return new NodeIterator(this.head, true);
    }

    public synchronized void clear() {
        this.head = this.tail = null;
    }

    public boolean isEmpty() {
        return (this.head == null);
    }

    public int size() {
        return this.size;
    }

    /**
     * Adds a node to the end of the list.
     */
    public synchronized void add(Node node) {
        if (this.tail == null) {
            if (this.head != null) {
                throw new RuntimeException();
            }
            this.tail = this.head = node;
            node.previous = node.next = null;
        } else {
            if (this.tail.next != null) {
                throw new RuntimeException();
            }
            Node oldTail = this.tail;
            Node newTail = node;
            
            oldTail.next = newTail;
            
            newTail.next = null;
            newTail.previous = oldTail;
                
            this.tail = newTail;
        }
        this.size++;
    }
    
    /**
     * Returns the first node in the list.
     */
    public Node getFirst() {
        return (this.head == null) ? null : this.head;
    }

    /**
     * Returns the last node in the list.
     */
    public Node getLast() {
        return (this.tail == null) ? null : this.tail;
    }

    /**
     * Removes and returns the first node in the list.
     */
    public synchronized Node removeFirst() {
        if (this.head == null) {
            return null;
        }
        Node oldHead = this.head;
        remove(this.head);
        return oldHead;
    }

    /**
     * Removes and returns the last node in the list.
     */
    public synchronized Node removeLast() {
        if (this.tail == null) {
            return null;
        }
        Node oldTail = this.tail;
        remove(this.tail);
        return oldTail;
    }
    
    /**
     * Removes the node from the list.
     */
    public synchronized void remove(Node node) {
        Node nextNode = node.next;
        Node prevNode = node.previous;
        
        if (nextNode == null && prevNode == null) {
            if (node != this.tail && node != this.head) {
                throw new RuntimeException();
            }
            this.head = this.tail = null;
        } else if (nextNode != null && prevNode != null) {
            prevNode.next = nextNode;
            nextNode.previous = prevNode;
        } else if (nextNode != null && prevNode == null) {
            if (node != this.head) {
                throw new RuntimeException();
            } 
            nextNode.previous = null;
            this.head = nextNode;
        } else if (nextNode == null && prevNode != null) {
            if (node != this.tail) {
                throw new RuntimeException();
            }
            prevNode.next = null;
            this.tail = prevNode;
        }
        this.size--;
        node.next = node.previous = null;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        int i = 0;
        Node n = this.head;
        if (n == null) {
            buf.append("<empty>");
        } else {
            while( n != null ) {
                buf.append("[" + i + "]: ").append(n).append("\r\n");
                n = n.next;
                i++;
            }
        }
        return buf.toString();
    }

    public static class Node {

        Node next;
        Node previous;
        Object value;

        public Node() {}

        public Node(Object value) {
            this.value = value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return this.value;
        }

        public Node getNext() {
            return this.next;
        }

        public Node getPrevious() {
            return this.previous;
        }
    }

    private static class NodeIterator implements Iterator {
        
        private Node currentNode;
        private boolean forward;
        
        public NodeIterator(Node startNode, boolean forward) {
            this.currentNode = startNode;
            this.forward = forward;
        }
        
        public boolean hasNext() {
            return (this.currentNode == null) ? false : true;
        }
        
        public Object next() {
            Node node = this.currentNode;
            if (node == null) {
                throw new NoSuchElementException();
            }
            if (this.forward) {
                this.currentNode = this.currentNode.next;
            } else {
                this.currentNode = this.currentNode.previous;
            }
            return node;
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
}

    
