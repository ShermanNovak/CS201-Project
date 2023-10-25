import java.io.*;
import java.util.*;

public class Utility {

    class Boundary {
        int xMin, yMin, xMax, yMax;

        public int getxMin() {
            return xMin;
        }

        public int getyMin() {
            return yMin;
        }

        public int getxMax() {
            return xMax;
        }

        public int getyMax() {
            return yMax;
        }

        public Boundary(int xMin, int yMin, int xMax, int yMax) {
            super();
            /*
            *  Storing two diagonal points 
            */
            this.xMin = xMin;
            this.yMin = yMin;
            this.xMax = xMax;
            this.yMax = yMax;
        }

        public boolean inRange(int x, int y) {
            return (x >= this.getxMin() && x <= this.getxMax()
                    && y >= this.getyMin() && y <= this.getyMax());
        }

        public int getWidth() {
            return this.xMax - this.xMin + 1;
        }

        public int getHeight() {
            return this.yMax - this.yMin + 1;
        }

        public String toString() {
            return String.format("[X1=%d X2=%d] \t[Y1=%d Y2=%d]\n", this.xMin, this.xMax, this.yMin, this.yMax);
        }
    }

    class QuadTreeNode {
        int r, g, b;
        int error;
        boolean is_subdivided;
        Boundary boundary;
        QuadTreeNode northWest, northEast, southWest, southEast;

        QuadTreeNode(Boundary boundary) {
            this.boundary = boundary;
        }

        public String toString() {
            return String.format("RGB: (%d, %d, %d), error: %d, %s", this.r, this.g, this.b, this.error, this.boundary.toString());
        }
    }

    class RGB {
        int r, g, b;
        
        RGB(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public String toString() {
            return String.format("(%d, %d, %d)", this.r, this.g, this.b);
        }
    }

    class QuadTree {
        final int[][][] pixels;
        final int maxHeight;
        final int maxError;

        QuadTree(int[][][] pixels, int maxHeight, int maxError) {
            this.pixels = pixels;
            this.maxHeight = maxHeight;
            this.maxError = maxError;
        }

        int[] getMean(Boundary boundary) {
            int width = boundary.getWidth();
            int height = boundary.getHeight();

            int[] mean = new int[3];

            for (int x = boundary.getxMin(); x <= boundary.getxMax(); x++) {
                for (int y = boundary.getyMin(); y <= boundary.getyMax(); y++) {
                    for (int i = 0; i < 3; i++) {
                        mean[i] += this.pixels[x][y][i];
                    }
                }
            }

            for (int i = 0; i < 3; i++) {
                mean[i] /= width * height;
            }

            return mean;
        }

        int getStandardDeviation(Boundary boundary, int[] mean) {
            int width = boundary.getWidth();
            int height = boundary.getHeight();
            double[] std = new double[3];

            for (int x = boundary.getxMin(); x < boundary.getxMax(); x++) {
                for (int y = boundary.getyMin(); y < boundary.getyMax(); y++) {
                    for (int i = 0; i < 3; i++) {
                        std[i] += Math.pow(this.pixels[x][y][i] - mean[i], 2);
                    }
                }
            }

            for (int i = 0; i < 3; i++) {
                std[i] = Math.sqrt(std[i] / (width * height - 1));
            }

            return (int) ((std[0] + std[1] + std[2]) * width * height);
        }
        
        void createTree(QuadTreeNode node, int height) {
            if (node.is_subdivided) {
                return;
            }

            int[] mean = getMean(node.boundary);
            int error = getStandardDeviation(node.boundary, mean);
            node.error = error;

            if (node.boundary.getWidth() == 1 || node.boundary.getHeight() == 1) {
                node.r = mean[0];
                node.g = mean[1];
                node.b = mean[2];
                return;
            }
 
            if (error <= this.maxError || height >= this.maxHeight) {
                node.r = mean[0];
                node.g = mean[1];
                node.b = mean[2];
                return;
            }

            /**  subdivide node */
            node.is_subdivided = true;

            int xMin = node.boundary.getxMin();
            int xMax = node.boundary.getxMax();
            int yMin = node.boundary.getyMin();
            int yMax = node.boundary.getyMax();

            int xOffset = node.boundary.getWidth() / 2;
            int yOffset = node.boundary.getHeight() / 2;

            node.northWest = new QuadTreeNode(
                    new Boundary(xMin, yMin, xMin + xOffset - 1, yMin + yOffset - 1));
            node.northEast = new QuadTreeNode(
                    new Boundary(xMin + xOffset, yMin, xMax, yMin + yOffset - 1));
            node.southWest = new QuadTreeNode(
                    new Boundary(xMin, yMin + yOffset, xMin + xOffset - 1, yMax));
            node.southEast = new QuadTreeNode(
                    new Boundary(xMin + xOffset, yMin + yOffset, xMax, yMax));

            createTree(node.northWest, height + 1);
            createTree(node.northEast, height + 1);
            createTree(node.southEast, height + 1);
            createTree(node.southWest, height + 1);
        }
        
