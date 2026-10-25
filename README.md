# SkyPort Algorithm Academy

An interactive, kid-friendly Java Swing (JPanel) visualizer that teaches sorting, searching,
Java collections, trees, graphs and hashing using an **airport departures & arrivals board**.

## Open and run in NetBeans

1. **File > Open Project...** and pick the `AirportAlgorithmVisualizer` folder (it is a Maven project).
2. Press **Run Project (F6)**. The project is configured to start `com.airport.visualizer.MainFrame`
   (`exec.mainClass` in `pom.xml`, `nbactions.xml`, and the jar manifest).
3. Open `MainFrame.java` and click the **Design** tab: the form (`MainFrame.form`) embeds the
   `AppShell` panel, so the real interface is shown in the designer. Build the project once
   (**Clean and Build**) so NetBeans can load the `AppShell` class into the designer.

The window opens maximized and fitted to the usable screen area (taskbar excluded).

Command line alternative (JDK 17+):

```bash
mvn -q compile exec:java
```

## What's inside

| Category | Lessons |
|---|---|
| Sort | Bubble, Selection, Insertion (Sort I) - Merge, Quick (Sort II) |
| Search | Linear Search, Binary Search |
| Trees & Graphs | Tree traversals, Binary Search Tree, Min-Heap, BFS / DFS route graph |
| Maps & Sets | HashMap, TreeMap, LinkedHashMap, HashSet, TreeSet, LinkedHashSet, comparison views, hash table |
| Collections | ArrayList, LinkedList, Deque boarding line, PriorityQueue next departure, `Collections` utilities |
| Practice Labs | Sorting Lab, Search Lab, My Flights Lab, Tree Lab, Name Hashing Lab (your own input) |

Every lesson has **Play, Pause, Back, Step, Reset**, a **speed slider** and a timeline scrubber;
live **comparisons, swaps, elapsed time, real run time and Big-O**; a step-by-step story written
for a 9-year-old; a Big-O / "when to use it" tab; and a `JTable` of the 24 sample flights.

## Design (OOP & SOLID)

* `core.Visualizer` is the one interface every demo implements; the UI (`VisualizerView`,
  `PlayerPanel`) depends only on it (**Dependency Inversion**, **Liskov**).
* New lessons are added by writing a class and registering one line in `app.LessonCatalog`
  - no screen code changes (**Open/Closed**).
* Small focused classes: `PlaybackController` (timing), `ControlBar` (buttons), `MetricsBar`,
  `NarrationPanel`, `VisualCanvas`, one class per algorithm / structure (**Single Responsibility**).
* Sorting uses the Strategy pattern (`SortAlgorithm` implementations inside one `SortVisualizer`).
* Snapshots (`Step` + immutable state objects) make Back / scrubbing trivial and safe.

## ISO/IEC 25010 quality notes

* **Functional suitability** - real `java.util` structures and real algorithms produce every step.
* **Performance efficiency** - steps are pre-computed once; painting is cheap; screens load lazily.
* **Usability** - consistent layout, plain-language narration (learnability), keyboard shortcuts
  (Space / Left / Right / R), tooltips, focus rings, colors always paired with words/legends,
  inline validation messages instead of pop-ups (user error protection), polished visual design.
* **Reliability** - all input is validated (empty, duplicate, invalid, too many values); painting and
  screen creation are guarded; an uncaught-error handler shows a friendly dialog instead of crashing.
* **Maintainability** - modular packages, one class per concept, shared theme and drawing helpers.
* **Portability** - pure Java 17+ Swing, no external libraries.
