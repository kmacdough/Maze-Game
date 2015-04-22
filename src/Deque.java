import tester.Tester;

// examples and tests for dequeue
class ExamplesDequeue {
    // NOTE: initialized examples outside and inside initializeDeque method
    //       since I wasn't sure if WebCat wanted them initialized outside
    
    // empty dequeue
    Sentinel<String> sentinel1 = new Sentinel<>(); // needed in some tests
    Dequeue<String> dequeue1 = new Dequeue<>(sentinel1);
    
    // dequeue with "abc", "bcd", "cde", "def"
    Sentinel<String> sentinel2 = new Sentinel<>();
    ANode<String> abcNode = new Node<>("abc", sentinel2, sentinel2);
    ANode<String> bcdNode = new Node<>("bcd", abcNode, sentinel2);
    ANode<String> cdeNode = new Node<>("cde", bcdNode, sentinel2);
    ANode<String> defNode = new Node<>("def", cdeNode, sentinel2);
    Dequeue<String> dequeue2 = new Dequeue<>(sentinel2);
    
    // dequeue2 with first removed
    Sentinel<String> sentinel2A;
    ANode<String> bcdNodeA;
    ANode<String> cdeNodeA;
    ANode<String> defNodeA;
    Dequeue<String> dequeue2A;
    
    // dequeue with "Peter", "Teixeira", "Kevin", "McDonough", "Lerner"
    Sentinel<String> sentinel3 = new Sentinel<>();
    ANode<String> peterNode = new Node<>("Peter", sentinel3, sentinel3);
    ANode<String> teixNode = new Node<>("Teixeira", peterNode, sentinel3);
    ANode<String> kevinNode = new Node<>("Kevin", teixNode, sentinel3);
    ANode<String> mcdNode = new Node<>("McDonough", kevinNode, sentinel3);
    ANode<String> lernerNode = new Node<>("Lerner", mcdNode, sentinel3);
    Dequeue<String> dequeue3 = new Dequeue<>(sentinel3);
    
    // dequeue3 with last removed
    Sentinel<String> sentinel3B;
    ANode<String> peterNodeB;
    ANode<String> teixNodeB;
    ANode<String> kevinNodeB;
    ANode<String> mcdNodeB;
    Dequeue<String> dequeue3B;
    
    // dequeue3 with teixNode removed
    Sentinel<String> sentinel3C;
    ANode<String> peterNodeC;
    ANode<String> kevinNodeC;
    ANode<String> mcdNodeC;
    ANode<String> lernerNodeC;
    Dequeue<String> dequeue3C;
    
    // dequeue with "NCAA"
    Sentinel<String> ncaaSent;
    ANode<String> ncaaNode;
    Dequeue<String> ncaaDeq;
    
    // initialization of tests
    void initializeDequeue() {
        dequeue1 = new Dequeue<>();
        
        sentinel2 = new Sentinel<>();
        abcNode = new Node<>("abc", sentinel2, sentinel2);
        bcdNode = new Node<>("bcd", abcNode, sentinel2);
        cdeNode = new Node<>("cde", bcdNode, sentinel2);
        defNode = new Node<>("def", cdeNode, sentinel2);
        dequeue2 = new Dequeue<>(sentinel2);
        
        // dequeue2 with first removed
        sentinel2A = new Sentinel<>();
        bcdNodeA = new Node<>("bcd", sentinel2A, sentinel2A);
        cdeNodeA = new Node<>("cde", bcdNodeA, sentinel2A);
        defNodeA = new Node<>("def", cdeNodeA, sentinel2A);
        dequeue2A = new Dequeue<>(sentinel2A);
        
        sentinel3 = new Sentinel<>();
        peterNode = new Node<>("Peter", sentinel3, sentinel3);
        teixNode = new Node<>("Teixeira", peterNode, sentinel3);
        kevinNode = new Node<>("Kevin", teixNode, sentinel3);
        mcdNode = new Node<>("McDonough", kevinNode, sentinel3);
        lernerNode = new Node<>("Lerner", mcdNode, sentinel3);
        dequeue3 = new Dequeue<>(sentinel3);
        
        // dequeue3 with last removed
        sentinel3B = new Sentinel<>();
        peterNodeB = new Node<>("Peter", sentinel3B, sentinel3B);
        teixNodeB = new Node<>("Teixeira", peterNodeB, sentinel3B);
        kevinNodeB = new Node<>("Kevin", teixNodeB, sentinel3B);
        mcdNodeB = new Node<>("McDonough", kevinNodeB, sentinel3B);
        dequeue3B = new Dequeue<>(sentinel3B);
        
        // dequeue3 with teixNode removed
        sentinel3C = new Sentinel<>();
        peterNodeC = new Node<>("Peter", sentinel3C, sentinel3C);
        kevinNodeC = new Node<>("Kevin", peterNodeC, sentinel3C);
        mcdNodeC = new Node<>("McDonough", kevinNodeC, sentinel3C);
        lernerNodeC = new Node<>("Lerner", mcdNodeC, sentinel3C);
        dequeue3C = new Dequeue<>(sentinel3C);
        
        ncaaSent = new Sentinel<>();
        ncaaNode = new Node<>("NCAA", ncaaSent, ncaaSent);
        ncaaDeq = new Dequeue<>(ncaaSent);
    }
    

    
    ///////////////////////////////////////////////////////////////////////////
    // Tests for Dequeue class
    
