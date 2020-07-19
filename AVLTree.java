/**
 * AVLTree
 * An implementation of a AVL Tree with distinct integer keys and info
 * Made by Nir Swartz and Sonia Moushaev as part of Data Structures course in TAU.
 */

public class AVLTree {

	private AVLNode root;
	private AVLNode minNode;
	private AVLNode maxNode;

	public AVLTree() { // Builder
		this.root=null;
		this.minNode=null;
		this.maxNode=null;
	}

	/**
	 * public boolean empty()
	 *
	 * returns true if and only if the tree is empty
	 *
	 */
	public boolean empty() {
		if (this.root == null) {
			return true;
		}
		return false; 
	}

	/**
	 * public String search(int k)
	 *
	 * returns the info of an item with key k if it exists in the tree
	 * otherwise, returns null
	 */
	public String search(int k)
	{
		if(this.empty())
			return null;
		IAVLNode myNode = this.root;

		while (myNode != null && myNode.isRealNode()) {
			if (k == myNode.getKey()) {
				return myNode.getValue();
			}
			else if (k < myNode.getKey()) {
				myNode = myNode.getLeft();
			}
			else {
				myNode = myNode.getRight();
			}
		}
		if (myNode == null | !myNode.isRealNode()) { //k does not exist in tree
			return null;
		}
		return myNode.getValue();  
	}

	/**
	 * public int insert(int k, String i)
	 *
	 * inserts an item with key k and info i to the AVL tree.
	 * the tree must remain valid (keep its invariants).
	 * returns the number of rebalancing operations, or 0 if no rebalancing operations were necessary.
	 * returns -1 if an item with key k already exists in the tree.
	 */
	public int insert(int k, String i) {
		int rebalanceOpp = 0;
		if (this.empty()) { //tree is empty
			this.root = new AVLNode(k, i, 0, null, null, null, 1); //insert node as a root
			this.root.setLeft(new AVLNode(this.root));
			this.root.setRight(new AVLNode(this.root));
			this.minNode=this.root;
			this.maxNode=this.root;
			return rebalanceOpp; 
		}
		else {
			AVLNode nodePosition = TreePosition(k);
			if (nodePosition.getKey() == k) { //key k already exists in the tree
				return rebalanceOpp-1;
			}
			if (k < nodePosition.getKey()) {
				nodePosition.setLeft(new AVLNode(k, i, 0, null, null, nodePosition, 1));
				nodePosition.getLeft().setLeft(new AVLNode((AVLNode) nodePosition.getLeft()));
				nodePosition.getLeft().setRight(new AVLNode((AVLNode) nodePosition.getLeft()));
			}
			else {
				nodePosition.setRight(new AVLNode(k, i, 0, null, null, nodePosition, 1));
				nodePosition.getRight().setLeft(new AVLNode((AVLNode) nodePosition.getRight()));
				nodePosition.getRight().setRight(new AVLNode((AVLNode) nodePosition.getRight()));
			}
			controlSize(1, nodePosition);
			if(this.minNode == null || k < this.minNode.getKey()) //update min pointer to k if k < minNode or this.minNode==null for split function
				this.minNode = TreePosition(k);
			if(this.maxNode == null || k > this.maxNode.getKey()) //update max pointer to k if k > maxNode or this.maxNode==null for split function
				this.maxNode = TreePosition(k);
			rebalanceOpp = this.insertionRebalance(nodePosition);
		}
		return rebalanceOpp;	
	}
	
