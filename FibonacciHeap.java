/**
 * FibonacciHeap
 * An implementation of fibonacci heap over integers.
 * Made by Nir Swartz and Sonia Moushaev as part of Data Structures course in TAU.
 */
public class FibonacciHeap
{
	private HeapNode min;
	private int size;
	private int trees;
	private int marked;
	private static int totalLinks=0; 
	private static int totalCuts=0; 
	private HeapNode first;
	
	public FibonacciHeap() { //builder
		this.min=null;
		this.size=0;
		this.trees=0;
		this.marked=0;
		this.first=null;
	}
	
   /**
    * public boolean isEmpty()
    *
    * precondition: none
    * 
    * The method returns true if and only if the heap
    * is empty.
    *   
    */
	public boolean isEmpty()
	{
		if (this.min == null) {
			return true;
		}
		return false;    
	}

   /**
    * public HeapNode insert(int key)
    *
    * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap. 
    */
    public HeapNode insert(int key)
    {    
    	HeapNode node=new HeapNode(key,0,false,null,null,null,null);  // Create new Binomial Tree with 0 degree
    	if(isEmpty()) { // If the Heap is empty
    		setMin(node);
    		node.setNext(node);
    		node.setPrev(node);
    	}
    	else {
    		if(first.getNext().equals(first)) { // We have only one HeapNode in the Heap
    			first.setNext(node);
    			first.setPrev(node);
    			node.setNext(first);
    			node.setPrev(first);
    		}
    		else {
    			node.setNext(first);
    			node.setPrev(first.getPrev());
    			first.getPrev().setNext(node);
    			first.setPrev(node);
    		}
    	}
    	if(key < min.getKey())
    		setMin(node);
    	setSize(1);
    	setNumTrees(1);
    	setFirst(node);
    	return node;
    }

   /**
    * public void deleteMin()
    *
    * Delete the node containing the minimum key.
    *
    */
    public void deleteMin()
    {
    	if(isEmpty()) {
    		return;
    	}
    	if(this.size == 1) { //there is only one key in heap and its the minimum
    		this.setMin(null);
    		this.setFirst(null);
    		this.setSize(-1);
    		setNumTrees(-1); //number of trees is changed
    		return;
    	}
    	HeapNode min = this.findMin();
    	if(min.getChild() != null) { //min node has children
    		childrenToRoots(min); //turn children into new tree roots in heap
    		setNumTrees(min.getRank() - 1); //number of trees is changed
    	}
    	else { //min node is a tree with rank 0
    		min.getPrev().setNext(min.getNext());
    		min.getNext().setPrev(min.getPrev());
    		setNumTrees(-1); //number of trees is changed
    	}
    	if(min.equals(this.first)) {
    		setFirst(this.first.getNext());
    	}
    	min.disconnecNode();
    	this.consolidate();
    	this.setSize(-1);
    }
   

   /**
    * public HeapNode findMin()
    *
    * Return the node of the heap whose key is minimal. 
    *
    */
    public HeapNode findMin()
    {
    	return this.min;
    } 
    
   /**
    * public void meld (FibonacciHeap heap2)
    *
    * Meld the heap with heap2
    *
    */
    public void meld (FibonacciHeap heap2)
    {
    	if(this.isEmpty() && heap2.isEmpty()) // both heaps are empty
    		return;
    	else if(this.isEmpty()) { // my Heap is empty, so switch to the not empty heap
    		this.min=heap2.findMin();
    		this.size=heap2.size();
    		this.trees=heap2.getNumTrees();
    		this.marked=heap2.getNumMarked();
    		this.first=heap2.first;
    	 }
    	else if(heap2.isEmpty()) { // Heap2 is empty
    		return;
    	}
    	else { //both heaps are not empty!
    		HeapNode mylast=this.first.getPrev(); //my heap last node
    		HeapNode otherlast=heap2.first.getPrev(); //my heap first node
    		mylast.setNext(heap2.first);
    		heap2.first.setPrev(mylast);
    		otherlast.setNext(this.first);
    		this.first.setPrev(otherlast);
    		
    		if(heap2.min.getKey() < this.min.getKey()) { //update min pointer
    			setMin(heap2.min);
    		}
    		setNumTrees(heap2.getNumTrees()); //update heap data
    		setNumMarked(heap2.getNumMarked());
    		setSize(heap2.size());
    	}
    }

