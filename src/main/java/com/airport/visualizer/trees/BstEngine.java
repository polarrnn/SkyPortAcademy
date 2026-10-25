package com.airport.visualizer.trees;

import com.airport.visualizer.core.StepRecorder;
import com.airport.visualizer.model.Flight;
import com.airport.visualizer.trees.TreeSnapshot.Mark;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A real binary search tree (nodes with left/right links) that can narrate what it does.
 * Rule: smaller (earlier) keys live on the LEFT, bigger (later) keys on the RIGHT.
 * Shared by the BST lesson and the Tree Lab.
 */
public final class BstEngine {

    private final Words words;
    private final List<Flight> tableFlights;
    private BstNode root;
    private int nextId;
    private int size;

    // per-operation narration context
    private String caption = "";
    private final List<String> trail = new ArrayList<>();

    public BstEngine(Words words, List<Flight> tableFlights) {
        this.words = words;
        this.tableFlights = tableFlights == null ? Collections.<Flight>emptyList() : tableFlights;
    }

    public BstNode getRoot() {
        return root;
    }

    public int size() {
        return size;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    public boolean contains(int key) {
        BstNode n = root;
        while (n != null) {
            if (key == n.item.getKey()) {
                return true;
            }
            n = key < n.item.getKey() ? n.left : n.right;
        }
        return false;
    }

    public List<TreeItem> inOrder() {
        List<TreeItem> out = new ArrayList<>();
        inOrder(root, out);
        return out;
    }

    private void inOrder(BstNode n, List<TreeItem> out) {
        if (n != null) {
            inOrder(n.left, out);
            out.add(n.item);
            inOrder(n.right, out);
        }
    }

    /** Inserts without recording (used to set up a scenario). Returns false for duplicates. */
    public boolean insertQuietly(TreeItem item) {
        if (root == null) {
            root = new BstNode(nextId++, item);
            size++;
            return true;
        }
        BstNode n = root;
        while (true) {
            if (item.getKey() == n.item.getKey()) {
                return false;
            }
            if (item.getKey() < n.item.getKey()) {
                if (n.left == null) {
                    n.left = new BstNode(nextId++, item);
                    size++;
                    return true;
                }
                n = n.left;
            } else {
                if (n.right == null) {
                    n.right = new BstNode(nextId++, item);
                    size++;
                    return true;
                }
                n = n.right;
            }
        }
    }

    /** Plain delete without recording (used to time the real algorithm). */
    public boolean deleteQuietly(int key) {
        int before = size;
        root = deleteRec(root, key);
        return size < before;
    }

    private BstNode deleteRec(BstNode n, int key) {
        if (n == null) {
            return null;
        }
        if (key < n.item.getKey()) {
            n.left = deleteRec(n.left, key);
        } else if (key > n.item.getKey()) {
            n.right = deleteRec(n.right, key);
        } else {
            if (n.left == null || n.right == null) {
                size--;
                return n.left != null ? n.left : n.right;
            }
            BstNode s = n.right;
            while (s.left != null) {
                s = s.left;
            }
            n.item = s.item;
            n.right = deleteRec(n.right, s.item.getKey());
        }
        return n;
    }

    /** Snapshot of the whole tree with no highlights (for "here is your tree" steps). */
    public BstState plainState(String caption) {
        return new BstState(TreeSnapshot.from(root).build(), caption, Collections.<String>emptyList(), tableFlights,
                Collections.<String>emptySet());
    }

    // ------------------------------------------------------------------ insert

    public boolean insert(TreeItem item, StepRecorder rec) {
        rec.setOperation("insert");
        caption = "Insert " + item.describe();
        trail.clear();
        String what = words.thing() + " " + item.describe();
        if (root == null) {
            snap(rec, "The tree is empty", "There is no tree yet, so " + what
                    + " becomes the ROOT - the very top of the tree, like the captain of the team.",
                    TreeSnapshot.from(null).ghost(-1, true, item.getLabel()), item, null);
            root = new BstNode(nextId++, item);
            size++;
            rec.swap();
            snap(rec, item.getLabel() + " is the root!", "We made one link: the tree now starts at " + item.describe() + ".",
                    TreeSnapshot.from(root).mark(root.id, Mark.NEW), item, root);
            return true;
        }
        snap(rec, "A new " + words.thing() + " arrives: " + item.describe(),
                "We always start at the root (the top) and walk down. At every " + words.thing()
                + " we ask: is the new one " + words.less() + " (go LEFT) or " + words.more() + " (go RIGHT)?",
                TreeSnapshot.from(root), item, null);
        List<Integer> path = new ArrayList<>();
        BstNode n = root;
        while (true) {
            rec.compare();
            if (item.getKey() == n.item.getKey()) {
                trail.add(item.keyText() + " = " + n.item.keyText() + " -> already here");
                snap(rec, "Already in the tree!", item.keyText() + " is the same as " + n.item.describe()
                        + ". A binary search tree keeps only ONE of each " + words.keyName() + ", so we do not add it again.",
                        TreeSnapshot.from(root).markAll(path, Mark.PATH).mark(n.id, Mark.REMOVED), item, n);
                return false;
            }
            boolean goLeft = item.getKey() < n.item.getKey();
            String dir = goLeft ? "LEFT" : "RIGHT";
            trail.add(item.keyText() + " vs " + n.item.keyText() + " -> " + (goLeft ? words.less() : words.more()) + " -> " + dir.toLowerCase());
            snap(rec, "Compare with " + n.item.describe(),
                    "Is " + item.keyText() + " " + words.less() + " or " + words.more() + " than " + n.item.keyText()
                    + "? It is " + (goLeft ? words.less() : words.more()) + ", so we go " + dir + ".",
                    TreeSnapshot.from(root).markAll(path, Mark.PATH).mark(n.id, Mark.CURRENT), item, n);
            path.add(n.id);
            BstNode next = goLeft ? n.left : n.right;
            if (next == null) {
                snap(rec, "Found an empty spot", "There is nobody on the " + dir.toLowerCase() + " of " + n.item.keyText()
                        + ". That empty spot is the new home for " + item.describe() + ".",
                        TreeSnapshot.from(root).markAll(path, Mark.PATH).ghost(n.id, goLeft, item.getLabel()), item, n);
                BstNode fresh = new BstNode(nextId++, item);
                if (goLeft) {
                    n.left = fresh;
                } else {
                    n.right = fresh;
                }
                size++;
                rec.swap();
                snap(rec, item.getLabel() + " joins the tree", "We changed just one link: " + n.item.keyText() + "'s "
                        + dir.toLowerCase() + " child is now " + item.describe() + ". We only looked at " + path.size()
                        + " " + (path.size() == 1 ? words.thing() : words.things()) + ", not the whole tree!",
                        TreeSnapshot.from(root).markAll(path, Mark.PATH).mark(fresh.id, Mark.NEW), item, fresh);
                return true;
            }
            n = next;
        }
    }

    // ------------------------------------------------------------------ search

    public boolean search(TreeItem target, StepRecorder rec) {
        rec.setOperation("search");
        caption = "Search for " + target.keyText();
        trail.clear();
        if (root == null) {
            snap(rec, "Nothing to search", "The tree is empty, so " + target.keyText() + " cannot be in it.",
                    TreeSnapshot.from(null), target, null);
            return false;
        }
        snap(rec, "Where is " + target.keyText() + "?", "We start at the root. Each answer lets us skip a whole side "
                + "of the tree - that is why searching a balanced tree is so fast!", TreeSnapshot.from(root), target, null);
        List<Integer> path = new ArrayList<>();
        BstNode n = root;
        while (n != null) {
            rec.compare();
            if (target.getKey() == n.item.getKey()) {
                trail.add(target.keyText() + " = " + n.item.keyText() + " -> found!");
                snap(rec, "Found it: " + n.item.describe(), "It matches! We found " + n.item.describe() + " after checking only "
                        + (path.size() + 1) + " of the " + size + " " + words.things() + ".",
                        TreeSnapshot.from(root).markAll(path, Mark.PATH).mark(n.id, Mark.FOUND), target, n);
                return true;
            }
            boolean goLeft = target.getKey() < n.item.getKey();
            String dir = goLeft ? "LEFT" : "RIGHT";
            trail.add(target.keyText() + " vs " + n.item.keyText() + " -> " + (goLeft ? words.less() : words.more()) + " -> " + dir.toLowerCase());
            snap(rec, "Check " + n.item.describe(), "Not this one. " + target.keyText() + " is "
                    + (goLeft ? words.less() : words.more()) + " than " + n.item.keyText() + ", so it can only be on the "
                    + dir + ". We ignore the whole other side!",
                    TreeSnapshot.from(root).markAll(path, Mark.PATH).mark(n.id, Mark.CURRENT), target, n);
            path.add(n.id);
            BstNode next = goLeft ? n.left : n.right;
            if (next == null) {
                trail.add("empty spot -> not in the tree");
                snap(rec, target.keyText() + " is not here", "We reached an empty spot on the " + dir.toLowerCase() + " of "
                        + n.item.keyText() + ". If " + target.keyText() + " were in the tree it would be right here - so it is missing.",
                        TreeSnapshot.from(root).markAll(path, Mark.PATH).ghost(n.id, goLeft, target.getLabel()), target, null);
                return false;
            }
            n = next;
        }
        return false;
    }

    // ------------------------------------------------------------------ delete

    public boolean delete(TreeItem target, StepRecorder rec) {
        rec.setOperation("delete");
        caption = "Delete " + target.keyText();
        trail.clear();
        if (root == null) {
            snap(rec, "Nothing to delete", "The tree is empty.", TreeSnapshot.from(null), target, null);
            return false;
        }
        snap(rec, "First, find " + target.keyText(), "Before we can remove something we must find it, using the same "
                + "left / right questions as a search.", TreeSnapshot.from(root), target, null);
        List<Integer> path = new ArrayList<>();
        BstNode parent = null;
        BstNode n = root;
        while (n != null && n.item.getKey() != target.getKey()) {
            rec.compare();
            boolean goLeft = target.getKey() < n.item.getKey();
            trail.add(target.keyText() + " vs " + n.item.keyText() + " -> " + (goLeft ? "left" : "right"));
            snap(rec, "Check " + n.item.describe(), target.keyText() + " is " + (goLeft ? words.less() : words.more())
                    + " than " + n.item.keyText() + ", so we go " + (goLeft ? "LEFT" : "RIGHT") + ".",
                    TreeSnapshot.from(root).markAll(path, Mark.PATH).mark(n.id, Mark.CURRENT), target, n);
            path.add(n.id);
            parent = n;
            n = goLeft ? n.left : n.right;
        }
        if (n == null) {
            trail.add("empty spot -> nothing to delete");
            snap(rec, target.keyText() + " is not in the tree", "We hit an empty spot, so there is nothing to delete. "
                    + "The tree stays exactly the same.", TreeSnapshot.from(root).markAll(path, Mark.PATH), target, null);
            return false;
        }
        rec.compare();
        trail.add(target.keyText() + " = " + n.item.keyText() + " -> found");
        final BstNode victim = n;
        final BstNode victimParent = parent;
        if (victim.isLeaf()) {
            snap(rec, "Case 1: a leaf", victim.item.describe() + " has no children - it is a LEAF at the bottom of the tree. "
                    + "We can simply unhook it.", TreeSnapshot.from(root).markAll(path, Mark.PATH).mark(victim.id, Mark.REMOVED),
                    target, victim);
            replaceChild(victimParent, victim, null);
            rec.swap();
            snap(rec, "Leaf removed", "One link changed: the spot where " + target.keyText() + " was is now empty.",
                    TreeSnapshot.from(root).markAll(path, Mark.PATH), target, null);
        } else if (victim.left == null || victim.right == null) {
            BstNode child = victim.left != null ? victim.left : victim.right;
            snap(rec, "Case 2: one child", victim.item.describe() + " has only ONE child: " + child.item.describe()
                    + ". The child simply moves up and takes its place, like stepping into an empty chair.",
                    TreeSnapshot.from(root).markAll(path, Mark.PATH).mark(victim.id, Mark.REMOVED).mark(child.id, Mark.SPECIAL),
                    target, victim);
            replaceChild(victimParent, victim, child);
            rec.swap();
            snap(rec, child.item.getLabel() + " moved up", "One link changed: "
                    + (victimParent == null ? "the root is now " + child.item.describe() : victimParent.item.keyText()
                    + " now points straight to " + child.item.keyText()) + ". The left/right rule is still true everywhere.",
                    TreeSnapshot.from(root).markAll(path, Mark.PATH).mark(child.id, Mark.NEW), target, child);
        } else {
            snap(rec, "Case 3: two children", victim.item.describe() + " has TWO children, so nobody can just move up. "
                    + "We need the SUCCESSOR: the next " + words.more() + " " + words.thing()
                    + ". Trick: step RIGHT once, then go LEFT as far as you can.",
                    TreeSnapshot.from(root).markAll(path, Mark.PATH).mark(victim.id, Mark.REMOVED), target, victim);
            List<Integer> succPath = new ArrayList<>();
            BstNode succParent = victim;
            BstNode succ = victim.right;
            while (true) {
                rec.compare();
                snap(rec, succ.left == null ? "Successor found: " + succ.item.describe() : "Keep going left",
                        succ.left == null ? succ.item.describe() + " has nobody on its left, so it is the smallest on the right side: "
                        + "the perfect replacement!" : succ.item.keyText() + " still has a " + words.less()
                        + " " + words.thing() + " on its left, so we keep going left.",
                        TreeSnapshot.from(root).markAll(path, Mark.PATH).mark(victim.id, Mark.REMOVED)
                                .markAll(succPath, Mark.PATH).mark(succ.id, succ.left == null ? Mark.SPECIAL : Mark.CURRENT),
                        target, succ);
                if (succ.left == null) {
                    break;
                }
                succPath.add(succ.id);
                succParent = succ;
                succ = succ.left;
            }
            TreeItem moved = succ.item;
            victim.item = moved;
            rec.swap();
            snap(rec, moved.getLabel() + " moves into the empty seat", moved.describe() + " is copied into the spot of "
                    + target.keyText() + ". Everything on the left is still " + words.less() + " and everything on the right is still "
                    + words.more() + "!", TreeSnapshot.from(root).markAll(path, Mark.PATH).mark(victim.id, Mark.NEW)
                            .mark(succ.id, Mark.REMOVED), target, victim);
            if (succParent == victim) {
                succParent.right = succ.right;
            } else {
                succParent.left = succ.right;
            }
            rec.swap();
            snap(rec, "Old copy removed", "The successor's old spot is removed" + (succ.right != null
                    ? " and its right child " + succ.right.item.keyText() + " moves up." : " (it was a leaf).")
                    + " Done!", TreeSnapshot.from(root).markAll(path, Mark.PATH).mark(victim.id, Mark.NEW), target, victim);
        }
        size--;
        return true;
    }

    private void replaceChild(BstNode parent, BstNode oldChild, BstNode newChild) {
        if (parent == null) {
            root = newChild;
        } else if (parent.left == oldChild) {
            parent.left = newChild;
        } else {
            parent.right = newChild;
        }
    }

    private void snap(StepRecorder rec, String title, String narration, TreeSnapshot.Builder b, TreeItem focus,
            BstNode current) {
        Set<String> hi = new LinkedHashSet<>();
        if (focus != null && focus.getFlight() != null) {
            hi.add(focus.getFlight().getFlightNumber());
        }
        if (current != null && current.item.getFlight() != null) {
            hi.add(current.item.getFlight().getFlightNumber());
        }
        rec.record(title, narration, new BstState(b.build(), caption, trail, tableFlights, hi));
    }
}