	/**
	 * public int delete(int k)
	 *
	 * deletes an item with key k from the binary tree, if it is there;
	 * the tree must remain valid (keep its invariants).
	 * returns the number of rebalancing operations, or 0 if no rebalancing operations were needed.
	 * returns -1 if an item with key k was not found in the tree.
	 */
	public int delete(int k)
	{
		if(this.empty())
			return -1;
		AVLNode nodePosition = TreePosition(k);
		if (nodePosition.getKey() != k) { //key k was not found in the tree
			return -1;
		}
		int balanceSuc=0;
		AVLNode parent = (AVLNode) nodePosition.getParent();
		if(parent!=null) { // checks if we the delete the min or max node when not the root
			if(k==this.minNode.getKey())
				this.minNode=successor(nodePosition);
			if(k==this.maxNode.getKey())
				this.maxNode=predecessor(nodePosition);
		}
		boolean flag2=false;
		if (nodePosition.isLeaf()) {
			if (parent == null) { //deletion of root
				setRoot(null);
				this.minNode=null;
				this.maxNode=null;
				return 0;
			}
			else if (nodePosition.isRightNode()){ //node is a right child
				parent.setRight(new AVLNode(parent)); //replace node with virtual node
			}
			else { //node is a left child
				parent.setLeft(new AVLNode(parent)); //replace node with virtual node
			}
			controlSize(-1, parent);
		}
		else if (nodePosition.hasOnlyLeftChild()) { //the child is in left side
			if (parent == null) { //deletion of root
				this.maxNode=predecessor(nodePosition);
				setRoot((AVLNode) nodePosition.getLeft());//defining a new root
			}
			else { //node is not a root
				if (parent.getRight().equals(nodePosition)) { // the node is in right side of his parent
					parent.setRight(nodePosition.getLeft());
				}
				else {// the node is in left side of his parent
					parent.setLeft(nodePosition.getLeft());
				}
				nodePosition.getLeft().setParent(parent);
			}
			controlSize(-1, parent);
		}
		else if (nodePosition.hasOnlyRightChild()) {//the child is in right side
			if (parent == null) { //deletion of root
				this.minNode=successor(nodePosition);
				setRoot((AVLNode) nodePosition.getRight());//defining a new root
			}
			else { //node is not a root
				if (parent.getRight().equals(nodePosition)) { // the node is in right side of his parent
					parent.setRight(nodePosition.getRight());
				}
				else {// the node is in left side of his parent
					parent.setLeft(nodePosition.getRight());
				}
				nodePosition.getRight().setParent(parent);
			}
			controlSize(-1, parent);
		}
		else { 
			AVLNode successor = successor(nodePosition);
			if(successor.equals(this.maxNode)) //delete will change the maxNode
				flag2=true;
			balanceSuc+=delete(successor.getKey());
			if(flag2==true) { // the successor will be maxNode
				this.maxNode=successor;
			}
			successor.setSize(nodePosition.getSize()); //update successor size
			successor.setHeight(nodePosition.getHeight()); //update successor size
			AVLNode successorNewParent = (AVLNode) nodePosition.getParent();
			successor.setLeft(nodePosition.getLeft());
			successor.setRight(nodePosition.getRight());
			successor.setParent(successorNewParent);
			nodePosition.getLeft().setParent(successor);
			nodePosition.getRight().setParent(successor);
			if(successorNewParent!=null) {
				if(successorNewParent.getLeft().equals(nodePosition)) { //the parent of node position is on the left side
					successorNewParent.setLeft(successor);
				}
				else {//the parent of node position is on the right side
					successorNewParent.setRight(successor);
				}
			}
			else {
				setRoot(successor);
			}
		}
		nodePosition.setParent(null);
		nodePosition.setLeft(null);
		nodePosition.setRight(null);
		return balanceSuc+deleteBalancing(parent);
	}

	/**
	 * public String min()
	 *
	 * Returns the info of the item with the smallest key in the tree,
	 * or null if the tree is empty
	 */
	public String min()
	{
		if(this.empty()) //tree is empty
			return null;
		return this.minNode.getValue();
	}


	/**
	 * public String max()
	 *
	 * Returns the info of the item with the largest key in the tree,
	 * or null if the tree is empty
	 */
	public String max()
	{
		if(this.empty()) //tree is empty
			return null;
		return this.maxNode.getValue();
	}