   /**
    * public int size()
    *
    * Return the number of elements in the heap
    *   
    */
    public int size()
    {
    	return this.size;
    }
    	
    /**
    * public int[] countersRep()
    *
    * Return a counters array, where the value of the i-th entry is the number of trees of order i in the heap. 
    * 
    */
    public int[] countersRep()
    {
    	if(isEmpty()) {
    		return null;
    	}
    	int[] arr = new int[maxRank() + 1];
    	HeapNode node=this.min;
    	arr[node.getRank()]++;
    	node=node.getNext();
		while (!node.equals(this.min)){
			arr[node.getRank()]++;
			node=node.getNext();
		}
		return arr;
    }
	
   /**
    * public void delete(HeapNode x)
    *
    * Deletes the node x from the heap. 
    *
    */
    public void delete(HeapNode x) 
    {    
    	if(x==null) { //if x=null, don't do anything
    		return;
    	}
    	//We assume that if x!=null so x is in the heap. We can't check if x is in the heap at amortize O(log n)!
    	decreaseKey(x, x.getKey() - this.min.getKey() + 1);
    	deleteMin();
    }

   /**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * The function decreases the key of the node x by delta. The structure of the heap should be updated
    * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
    */
    public void decreaseKey(HeapNode x, int delta)
    {    
    	x.setKey(x.getKey() - delta);
    	if(!x.isRoot() ) { // if x is the root of the tree, we don't have any problems
    		if(x.getKey() < x.parent.getKey()) { // if the new key of x is smaller than is parent, the heap order is violation
    			cascadingCut(x);
    		}
    	}
    	if(x.getKey() < this.min.getKey()) //update min pointer
			setMin(x);
    }

   /**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * The potential equals to the number of trees in the heap plus twice the number of marked nodes in the heap. 
    */
    public int potential() //the potential function is #tress + 2#marked as defined in class
    {    
    	return getNumTrees() + 2 * getNumMarked();
    }

   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the run-time of the program.
    * A link operation is the operation which gets as input two trees of the same rank, and generates a tree of 
    * rank bigger by one, by hanging the tree which has larger value in its root on the tree which has smaller value 
    * in its root.
    */
    public static int totalLinks()
    {    
    	return totalLinks;
    }

    /**
     * public static int totalCuts() 
     *
     * This static function returns the total number of cut operations made during the run-time of the program.
     * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods). 
     */
    public static int totalCuts()
    {    
    	return totalCuts;
    }

    /**
     * public static int[] kMin(FibonacciHeap H, int k) 
     *
     * This static function returns the k minimal elements in a binomial tree H.
     * The function should run in O(k(logk + deg(H)). 
     */
    public static int[] kMin(FibonacciHeap H, int k)
    {        	
    	int[] arr = new int[k];
    	if(H.isEmpty() || k == 0) {
    		return arr;
    	}
    	arr[0] = H.findMin().getKey(); //insert root to array
    	int counter = 1;
    	FibonacciHeap tmpHeap = new FibonacciHeap();
    	HeapNode inserted = tmpHeap.insert(H.findMin().getKey()); //insert root into heap (key, pointer to node)
    	inserted.setInfo(H.findMin());
    	HeapNode child;
    	while(counter < k) { //root is already in array, counter runs from 1 to k-1 -> exactly k items in array
    		//add next level of minimums to heap
    		if(tmpHeap.findMin().getInfo().getChild() != null) {
    			child = tmpHeap.findMin().getInfo().getChild();
    			for(int i=0; i < tmpHeap.findMin().getInfo().getRank(); i++) { //run over all children of the minimal node and add them to tmpHeap
    				inserted = tmpHeap.insert(child.getKey()); //insert child into heap (key, pointer to node)
    				inserted.setInfo(child);
    				child = child.getNext();
    			}
    		}
    		tmpHeap.deleteMin();
    		arr[counter] = tmpHeap.findMin().getKey(); //add next minimun to array       	
    		counter++;
    	}
        return arr; 
    }
    
    
    public void setMin(HeapNode node) { // Update the min pointer
    	this.min=node;
    }
    
    public void setFirst(HeapNode node) { // Update the first pointer
    	this.first=node;
    }
    