    // test size method on Dequeue class
    void testSizeDequeue(Tester t) {
        initializeDequeue();
        t.checkExpect(this.dequeue1.size(), 0);
        t.checkExpect(this.dequeue2.size(), 4);
        t.checkExpect(this.dequeue3.size(), 5);
    }
    
    // test addAtHead method on Dequeue class
    void testAddAtHead(Tester t) {
        initializeDequeue();
        this.dequeue1.addAtHead("NCAA");
        t.checkExpect(dequeue1, ncaaDeq);
        
        initializeDequeue();
        this.dequeue1.addAtHead("def");
        this.dequeue1.addAtHead("cde");
        this.dequeue1.addAtHead("bcd");
        this.dequeue1.addAtHead("abc");
        t.checkExpect(dequeue1, dequeue2);
        
        initializeDequeue();
        this.dequeue2A.addAtHead("abc");
        t.checkExpect(dequeue2A, dequeue2);
        
        initializeDequeue();
        this.dequeue1.addAtHead("Lerner");
        this.dequeue1.addAtHead("McDonough");
        this.dequeue1.addAtHead("Kevin");
        this.dequeue1.addAtHead("Teixeira");
        this.dequeue1.addAtHead("Peter");
        t.checkExpect(dequeue1, dequeue3);
    }
    
    // test addAtTail method on Dequeue class
    void testAddAtTail(Tester t) {
        initializeDequeue();
        this.dequeue1.addAtTail("NCAA");
        t.checkExpect(dequeue1, ncaaDeq);
        
        initializeDequeue();
        this.dequeue1.addAtTail("abc");
        this.dequeue1.addAtTail("bcd");
        this.dequeue1.addAtTail("cde");
        this.dequeue1.addAtTail("def");
        t.checkExpect(dequeue1, dequeue2);
        
        initializeDequeue();
        this.dequeue1.addAtTail("Peter");
        this.dequeue1.addAtTail("Teixeira");
        this.dequeue1.addAtTail("Kevin");
        this.dequeue1.addAtTail("McDonough");
        this.dequeue1.addAtTail("Lerner");
        t.checkExpect(dequeue1, dequeue3);
        
        initializeDequeue();
        this.dequeue3B.addAtTail("Lerner");
        t.checkExpect(dequeue3B, dequeue3);
    }
    
    // test removeFromHead method on Dequeue class
    void testRemoveFromHead(Tester t) {
        initializeDequeue();
        t.checkException(new RuntimeException(
                "Attempted to remove a node from an empty dequeue"),
                dequeue1, "removeFromHead");
        
        ncaaDeq.removeFromHead();
        t.checkExpect(ncaaDeq, dequeue1);

        initializeDequeue();
        dequeue2.removeFromHead();
        t.checkExpect(dequeue2, dequeue2A);
        
        initializeDequeue();
        dequeue2.removeFromHead();
        dequeue2.removeFromHead();
        dequeue2.removeFromHead();
        dequeue2.removeFromHead();
        t.checkExpect(dequeue2, dequeue1);

        initializeDequeue();
        dequeue3.removeFromHead();
        dequeue3.removeFromHead();
        dequeue3.removeFromHead();
        dequeue3.removeFromHead();
        dequeue3.removeFromHead();
        t.checkExpect(dequeue3, dequeue1);
    }
    