	/**
	 * public int[] keysToArray()
	 *
	 * Returns a sorted array which contains all keys in the tree,
	 * or an empty array if the tree is empty.
	 */
	public int[] keysToArray()
	{
		if (this.empty()) { //tree is empty
			int [] emptyArr = {};
			return emptyArr;
		}
		int[] arr = new int[getRoot().getSize()];
		indexKey=0;
		fillKeysArray(getRoot(),arr);      
		return arr;              
	}

	static int indexKey;

	private void fillKeysArray(IAVLNode myNode,int [] arr) {
		if(myNode.getLeft().isRealNode())
			fillKeysArray(myNode.getLeft(),arr);
		arr[indexKey]=myNode.getKey();
		indexKey++;
		if(myNode.getRight().isRealNode())
			fillKeysArray(myNode.getRight(),arr);  
	}

	/**
	 * public String[] infoToArray()
	 *
	 * Returns an array which contains all info in the tree,
	 * sorted by their respective keys,
	 * or an empty array if the tree is empty.
	 */
	public String[] infoToArray()
	{
		if (this.empty()) { //tree is empty
			String [] emptyArr = {};
			return emptyArr;
		}
		String[] arr = new String[getRoot().getSize()];
		indexInfo=0;
		fillInfoArray(getRoot(),arr);      
		return arr;  
	}

	static int indexInfo;

	private void fillInfoArray(IAVLNode myNode,String [] arr) {
		if(myNode.getLeft().isRealNode())
			fillInfoArray(myNode.getLeft(),arr);
		arr[indexInfo]=myNode.getValue();
		indexInfo++;
		if(myNode.getRight().isRealNode())
			fillInfoArray(myNode.getRight(),arr);  
	}
	/**
	 * public int size()
	 *
	 * Returns the number of nodes in the tree.
	 *
	 * precondition: none
	 * postcondition: none
	 */
	public int size()
	{
		if(getRoot()==null)
			return 0;
		return this.root.getSize();
	}

		
	/**
	 * public string split(int x)
	 *
	 * splits the tree into 2 trees according to the key x. 
	 * Returns an array [t1, t2] with two AVL trees. keys(t1) < x < keys(t2).
	 * precondition: search(x) != null
	 * postcondition: none
	 */   
	public AVLTree[] split(int x)
	{
		AVLNode node=TreePosition(x); // X in the tree because of precondition
		AVLTree[] splited=new AVLTree[2];
		AVLTree smaller=new AVLTree(); //builder t1
		AVLTree bigger=new AVLTree(); //builder t2
		AVLNode tmp_min=this.minNode; //saving min and max values before changing the tree
		AVLNode tmp_max=this.maxNode;
		AVLNode tmp_predecessor=predecessor(node);
		AVLNode tmp_successor=successor(node);
		smaller.maxNode=predecessor(node); // sets tmp min and max for smaller and bigger before changing them
		smaller.minNode=this.minNode;
		bigger.minNode=successor(node);
		bigger.maxNode=this.maxNode;
		if (!node.isLeaf()){ // take care on the node x
			if(node.getLeft().isRealNode()) {
				smaller.setRoot((AVLNode) node.getLeft());
				node.setLeft(new AVLNode(node));
			}
			if(node.getRight().isRealNode()) {
				bigger.setRoot((AVLNode) node.getRight());
				node.setRight(new AVLNode(node));
			}
		}
		node=(AVLNode) node.getParent();
		AVLNode saveParent;
		while(node!=null) { // starting joining from the parent of x
			if(node.getKey()<x) {
				AVLTree tmp=new AVLTree();
				tmp.minNode=tmp_max; //sets tmp min and max tmp tree
				tmp.maxNode=tmp_min;
				if(node.getLeft().isRealNode()) {
					tmp.setRoot((AVLNode) node.getLeft());
					node.setLeft(new AVLNode(node));
				}
				node.setRight(new AVLNode(node));
				saveParent = (AVLNode) node.getParent();
				smaller.join(node,tmp);
				node=saveParent;
			}
			else if(node.getKey()>x) {
				AVLTree tmp=new AVLTree();
				tmp.minNode=tmp_max; //sets tmp min and max tmp tree
				tmp.maxNode=tmp_min;
				if(node.getRight().isRealNode()) {
					tmp.setRoot((AVLNode) node.getRight());
					node.setRight(new AVLNode(node));
				}
				node.setLeft(new AVLNode(node));
				saveParent = (AVLNode) node.getParent();
				bigger.join(node,tmp);
				node=saveParent;
			}
		}
		smaller.maxNode=tmp_predecessor; //setting the min and max
		smaller.minNode=tmp_min;
		bigger.minNode=tmp_successor;
		bigger.maxNode=tmp_max;
		splited[0]=smaller;
		splited[1]=bigger;
		return splited;
	}
		