        void dfs(QuadTreeNode node) {
            if (node == null) {
                return;
            }

            System.out.printf("R: %d, G: %d, B: %d, error: %d, [X1=%d X2=%d] \t[Y1=%d Y2=%d]\n",
                    node.r, node.g, node.b, node.error, node.boundary.getxMin(),
                    node.boundary.getxMax(), node.boundary.getyMin(), node.boundary.getyMax());

            dfs(node.northWest);
            dfs(node.northEast);
            dfs(node.southWest);
            dfs(node.southEast);
        }
        
        void extractData(QuadTreeNode node, List<Boolean> booleanFlags, List<QuadTreeNode> leafNodes) {
            booleanFlags.add(node.is_subdivided);

            if (node.is_subdivided) {
                extractData(node.northWest, booleanFlags, leafNodes);
                extractData(node.northEast, booleanFlags, leafNodes);
                extractData(node.southWest, booleanFlags, leafNodes);
                extractData(node.southEast, booleanFlags, leafNodes);
            } else {
                leafNodes.add(node);
            }
        }
    }

    class ByteEncoder {
        ByteArrayOutputStream byteArrayOutputStream;
        DataOutputStream dataOutputStream;
        String outputFileName;

        ByteEncoder(String outputFileName) {
            this.byteArrayOutputStream = new ByteArrayOutputStream();
            this.dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            this.outputFileName = outputFileName;
        }

        void encodeBooleans(List<Boolean> booleanFlags) throws IOException {
            int numFlags = booleanFlags.size();
            this.dataOutputStream.writeInt(numFlags);

            int byteCount = (int) Math.ceil((double) numFlags / 8);
            for (int byteIndex = 0; byteIndex < byteCount; byteIndex++) {
                byte b = 0;

                for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
                    int listIndex = byteIndex * 8 + bitIndex;
                    if (listIndex >= numFlags || !booleanFlags.get(listIndex)) {
                        continue;
                    }
                    b |= 1 << bitIndex;
                }

                this.dataOutputStream.writeByte(b);
            }
        }

        void outputToFile() throws IOException {
            try (FileOutputStream fileOutputStream = new FileOutputStream(this.outputFileName)) {
                this.byteArrayOutputStream.writeTo(fileOutputStream);
            }
        }

        void encodeTree(int width, int height, List<Boolean> booleanFlags, List<QuadTreeNode> leafNodes)
                throws IOException {
            /** encode image dimensions */
            this.dataOutputStream.writeInt(width);
            this.dataOutputStream.writeInt(height);

            encodeBooleans(booleanFlags);
            for (QuadTreeNode node : leafNodes) {
                this.dataOutputStream.writeByte(node.r);
                this.dataOutputStream.writeByte(node.g);
                this.dataOutputStream.writeByte(node.b);
            }

            outputToFile();
        }
    }

    class ByteDecoder {
        ByteArrayInputStream byteArrayInputStream;
        DataInputStream dataInputStream;
        int numFalseFlags;

        ByteDecoder(String inputFileName) throws IOException {
            readFromFile(inputFileName);
            this.dataInputStream = new DataInputStream(byteArrayInputStream);
        }

        void readFromFile(String inputFileName) throws IOException {
            File file = new File(inputFileName);
            try (FileInputStream fileInputStream = new FileInputStream(inputFileName)) {
                // Create a byte array to hold the file's contents
                byte[] buffer = new byte[(int) file.length()];

                // Read the file into the byte array
                fileInputStream.read(buffer);

                // Create a ByteArrayInputStream using the byte array
                this.byteArrayInputStream = new ByteArrayInputStream(buffer);
            }
        }

        int readInt() throws IOException {
            return this.dataInputStream.readInt();
        }

        byte readByte() throws IOException {
            return this.dataInputStream.readByte();
        }

        Queue<Boolean> decodeBooleans() throws IOException {
            int numFlags = this.dataInputStream.readInt();

            Queue<Boolean> booleanFlags = new LinkedList<>();
            this.numFalseFlags = 0;

            int byteCount = (int) Math.ceil((double) numFlags / 8);
            for (int byteIndex = 0; byteIndex < byteCount; byteIndex++) {
                byte byteData = this.dataInputStream.readByte();

                for (int bitIndex = 0; bitIndex < 8; bitIndex++) {
                    int listIndex = byteIndex * 8 + bitIndex;
                    if (listIndex < numFlags) {
                        boolean flag = (byteData & (1 << bitIndex)) > 0;
                        if (!flag)
                            numFalseFlags++;
                        booleanFlags.add(flag);
                    }
                }
            }

            return booleanFlags;
        }
    }

    class RebuildTree {
        Queue<Boolean> booleanFlags;
        int numFlags;
        Queue<RGB> colors;
        int[][][] pixels;