    // test removeFromTail method on Dequeue class
    void testRemoveFromTail(Tester t) {
        initializeDequeue();
        t.checkException(new RuntimeException(
                "Attempted to remove a node from an empty dequeue"),
                dequeue1, "removeFromTail");
        
        ncaaDeq.removeFromTail();
        t.checkExpect(ncaaDeq, dequeue1);

        initializeDequeue();
        dequeue3.removeFromTail();
        t.checkExpect(dequeue3, dequeue3B);
        
        initializeDequeue();
        dequeue2.removeFromTail();
        dequeue2.removeFromTail();
        dequeue2.removeFromTail();
        dequeue2.removeFromTail();
        t.checkExpect(dequeue2, dequeue1);

        initializeDequeue();
        dequeue3.removeFromTail();
        dequeue3.removeFromTail();
        dequeue3.removeFromTail();
        dequeue3.removeFromTail();
        dequeue3.removeFromTail();
        t.checkExpect(dequeue3, dequeue1);
    }
    
    // test the find method on Dequeue
    void testFindDequeue(Tester t) {
        initializeDequeue();
        t.checkExpect(dequeue1.find(new MatchStringPred("hi")), sentinel1);
        t.checkExpect(dequeue2.find(new MatchStringPred("hi")), sentinel2);
        t.checkExpect(dequeue2.find(new MatchStringPred("abc")), abcNode);
        t.checkExpect(dequeue3.find(new MatchStringPred("abc")), sentinel3);
        t.checkExpect(dequeue3.find(new MatchStringPred("Lerner")), lernerNode);
    }
    
    // test the removeNode method on Dequeue
    void testRemoveNode(Tester t) {
        initializeDequeue();
        ncaaDeq.removeNode(ncaaNode);
        t.checkExpect(ncaaDeq, dequeue1);
        
        // using truth of above test to test that dequeue remains
        // unaltered by removing its sentinel
        initializeDequeue();
        ncaaDeq.removeNode(ncaaNode);
        dequeue1.removeNode(sentinel1);
        t.checkExpect(dequeue1, ncaaDeq);

        initializeDequeue();
        dequeue2.removeNode(abcNode);
        t.checkExpect(dequeue2, dequeue2A);

        initializeDequeue();
        dequeue3.removeNode(lernerNode);
        t.checkExpect(dequeue3, dequeue3B);
        
        initializeDequeue();
        dequeue3.removeNode(teixNode);
        t.checkExpect(dequeue3, dequeue3C);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Tests for ANode class
    
    // test size method on ANode
    void testSizeANode(Tester t) {
        initializeDequeue();
        t.checkExpect(sentinel1.size(), 0);
        t.checkExpect(sentinel3.size(), 0);
        t.checkExpect(abcNode.size(), 4);
        t.checkExpect(defNode.size(), 1);
        t.checkExpect(kevinNode.size(), 3);
    }
    
    // test removeSelf method on ANode
    void testRemoveSelf(Tester t) {
        initializeDequeue();
        sentinel1.removeSelf();
        t.checkExpect(sentinel1, sentinel1);
        

        initializeDequeue();
        sentinel2.removeSelf();
        t.checkExpect(sentinel2, sentinel2);

        initializeDequeue();
        abcNode.removeSelf();
        t.checkExpect(sentinel2, sentinel2A);
        t.checkExpect(bcdNode, bcdNodeA);

        initializeDequeue();
        lernerNode.removeSelf();
        t.checkExpect(mcdNode, mcdNodeB);
        t.checkExpect(sentinel3, sentinel3B);
    }
    
    // test find method on ANode
    void testFindANode(Tester t) {
        initializeDequeue();
        t.checkExpect(sentinel1.find(new MatchStringPred("hi")), sentinel1);
        t.checkExpect(sentinel2.find(new MatchStringPred("hi")), sentinel2);
        t.checkExpect(abcNode.find(new MatchStringPred("hi")), sentinel2);
        t.checkExpect(abcNode.find(new MatchStringPred("abc")), abcNode);
        t.checkExpect(bcdNode.find(new MatchStringPred("abc")), sentinel2);
        t.checkExpect(sentinel3.find(new MatchStringPred("Lerner")), sentinel3);
        t.checkExpect(teixNode.find(new MatchStringPred("Lerner")), lernerNode);
    }
}

// to represent a predicate
interface IPred<T> {
    // apply this predicate to the given T
    boolean apply(T t);
}

// a predicate checking if given string matches reference string
class MatchStringPred implements IPred<String> {
    String ref;
    