	/**
	 * public join(IAVLNode x, AVLTree t)
	 *
	 * joins t and x with the tree. 	
	 * Returns the complexity of the operation (rank difference between the tree and t)
	 * precondition: keys(x,t) < keys() or keys(x,t) > keys()
	 * postcondition: none
	 */   
	public int join(IAVLNode x, AVLTree t) //myTree is 'this' tree
	{		
		if (t.empty()) { // myTree can be empty or not
			int heightDifference;
			if(this.empty()) {
				heightDifference = 1;
			}
			else {
				heightDifference = this.getRoot().getHeight() + 1; //rank difference is myTree height
			}
			this.insert(x.getKey(), x.getValue()); //from preconditions we derive that x has a key -> only had it to myTree
			return heightDifference; 
		}
		
		if (this.empty()) { //t is not empty
			int heightDifference = t.getRoot().getHeight() + 1; //rank difference is t tree height
			t.insert(x.getKey(), x.getValue());
			this.setRoot(t.getRoot());
			this.minNode=t.minNode;
			this.maxNode=t.maxNode;
			return heightDifference;
		}

		else { //both trees are not empty
			int myTreeKeys = this.getRoot().getKey();
			int myTreeHeight = this.getRoot().getHeight();
			int tTreeHeight = t.getRoot().getHeight();
			int treeSize = 0;
			AVLNode tmp;	
			int counter = 1;		

			if (x.getKey() > myTreeKeys) { //keys(x,t) > keys()
				this.maxNode=t.maxNode; // the new max of the jointed tree is t.maxNode ( my max < t max)
				if (myTreeHeight == tTreeHeight) {
					x.setLeft(this.getRoot());
					this.getRoot().setParent(x);
					x.setRight(t.getRoot());
					t.getRoot().setParent(x);
					this.setRoot((AVLNode) x);
					controlHeight(x); //change height of new tree
					setNodeSize((AVLNode) x);
					return 1; //rank difference = 0
				}
				else if (myTreeHeight < tTreeHeight) { // t is higher than my tree
					tmp = t.getRoot();
					while (tmp.getHeight() > myTreeHeight) {
						tmp = (AVLNode) tmp.getLeft();
						counter++;
					}
					x.setLeft(this.getRoot());
					this.getRoot().setParent(x);
					x.setRight(tmp);
					x.setParent(tmp.getParent());
					tmp.getParent().setLeft(x);
					tmp.setParent(x);
					treeSize = this.getRoot().getSize();
					this.setRoot(t.getRoot());
				}
				else { //myTree is higher than t
					tmp = this.getRoot();
					while (tmp.getHeight() > tTreeHeight) {
						tmp = (AVLNode) tmp.getRight();
						counter++;
					}
					x.setRight(t.getRoot());
					t.getRoot().setParent(x);
					x.setLeft(tmp);
					x.setParent(tmp.getParent());
					tmp.getParent().setRight(x);
					tmp.setParent(x);
					treeSize = t.getRoot().getSize();
				}
			}
			else { //keys(x,t) < keys()
				this.minNode=t.minNode; // the new min of the jointed tree is t.minNode ( my min > t min)
				if (myTreeHeight == tTreeHeight) {
					x.setLeft(t.getRoot());
					t.getRoot().setParent(x);
					x.setRight(this.getRoot());
					this.getRoot().setParent(x);
					this.setRoot((AVLNode) x);
					controlHeight(x); //change height of new tree
					setNodeSize((AVLNode) x);
					return 1; //rank difference = 0
				}
				if (myTreeHeight < tTreeHeight) { // t is higher than my tree
					tmp = t.getRoot();
					while (tmp.getHeight() > myTreeHeight) {
						tmp = (AVLNode) tmp.getRight();
						counter++;
					}
					x.setRight(this.getRoot());
					this.getRoot().setParent(x);
					x.setLeft(tmp);
					x.setParent(tmp.getParent());
					tmp.getParent().setRight(x);
					tmp.setParent(x);
					treeSize = this.getRoot().getSize();
					this.setRoot(t.getRoot());
				}
				else { //myTree is higher than t
					tmp = this.getRoot(); 
					while (tmp.getHeight() > tTreeHeight) {
						tmp = (AVLNode) tmp.getLeft();
						counter++;
					}
					x.setLeft(t.getRoot());
					t.getRoot().setParent(x);
					x.setRight(tmp);
					x.setParent(tmp.getParent());
					tmp.getParent().setLeft(x);
					tmp.setParent(x);	
					treeSize = t.getRoot().getSize();
				}
			}
			x.setHeight(Math.max(x.getLeft().getHeight(), x.getRight().getHeight()) + 1);			
			controlSize(1 + treeSize, (AVLNode) x.getParent());
			setNodeSize((AVLNode) x);
			insertionRebalance((AVLNode) x.getParent());
			return counter; 
		}
	}

