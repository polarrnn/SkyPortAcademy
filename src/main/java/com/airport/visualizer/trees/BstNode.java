package com.airport.visualizer.trees;

/** A real binary-tree node with left / right child links (mutable; snapshots copy it). */
public final class BstNode {

    final int id;
    TreeItem item;
    BstNode left;
    BstNode right;

    BstNode(int id, TreeItem item) {
        this.id = id;
        this.item = item;
    }

    public int getId() {
        return id;
    }

    public TreeItem getItem() {
        return item;
    }

    public BstNode getLeft() {
        return left;
    }

    public BstNode getRight() {
        return right;
    }

    boolean isLeaf() {
        return left == null && right == null;
    }
}