    public void setSize(int delta) { // Change the size field by delta
    	this.size+=delta;
    }
    
    
    public void setNumTrees(int delta) { // Change the trees field by delta
    	this.trees+=delta;
    }
    
    public void setNumMarked(int delta) { // Change the marked field by delta
    	this.marked+=delta;
    }
    
    public void setTotalLinks(int delta) { // Change the totalLinks field by delta
    	totalLinks+=delta;
    }
    
    public void setTotalCuts(int delta) { // Change the totalCuts field by delta
    	totalCuts+=delta;
    }
    
    public int getNumTrees() {
    	return this.trees;
    }
    
    public int getNumMarked() {
    	return this.marked;
    }
    
    private int maxRank() {
    	return (int) Math.ceil(Math.log(this.size) / Math.log(2));
    }
    
    private void childrenToRoots(HeapNode min)  //turns node's children into new tree roots in heap
    {
    	HeapNode tmp = min.getChild();
    	while(tmp.getParent() != null) { //make all children's parent be null & if children are marked, unMark them
    		tmp.setParent(null);
    		if(tmp.mark) {
    			tmp.unMark();
    			setNumMarked(-1);
    		}
    		tmp = tmp.getNext();
    	}
    	//now we change pointers so the children would be the continuation of roots chain instead of min
    	if (min.getPrev().equals(min)) {
    		min.setNext(min.getChild());
    		min.setPrev(min.getChild().getPrev());
    	}
    	else {
    		min.getPrev().setNext(min.getChild());
    		min.getChild().getPrev().setNext(min.getNext());
    		min.getNext().setPrev(min.getChild().getPrev());
    		min.getChild().setPrev(min.getPrev());
    	}
    }
    
    
    private void consolidate() 
    {
    	int maxRank = this.maxRank();
    	HeapNode[] arr = new HeapNode[maxRank + 1];
    	toBuckets(arr);
    	this.setFirst(null);
    	this.setMin(null);
    	this.fromBuckets(arr);
    }
   
    private void insertFirst(HeapNode x)  //Attach node x to the first place on root list
    {
    	HeapNode mylast=this.first.getPrev();
    	if(mylast.getNext().equals(mylast)) { // We have only one HeapNode in newHeap
    		mylast.setNext(x);
    		mylast.setPrev(x);
			x.setNext(mylast);
			x.setPrev(mylast);
		}
		else { //we have more than one HeapNode in newHeap
			x.setNext(mylast.getNext());
			mylast.getNext().setPrev(x);
			mylast.setNext(x);
			x.setPrev(mylast);
		}
    	setFirst(x);
    }
    
    private void fromBuckets(HeapNode[] arr) //collect all new trees into a new heap
    {
    	for (int i = arr.length-1; i >= 0 ; i--) {
    		if(arr[i] != null) { //there is a tree of rank = i in the array
    			if(this.isEmpty()) { // the first tree we see in the array
    				this.setFirst(arr[i]);
    				this.setMin(arr[i]);
    				arr[i].setNext(arr[i]);
    				arr[i].setPrev(arr[i]);
    			}
    			else { //another tree exits in newHeap
    				insertFirst(arr[i]);
    				this.setFirst(arr[i]);
    				if(arr[i].getKey() < this.findMin().getKey()) { //update min
    					this.setMin(arr[i]);
    				}
    			}
    		}
    	}
    }

    private void toBuckets(HeapNode[] arr) //create heap with only one tree of each degree
    {
    	HeapNode tmp = this.first;
    	tmp.getPrev().setNext(null);
    	HeapNode y;
    	while(tmp != null) {
    		y = tmp;
    		tmp = tmp.getNext();
    		y.setNext(null);
    		y.setPrev(null);
    		while(arr[y.getRank()] != null) {
    			y = link(y, arr[y.rank]);
    			arr[y.getRank()-1] = null;
    		}
    		arr[y.getRank()] = y;
    	}
    }

