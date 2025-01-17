<h2>How to use this applet</h2>
On top there are two big panels. They show [-10,10]^2. The left panel shows the primal situation, where the objects are points. The right panel shows the dual situation, where the objects are lines. Points can be added by left-clicking in the left panel or by using the text fields and the “place points at” panel. Right clicking deletes points in either panel. Hovering over a point highlights it in both panels. Pressing space or using the Toggle color button changes the color of the points added. The view in the right panel can be zoomed by using the mouse wheel or dragging the right mouse button, the left mouse button pans it and Reset view resets it. 
<br>
The algorithm can be activated one step at a time, or all remaining steps. Reset erases the points, start again keeps the points but erases the progress of the algorithm. All buttons can be activated by use the underlined key. There are some presets of points that can be added, they are explained in greater detail below.

<h2>Explanation of the algorithm</h2>
<h3>The problem</h3>
We are given two points sets in the plane, blue and red and we want to find a line that halves both sets. This means that we want at most half the points of any given color to be on one side of the line. For this purpose, points on the line are not counted to either side. They are, however counted to the total number of points, which leads to situations such as "all points on one side" (see below).

<h3>Duality to the rescue</h3>
For the algorithm, it is useful to view the dual of the points. The dual of a point (a,b) is the line y=ax+b. Note that this definition differs slightly from the one we used in the lecture (see <a href="#ref4">[4]</a>). We can see that if a point is above/on/below a line, the dual of the point will be above/on/below the dual of the line. Another facet of duality is that it gets tedious differentiating between points and lines. So from this point on, they are treated as the same object with two different ways of looking at them and whenever we write points or lines, you should assume we mean both.
<br>
In our applet, the points are displayed and added on the left side and the lines they belong to are displayed on the right side. Feel free to place a point and drag it around to see how the primal and dual cases interact.
<br>
If we restrict the problem to just one set of points, we see that a line is below no more than half the points and above no more than half the points if and only if the dual of the line is above and below no more than half the duals of the points. For each x, there is at least one point (x,y) that is below and above no more than half the lines. The union of all these points is called the median level. Every point on the media level corresponds to a line that halves the points. Also, if the number of points in the set is odd, the median level looks like a jagged line which is composed of line segments. These line sections run between segments. If the number is even, we may delete any one point and the resulting median level is a subset of the previous median level, so wlog the point sets are of odd size.
<br>
Coming back to the case with two point sets, there is a median level for each point set.
If we find an intersection point of the two median levels, we find a line that halves both the red and the blue point set.

<h3>A few words about general position</h3>
How do we know that the red and blue median levels intersect? One important thing to note is that the two unbounded segments of the median level are on the same line. If all lines have differing slopes, we know that the blue and red median levels switch sides in the long run, so they intersect an odd number of times. We use a technique called simulation of simplicity (see <a href="#ref2">[2]</a>) to treat all lines as if they were in general position. General position for this problem means that all slopes of lines are different and consequently every pair of lines has exactly one crossing, even those that are parallel before simulation of simplicity.
This also lets us treat all intersections as having a different x value, which will be useful later.

<h3>The algorithm by Lo, Matoušek and Steiger (see <a href="#ref1">[1]</a> for more details)</h3>
We know we want to find an intersection of the red and blue median levels, but calculating them completely is too costly, we want to be done in O(n) time. The algorithm operates iteratively. Each iteration is guaranteed to remove a constant fraction of lines that we don’t need for our ham sandwich cut and takes time linearly in the number of remaining points. Each iteration is devided into steps, which we will explain in more detail:
<p>Step one: The considered interval (at the beginning, this is all of the real numbers) is devided into halfopen subintervals, such that each subinterval contains at most 1/32 of all crossings (at least one). In the right panel, the intervals are shown as delimited by vertical soft grey lines. All of the considered interval is delimited by thicker black lines.</p>
<p>Step two: One interval is chosen in which the blue and red median levels cross an odd number of times. This is done by binary search: for one x, we can find out via quickselect in O(n) time whether the blue or the red median level is higher. If they change inside some interval, they cross an odd number of times. Note that steps one and two can be done in linear time (see <a href="#ref3">[3]</a>).</p>
<p>Step three: A trapeze is constructed around the median level of the bigger of the two sets. We do this by counting ⅛ of all lines of that color up and down on both sides of the interval and connecting the four points. Because of the limited number of crossings inside the interval, we can prove that the median level stays completely inside the trapeze. The trapeze is drawn in thick black lines. There is a further “step” here that just moves the camera to the trapeze.</p>
<p>Step four: We delete all lines that do not intersect the trapeze. Because we know one of the median levels is completely inside the trapeze, we know that all crossings between the two median levels are. Therefore, the lines outside the trapeze do not contribute to finding the intersections, and we delete them. If we delete a line above the trapeze, we take note that the number of lines above the median level decreases for the purpose of finding the median level inside our considered interval. Because of the construction of the trapeze, we delete at least a constant fraction of lines. Deleted lines are drawn dotted and faintly respectivly as as empty circles.</p>
<p>
After step four, the process repeats until we only have one blue and one red line remaining. Their crossing must be the ham sandwich cut.
<br>
The algorithm may stop earlier though. If at any point where we check whether the red or the blue median level is higher and it turns out they cross at this point, we found a ham sandwich cut and can stop the algorithm.
</p>