	/**
	 * public int getRoot()
	 *
	 * Returns the root AVL node, or null if the tree is empty
	 *
	 * precondition: none
	 * postcondition: none
	 */
	public AVLNode getRoot()
	{
		return this.root;
	}

	private void setRoot(AVLNode node)
	{
		if(node!=null) {
			node.setParent(null);
		}
		this.root=node;
	}
	private void setNodeSize(AVLNode x) { //defines the size of a specific node by his children
		x.setSize(((AVLNode) x.getLeft()).getSize() + ((AVLNode) x.getRight()).getSize() +1);
	}
	
	private void controlSize(int m, AVLNode node) { //Change Size to all of node ancestors by a factor of m
		AVLNode currNode = node;
		while (currNode != null) {
			currNode.setSize(currNode.getSize() + m);
			currNode = (AVLNode) currNode.getParent();
		}
	}

	private void controlHeight(IAVLNode node) { //Change height to all of node ancestors by a factor of k
		IAVLNode currNode = node;
		while (currNode != null) {
			currNode.setHeight(Math.max(currNode.getLeft().getHeight(), currNode.getRight().getHeight()) + 1);
			currNode = currNode.getParent();
		}
	}
	
	private static void promote(AVLNode node) { //add '1' to height of node
		node.setHeight(node.getHeight() + 1);
	}

	private static void demote(AVLNode node) { //reduce height of node by '1'
		node.setHeight(node.getHeight() - 1);
	}

	private static String archCalc(AVLNode node) { //calculates arches difference and returns a string with left arch in first char and right arch in second char
		int leftArch = node.getHeight() - node.getLeft().getHeight();
		int rightArch = node.getHeight() - node.getRight().getHeight();
		return leftArch+""+rightArch;
	}