    private HeapNode link(HeapNode a, HeapNode b) { //connect two given trees
    	HeapNode x;
    	HeapNode y;
    	if(a.getKey() > b.getKey()) {
    		x = b;
    		y = a;
    	}
    	else { // a.key < b.key
    		x = a;
    		y = b;
    	}
    	if(x.getChild() == null) {
    		y.setNext(y);
    		y.setPrev(y);
    	}
    	else { // x has children
    		y.setNext(x.getChild());
    		y.setPrev(x.getChild().getPrev());
    		x.getChild().getPrev().setNext(y);
    		x.getChild().setPrev(y);
    	}
    	y.setParent(x);
    	x.setChild(y);
    	setTotalLinks(1); //increase number of total links by 1
    	setNumTrees(-1); //the amount of trees in a heap is decreased by 1 after a link
    	x.setRank(x.getRank() + 1);
    	return x;
    }
    
    private void deleteNodeFromParent(HeapNode node, HeapNode parent) // delete the node from the parent's children list
    {
    	node.setParent(null);
    	if(node.getNext().equals(node)) { // if we cut node which is only child
    		parent.setChild(null);
    	}
    	else { // we have more than one child, so delete from circular doubly linked list
    		node.getNext().setPrev(node.getPrev());
    		node.getPrev().setNext(node.getNext());
    		if(parent.getChild().equals(node)) {
    			parent.setChild(node.getNext());
    		}
    	}
    }
    
    private void cut(HeapNode node, HeapNode parent) // cuts the node from his parent, and add to the root list
    {
    	deleteNodeFromParent(node, parent); // disconnects the node from the parent
    	insertFirst(node); // insert the node to the first place on roots list (before first)
    	if(node.isMark()) { // node is a root now, so un-mark it.
    		node.unMark();
    		setNumMarked(-1);
    	}
    	parent.setRank(parent.getRank()-1);
    	setNumTrees(1); // every cut adds one more tree to the heap
    	setTotalCuts(1); //update total number of cuts
    }
    
    private void cascadingCut(HeapNode node) 
    {
    	HeapNode parent=node.getParent();
    	cut(node,parent);
    	if(!parent.isRoot()) { //checks if we need to do cascadingCut again
    		if(!parent.isMark()) {
    			parent.mark(); // no need cascadingCut, but mark the parent
    			setNumMarked(1);
    		}
    		else
    			cascadingCut(parent);
    	}
    }
    
   /**
    * public class HeapNode
    * 
    * If you wish to implement classes other than FibonacciHeap
    * (for example HeapNode), do it in this file, not in 
    * another file 
    *  
    */
     
    
    public class HeapNode{

    	public int key;
    	private int rank;
    	private boolean mark;
    	private HeapNode child;
    	private HeapNode next;
    	private HeapNode prev;
    	private HeapNode parent;
    	private HeapNode info;

   	
       	public HeapNode(int key) {
    		this.key = key;
    	}

    	public HeapNode(int key, int rank, boolean mark, HeapNode child, HeapNode next, HeapNode prev, HeapNode parent) {
    		this.key = key;
    		this.rank = rank;
    		this.mark = mark;
    		this.child = child;
    		this.next = next;
    		this.prev = prev;
    		this.parent = parent;
    	}


    	public int getKey() {
    		return this.key;
    	}
    	
    	private void setKey(int key) {
    		this.key=key;
    	}

    	public int getRank() {
    		return rank;
    	}

    	private void setRank(int rank) {
    		this.rank = rank;
    	}

    	public boolean isMark() {
    		return mark;
    	}

    	private void mark() {
    		this.mark = true;
    	}
    	
    	private void unMark() {
    		this.mark = false;
    	}

    	public HeapNode getChild() {
    		return child;
    	}

    	private void setChild(HeapNode child) {
    		this.child = child;
    	}

    	public HeapNode getNext() {
    		return next;
    	}

    	private void setNext(HeapNode next) {
    		this.next = next;
    	}

    	public HeapNode getPrev() {
    		return prev;
    	}

    	private void setPrev(HeapNode prev) {
    		this.prev = prev;
    	}

    	public HeapNode getParent() {
    		return parent;
    	}

    	private void setParent(HeapNode parent) {
    		this.parent = parent;
    	}
    	
    	private boolean isRoot() { // checks if the node is a root of a tree
    		if(this.parent==null)
    			return true;
    		return false;
    	}
    	
        private void disconnecNode() {
        	this.setChild(null);
        	this.setNext(null);
        	this.setPrev(null);
        }
        
		private HeapNode getInfo() {
			return info;
		}
		
		private void setInfo(HeapNode info) {
			this.info = info;
		}
    }
}