<h3>What about higher dimensions?</h3>
The ham sandwich problem can be generalized to higher dimensions. For example, in three dimensions you can always find a plane that halves three sets of points. Or, more generally, in d dimensions you can always find a hyperplane that halves d sets of points.
The algorithm <a href="#ref1">[1]</a> by Lo, Matoušek and Steiger can also be generalized to higher dimensions and achieves O(n<sup>d-1-a(d)</sup>) time for some small constant a(d). The problem requires at least Ω(n<sup>d-2</sup>) time to solve.

<h3>Some interesting special cases that can arise:</h3>
<h4>All points on one side</h4>
The solution to this point set looks wrong on first glance because there are no points on one side of the solution at all! It doesn’t quite look like what we would expect of a “halving”. But it is still correct. Points on the line are counted to neither side, and not more than half the points of any color are on a side of the line.
<h4>Vertical solution</h4>
In some cases, the solution is a vertical line. This is a special case, and the corresponding point that is dual to the solution is “at infinity” insofar as two parallel lines meet at infinity. For the algorithm, this means counting the number of crossings at infinity. All lines cross because we’re in general position, remember?
<h4>Only one color</h4>
If we only have one color, it is easy to find a ham sandwich cut. It is even possible to construct one for any slope! If you imagine adding a single point of the other color, you can see that you can also have a ham sandwich cut of your set that goes through any given point. (These two statements mean the same thing in projective geometry!) <br>
Our implementation is boring and chooses the cut with slope 0.
<h4>Collinear case</h4>
If all the points are on a line and the numbers around the middle point aren’t symmetrical, there is only a single solution. Contrast this to the general case, where the middle levels may intersect very often.
<h4>Unbounded trapeze</h4>
If the interval chosen in step two is unbounded (this doesn’t happen very often and is tricky to do on purpose), the trapeze is unbounded on one side. In this special example the trapeze is also degenerate, so it is actually just a ray.
<h4>Multiple solutions</h4>
There are some reasons why the same set of points can lead to different ham sandwich cuts with this algorithm. The order in which the points are inserted matters. If the point sets are even, the algorithm deletes the first point in the set to make sure the median level is one-dimensional. The order also determines whether two lines that are parallel before simulation of simplicity meet at positive or negative infinity. The order of the points also determines the order of two crossings with the same x value before simulation of simplicity. This can change the decomposition into intervals and and the result of the algorithm.

<h3>Authors</h3>
Annette Karrer, Sarah Lutteropp, Fabian Stroh

<h3>Links</h3>
<a name="ref1">[1]</a>  Chi-Yuan Lo, J. Matoušek, W. Steiger, Algorithms for ham-sandwich cuts
    The algorithm we use is described in here.
 <a href="http://link.springer.com/article/10.1007/BF02574017">http://link.springer.com/article/10.1007/BF02574017</a>
<br>
<a name="ref2">[2]</a> H. Edelsbrunner and E. Mücke, Simulation of Simplicity: A Technique to Cope With Degenerate Cases in Geometric Algorithms.
    This technique is employed in the algorithm in order to pretend that the points are in general position
 <a href="http://arxiv.org/pdf/math/9410209.pdf">http://arxiv.org/pdf/math/9410209.pdf</a>
<br>
<a name="ref3">[3]</a> J. Matoušek, The construction of ε-nets.
    Lets us find a small interval with an odd number od crossings in O(n).
 <a href="http://link.springer.com/article/10.1007%2FBF02187804">http://link.springer.com/article/10.1007%2FBF02187804</a>
<br>
<a name="ref4">[4]</a> M. Nöllenburg, Vorlesung Algorithmische Geometrie - lecture slides
    Here you can read up on properties of dual line arrangements and their points.
 <a href="http://i11www.iti.uni-karlsruhe.de/_media/teaching/sommer2014/compgeom/algogeom-ss14-vl10.pdf">http://i11www.iti.uni-karlsruhe.de/_media/teaching/sommer2014/compgeom/algogeom-ss14-vl10.pdf</a>
<br>
Other ham sandwich algorithms:
<a name="ref5">[5]</a> H. Edelsbrunner and R. Waupotitsch, Computing a ham-sandwich cut in two dimensions
    This algorithm is arguably easier to understand and runs in O(n log n) time.
 <a href="http://www.sciencedirect.com/science/article/pii/S0747717186800207#">http://www.sciencedirect.com/science/article/pii/S0747717186800207#</a>
<br>
<a name="ref6">[6]</a> D. Mount, Computational Geometry - lecture notes pp. 136 lecture 30
Discussion of the easier special case that the point sets are seperated by a vertical line
 <a href="http://www.cs.umd.edu/class/spring2012/cmsc754/Lects/cmsc754-lects.pdf">http://www.cs.umd.edu/class/spring2012/cmsc754/Lects/cmsc754-lects.pdf</a>
<br>
Other online resources and applets
<br>
<a name="ref7">[7]</a> D MacNevin, Ham Sandwich Cuts
    A cursory explanation of the algorithm we use. It has pictures.
 <a href="http://cgm.cs.mcgill.ca/~athens/cs507/Projects/2002/DanielleMacNevin/algorithm-pg3.html">http://cgm.cs.mcgill.ca/~athens/cs507/Projects/2002/DanielleMacNevin/algorithm-pg3.html</a>
<br>
<a name="ref8">[8]</a> G. Fredricks The ham sandwich Theorem
    An interactive applet.
 <a href="http://gfredericks.com/gfrlog/93">http://gfredericks.com/gfrlog/93</a>