	private int insertionRebalance(AVLNode node) { //the input node is the position in tree which was connected our new node
		int counter = 0;
		AVLNode currNode = node;
		if (node.hasTwoChildren()) {
			controlHeight(currNode);
			return 0;
		}
		String archDifference = archCalc(currNode);
		while (currNode != null && (!archDifference.equals("11") && !archDifference.equals("12") && !archDifference.equals("21"))) { //all valid cases
			if((archDifference.equals("01")) || (archDifference.equals("10"))) {
				promote(currNode);
				counter++;
			}
			else { // one arch is '0' and the other is '2'
				if (archDifference.equals("02")) {
					if (archCalc((AVLNode) currNode.getLeft()).equals("12")) { //calculates arches difference of child with '0' arch
						this.rotateRight((AVLNode) currNode.getLeft());
						demote(currNode);
						counter+= 2;
					}
					else { //arch string of child with '0' arch equals "21"
						rotateLeft((AVLNode) currNode.getLeft().getRight());
						demote((AVLNode) currNode.getLeft().getLeft());
						this.rotateRight((AVLNode) currNode.getLeft());
						demote(currNode);
						promote((AVLNode) currNode.getParent());
						counter+= 5;
					}
				}
				else { // archCalc(currNode).equals("20") - the symmetric situation
					if (archCalc((AVLNode) currNode.getRight()).equals("21")) { //calculates arches difference of child with '0' arch
						this.rotateLeft((AVLNode) currNode.getRight());
						demote(currNode);
						counter+= 2;
					}
					else { //arch string of child with '0' arch equals "12"
						rotateRight((AVLNode) currNode.getRight().getLeft());
						demote((AVLNode) currNode.getRight().getRight());
						this.rotateLeft((AVLNode) currNode.getRight());
						demote(currNode);
						promote((AVLNode) currNode.getParent());
						counter+= 5;
					}
				}
				currNode = (AVLNode) currNode.getParent();
			}
			currNode = (AVLNode) currNode.getParent();
			if (currNode != null) {
				archDifference = archCalc(currNode);
			}
		}
		if (currNode != null) {
			controlHeight(currNode);
		}
		return counter;
	}
	
	private int deleteBalancing(AVLNode node) { //get the parent of that the deleted node
		if(node==null) { // we deleted a the entire tree, last delete was the root
			String rootArch=archCalc(getRoot());
			if(rootArch.equals("21") | rootArch.equals("12")|rootArch.equals("11")) // checks if root is OK
				return 0;
			return deleteBalancing(getRoot());
		}
		String myArch=archCalc(node);
		if(node.hasTwoChildren() && (myArch.equals("21") | myArch.equals("12")|myArch.equals("11"))) { // has two children with valid arches
			controlHeight(node);
			return 0;
		}
		else if(node.hasOnlyLeftChild() && ((AVLNode) node.getLeft()).isLeaf()){ //we delete a right leaf
			controlHeight(node);
			return 0;
		}
		else if(node.hasOnlyRightChild() && ((AVLNode) node.getRight()).isLeaf()){ //we delete a left leaf
			controlHeight(node);
			return 0;
		}
		else if(node.isLeaf()) { // we deleted leaf from unary node
			demote(node);
			return 1+deleteBalancing((AVLNode) node.getParent()); //case 1
		}
		else if(myArch.equals("22")) { // "22" arches
			demote(node);
			return 1+deleteBalancing((AVLNode) node.getParent()); //case 1
		}
		else if(myArch.equals("31")) { // "31" arches
			String sonArch=archCalc((AVLNode) node.getRight());
			if(sonArch.equals("11")) { //case 2
				demote(node);
				promote((AVLNode) node.getRight());
				rotateLeft((AVLNode) node.getRight());
				controlHeight(node);
				return 3;
			}
			else if(sonArch.equals("21")) {//case 3
				demote(node);
				demote(node);
				rotateLeft((AVLNode) node.getRight());
				return 3 + deleteBalancing((AVLNode) node.getParent().getParent());
			}
			else {//case 4
				demote(node); 
				demote(node);
				demote((AVLNode) node.getRight());
				rotateRight((AVLNode) node.getRight().getLeft());
				rotateLeft((AVLNode) node.getRight());
				promote((AVLNode) node.getParent());
				return 6 + deleteBalancing((AVLNode) node.getParent().getParent()); 
			}
		}
		else if(myArch.equals("13")) { // has two children with "13" arches
			String sonArch=archCalc((AVLNode) node.getLeft());
			if(sonArch.equals("11")) { //case 2 symmetric
				demote(node);
				promote((AVLNode) node.getLeft());
				rotateRight((AVLNode) node.getLeft());
				controlHeight(node);
				return 3;
			}
			else if(sonArch.equals("12")) {//case 3 symmetric
				demote(node);  
				demote(node);
				rotateRight((AVLNode) node.getLeft());
				return 3 + deleteBalancing((AVLNode) node.getParent().getParent()); 
			}
			else {//case 4 symmetric
				demote(node);  
				demote(node);
				demote((AVLNode) node.getLeft());
				rotateLeft((AVLNode) node.getLeft().getRight());
				rotateRight((AVLNode) node.getLeft());
				promote((AVLNode) node.getParent());
				return 6 + deleteBalancing((AVLNode) node.getParent().getParent());
			}
		}
		return -1; //should never get here
	}