    MatchStringPred(String ref) {
        this.ref = ref;
    }

    // does 
    public boolean apply(String given) {
        return ref.equals(given);
    }
}

// to represent a dequeue
class Dequeue<T> {
    Sentinel<T> header;
    
    // Standard constructor
    Dequeue() {
        this(new Sentinel<T>());
    }
    
    // Convenience constructor taking a particular header
    Dequeue(Sentinel<T> header) {
        this.header = header;
    }
    
    // count the number of nodes in this dequeue (not including header)
    int size() {
        return this.header.next.size();
    }
    
    // EFFECT: modifies the prev and next nodes in the list to insert a
    //   node with the given data at the head of this dequeue
    void addAtHead(T data) {
        new Node<T>(data, header, header.next);
    }
    
    // EFFECT: modifies the prev and next nodes in the list to insert a
    //   node with the given data at the tail of this dequeue
    void addAtTail(T data) {
        new Node<T>(data, header.prev, header);
    }
    
    // EFFECT: modifies the prev and next nodes in the list to remove the
    //   node at the head of this dequeue
    void removeFromHead() {
        if(this.size() == 0) {
            throw new RuntimeException("Attempted to remove a node from an empty dequeue");
        }
        else {
            header.next.removeSelf();
        }
    }
    
    // EFFECT: modifies the prev and next fields of nodes in this dequeue
    //   to remove the node at the tail of this dequeue
    void removeFromTail() {
        if(this.size() == 0) {
            throw new RuntimeException("Attempted to remove a node from an empty dequeue");
        }
        else {
            header.prev.removeSelf();
        }
    }
    
    // find the first node in this dequeue matching the given predicate
    ANode<T> find(IPred<T> pred) {
        return header.next.find(pred);
    }
    
    // EFFECT: modifies prev and next fields of the next and prev fields
    //    of given node to remove it from list
    void removeNode(ANode<T> node) {
        node.removeSelf();
    }
}

// to represent an abstract node
abstract class ANode<T> {
    ANode<T> next;
    ANode<T> prev;
    
    // returns the number of nodes in this chain before a sentinel
    abstract int size();
    
    // EFFECT: if this is a Node, modify this node's prev's next and
    //   next's prev to remove this node from the chain, else do nothing
    abstract T removeSelf();

    // find the first node in this dequeue matching the given predicate
    abstract ANode<T> find(IPred<T> pred);
}

// to represent a sentinel
class Sentinel<T> extends ANode<T> {
    // Default constructor, pointing to itself
    Sentinel() {
        this.next = this;
        this.prev = this;
    }

    // returns the number of nodes in this chain before a sentinel
    int size() {
        return 0;
    }

    // do nothing, since a sentinel cannot be removed
    T removeSelf() {
        return null;
    }

    // find the first node in this dequeue matching the given predicate
    ANode<T> find(IPred<T> pred) {
        return this;
    }
}

// to represent a node
class Node<T> extends ANode<T> {
    T data;
    
    // Default constructor, null prev and next nodes
    Node(T data) {
        this.data = data;
        this.next = null;
        this.prev = null;
    }
    
    // Convenience constructor, constructs a node with given prev and next nodes
    Node(T data, ANode<T> prev, ANode<T> next) {
        this.data = data;
        
        if(prev == null || next == null) {
            throw new IllegalArgumentException("Null arguments not allowed");
        }
        else {
            this.prev = prev;
            this.prev.next = this;
            
            this.next = next;
            this.next.prev = this;
        }
    }

    // returns the number of nodes in this chain before a sentinel
    int size() {
        return 1 + next.size();
    }

    // EFFECT: if this is a Node, modify this node's prev's next and
    //   next's prev to remove this node from the chain, else do nothing
    T removeSelf() {
        this.prev.next = this.next;
        this.next.prev = this.prev;
        return this.data;
    }

    // find the first node in this dequeue matching the given predicate
    ANode<T> find(IPred<T> pred) {
        if(pred.apply(this.data)) {
            return this;
        } else {
            return this.next.find(pred);
        }
    }
    
    
}