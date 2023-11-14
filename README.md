Implementation of a QuadTree image compression algorithm in Java


Refer to Utility.java for the Compress and Decompress functions. As we were expected to incorporate all of our code in Utility.java, the classes were not separated into different files.

### Tree-Based Image Compression
I wrote this code for our Data Structures and Algorithms group project. 
For more details on the project, refer to our YouTube video here: https://www.youtube.com/watch?v=VGDV31eFib0

### Quad-Tree Image Compression
Quadtree keeps splitting the image into 4 sections recursively and ensures that the sections have standard deviation less than the error parameter.

Input Parameters:
1. Max height of tree
2. Error threshold

### Encoding
To store the QuadTree, we encoded these components in the binary file:
1. **Image height and width**: This is stored at the start of the binary file.
2. **Boolean flags of whether each node is subdivided**: These were represented as 0 or 1 and 8 flags were put together in a byte using bit manipulation.
3. **RGB values of leaf nodes**: Each R/G/B value was encoded as a byte.

### Potential Improvements
- Adding gzip compression will decrease the file size slightly but increase the time taken for compression.

### References
- https://gist.github.com/AbhijeetMajumdar/c7b4f10df1b87f974ef4
- https://github.com/Inspiaaa/QuadTreeImageCompression