	private void rotateRight(AVLNode node) { //rotate right when node go up
		if(!node.isRealNode()||node.getParent()==null||node.isRightNode()) //if not real node or root or (node is right node), don't do nothing
			return;
		AVLNode parent=(AVLNode) node.getParent();
		AVLNode tmp=(AVLNode) node.getRight();
		AVLNode grandpa=(AVLNode) parent.getParent();
		tmp.setParent(parent);
		parent.setLeft(tmp);
		node.setRight(parent);
		node.setParent(grandpa);
		if(grandpa!=null) {
			if(parent.isRightNode())
				grandpa.setRight(node);
			else 
				grandpa.setLeft(node);
		}
		else // the parent is the root 
		{
			setRoot(node);
		}
		parent.setParent(node);
		node.setSize(parent.getSize()); // set size field on rotation
		parent.setSize(((AVLNode)parent.getLeft()).getSize()+((AVLNode)parent.getRight()).getSize()+1);
	}

	private void rotateLeft(AVLNode node) { //rotate left when node go up
		if(!node.isRealNode()||node.getParent()==null||!node.isRightNode()) //if not real node or root or (node is left node), don't do nothing
			return;
		AVLNode parent=(AVLNode) node.getParent();
		AVLNode tmp=(AVLNode) node.getLeft();
		AVLNode grandpa=(AVLNode) parent.getParent();
		tmp.setParent(parent);
		parent.setRight(tmp);
		node.setLeft(parent);
		node.setParent(grandpa);
		if(grandpa!=null) {
			if(parent.isRightNode())
				grandpa.setRight(node);
			else 
				grandpa.setLeft(node);
		}
		else // the parent is the root
		{
			setRoot(node);
		}
		parent.setParent(node);
		node.setSize(parent.getSize()); // set size field on rotation
		parent.setSize(((AVLNode)parent.getRight()).getSize()+((AVLNode)parent.getLeft()).getSize()+1);
	}

	private AVLNode TreePosition (int k) { // if key in tree, return his node. else return his parent.

		IAVLNode xPointer = getRoot();
		IAVLNode yPointer = xPointer;
		while (xPointer.isRealNode()) {
			yPointer = xPointer;
			if (k == xPointer.getKey()) {
				return (AVLNode) xPointer;
			}
			else if (k < xPointer.getKey()) {
				xPointer = xPointer.getLeft();
			}
			else {
				xPointer = xPointer.getRight();
			}
		}
		return (AVLNode) yPointer;
	}
	
	private AVLNode successor(IAVLNode node)  { //returns node of successor as seen in class
		if (node.getRight().isRealNode()) {
			return (AVLNode) ((AVLNode)node.getRight()).minNode();
		}
		IAVLNode parent = node.getParent();
		while (parent != null && node.equals(parent.getRight())) {
			node = parent;
			parent = node.getParent();
		}
		return (AVLNode) parent;
	}
	
	private AVLNode predecessor(IAVLNode node) { //returns node of predecessor as seen in class
		if (node.getLeft().isRealNode()) {
			return (AVLNode) ((AVLNode)node.getLeft()).maxNode();
		}
		IAVLNode parent = node.getParent();
		while (parent != null && node.equals(parent.getLeft())) {
			node = parent;
			parent = node.getParent();
		}
		return (AVLNode) parent;
	}
	