        RebuildTree(Queue<Boolean> booleanFlags, Queue<RGB> colors, int[][][] pixels) {
            this.booleanFlags = booleanFlags;
            this.numFlags = booleanFlags.size();
            this.colors = colors;
            this.pixels = pixels;
        }

        void reconstructTree(QuadTreeNode node) {
            if (this.booleanFlags.size() == 0 || this.colors.size() == 0) {
                return;
            }

            node.is_subdivided = booleanFlags.remove();

            if (node.is_subdivided) {
                int xMin = node.boundary.getxMin();
                int xMax = node.boundary.getxMax();
                int yMin = node.boundary.getyMin();
                int yMax = node.boundary.getyMax();

                int xOffset = node.boundary.getWidth() / 2;
                int yOffset = node.boundary.getHeight() / 2;

                node.northWest = new QuadTreeNode(
                        new Boundary(xMin, yMin, xMin + xOffset - 1, yMin + yOffset - 1));
                node.northEast = new QuadTreeNode(
                        new Boundary(xMin + xOffset, yMin, xMax, yMin + yOffset - 1));
                node.southWest = new QuadTreeNode(
                        new Boundary(xMin, yMin + yOffset, xMin + xOffset - 1, yMax));
                node.southEast = new QuadTreeNode(
                        new Boundary(xMin + xOffset, yMin + yOffset, xMax, yMax));

                reconstructTree(node.northWest);
                reconstructTree(node.northEast);
                reconstructTree(node.southWest);
                reconstructTree(node.southEast);
            } else {
                RGB color = colors.remove();
                node.r = color.r;
                node.g = color.g;
                node.b = color.b;
            }
        }
        
        void reconstructImage(QuadTreeNode node) {

            if (node == null) {
                return;
            }

            if (node.is_subdivided) {
                reconstructImage(node.southEast);
                reconstructImage(node.southWest);
                reconstructImage(node.northEast);
                reconstructImage(node.northWest);
            } else {
                Boundary boundary = node.boundary;

                for (int x = boundary.getxMin(); x <= boundary.getxMax(); x++) {
                    for (int y = boundary.getyMin(); y <= boundary.getyMax(); y++) {
                        this.pixels[x][y][0] = node.r;
                        this.pixels[x][y][1] = node.g;
                        this.pixels[x][y][2] = node.b;
                    }
                }
            }
        }

        void dfs(QuadTreeNode node) {
            if (node == null) {
                return;
            }

            System.out.printf("R: %d, G: %d, B: %d, error: %d, [X1=%d X2=%d] \t[Y1=%d Y2=%d]\n",
                    node.r, node.g, node.b, node.error, node.boundary.getxMin(),
                    node.boundary.getxMax(), node.boundary.getyMin(), node.boundary.getyMax());

            dfs(node.northWest);
            dfs(node.northEast);
            dfs(node.southWest);
            dfs(node.southEast);
        }
    }

    public void Compress(int[][][] pixels, String outputFileName) throws IOException {

        int width = pixels.length;
        int height = pixels[0].length;

        QuadTree tree = new QuadTree(pixels, 100, 100);
        QuadTreeNode root = new QuadTreeNode(new Boundary(0, 0, width-1, height-1));
        tree.createTree(root, 0);
        //tree.dfs(root);

        List<Boolean> booleanFlags = new ArrayList<Boolean>();
        List<QuadTreeNode> leafNodes = new ArrayList<QuadTreeNode>();

        tree.extractData(root, booleanFlags, leafNodes);

        ByteEncoder byteEncoder = new ByteEncoder(outputFileName);
        byteEncoder.encodeTree(width, height, booleanFlags, leafNodes); 
    }

    public int[][][] Decompress(String inputFileName) throws IOException, ClassNotFoundException {
        
        ByteDecoder byteDecoder = new ByteDecoder(inputFileName);
        int width = byteDecoder.readInt();
        int height = byteDecoder.readInt();

        QuadTreeNode root = new QuadTreeNode(new Boundary(0, 0, width-1, height-1));
        int[][][] pixels = new int[width][height][3];

        Queue<Boolean> booleanFlags = byteDecoder.decodeBooleans();
        Queue<RGB> colors = new LinkedList<>();
        
        for (int i = 0; i < byteDecoder.numFalseFlags; i++) {
            int r = Byte.toUnsignedInt(byteDecoder.readByte());
            int g = Byte.toUnsignedInt(byteDecoder.readByte());
            int b = Byte.toUnsignedInt(byteDecoder.readByte());
            RGB color = new RGB(r, g, b);
            colors.add(color);
        }

        RebuildTree rebuildTree = new RebuildTree(booleanFlags, colors, pixels);
        rebuildTree.reconstructTree(root);
        //rebuildTree.dfs(root);
        rebuildTree.reconstructImage(root);

        return rebuildTree.pixels;
    }

}