	/**
	 * public interface IAVLNode
	 * ! Do not delete or modify this - otherwise all tests will fail !
	 */
	public interface IAVLNode{	
		public int getKey(); //returns node's key (for virtuval node return -1)
		public String getValue(); //returns node's value [info] (for virtuval node return null)
		public void setLeft(IAVLNode node); //sets left child
		public IAVLNode getLeft(); //returns left child (if there is no left child return null)
		public void setRight(IAVLNode node); //sets right child
		public IAVLNode getRight(); //returns right child (if there is no right child return null)
		public void setParent(IAVLNode node); //sets parent
		public IAVLNode getParent(); //returns the parent (if there is no parent return null)
		public boolean isRealNode(); // Returns True if this is a non-virtual AVL node
		public void setHeight(int height); // sets the height of the node
		public int getHeight(); // Returns the height of the node (-1 for virtual nodes)

	}

	/**
	 * public class AVLNode
	 *
	 * If you wish to implement classes other than AVLTree
	 * (for example AVLNode), do it in this file, not in 
	 * another file.
	 * This class can and must be modified.
	 * (It must implement IAVLNode)
	 */
	public class AVLNode implements IAVLNode{

		private int key;
		private String info;
		private int height;
		private AVLNode right;
		private AVLNode left;
		private AVLNode parent;
		private int size;

		public AVLNode(int key,String info,int height,AVLNode right,AVLNode left,AVLNode parent,int size) { // Full builder
			this.key=key;
			this.info=info;
			this.height=height;
			this.right=right;
			this.left=left;
			this.parent=parent;
			this.size=size;
		}
		
		public AVLNode(AVLNode parent) { // Virtual Node Builder
			this (-1,null, -1, null, null, parent, 0);
		}

		public int getKey()
		{
			return this.key;
		}
		public String getValue()
		{
			return this.info;
		}
		public void setLeft(IAVLNode node)
		{
			this.left=(AVLNode) node;
		}
		public IAVLNode getLeft()
		{
			return this.left;
		}
		public void setRight(IAVLNode node)
		{
			this.right=(AVLNode) node;
		}
		public IAVLNode getRight()
		{
			return this.right;
		}
		public void setParent(IAVLNode node)
		{
			this.parent=(AVLNode) node;
		}
		public IAVLNode getParent()
		{
			return this.parent;
		}
		// Returns True if this is a non-virtual AVL node
		public boolean isRealNode()
		{
			if(this.key==-1)
				return false;
			return true;
		}
		public void setHeight(int height)
		{
			this.height=height;
		}
		public int getHeight()
		{
			return this.height;
		}

		public boolean isRightNode() { //checks if the node is right node of the parent or not
			if(this.getParent().getRight().equals(this))
				return true;
			return false;
		}

		protected int getSize() {
			return this.size;
		}

		protected void setSize(int size) {
			this.size=size;
		}

		protected boolean isLeaf() { // checks if the node is a leaf or not
			if(!this.getRight().isRealNode() && !this.getLeft().isRealNode()) { 
				return true;
			}
			return false;
		}

		protected boolean hasOnlyRightChild() { // checks if the node has only right child
			if(this.getRight().isRealNode() && !this.getLeft().isRealNode()) { 
				return true;
			}
			return false;
		}

		protected boolean hasOnlyLeftChild() { // checks if the node has only left child
			if(!this.getRight().isRealNode() && this.getLeft().isRealNode()) { 
				return true;
			}
			return false;
		}

		protected IAVLNode minNode() { //returns node with minimal key
			IAVLNode myNode = this;
			while (myNode.getLeft().isRealNode()) {
				myNode = myNode.getLeft();
			}
			return myNode;
		}
		
		protected IAVLNode maxNode() {  //returns node with maximal key
			IAVLNode myNode = this;
			while (myNode.getRight().isRealNode()) {
				myNode = myNode.getRight();
			}
			return myNode;
		}
		protected boolean hasTwoChildren() { // checks if the node has two children
			if(this.getRight().isRealNode() && this.getLeft().isRealNode()) { 
				return true;
			}
			return false;
		}
	}
}

